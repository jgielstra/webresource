/**
 * This file is part of Everit - WebResource.
 *
 * Everit - WebResource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - WebResource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - WebResource.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.webresource.internal;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.felix.utils.version.VersionRange;
import org.osgi.framework.Version;

public class LibContainer {

    private final Map<String, NavigableMap<Version, Set<WebResourceImpl>>> versionedResourcesByName =
            new ConcurrentSkipListMap<>();

    public synchronized void addWebResource(final WebResourceImpl resource) {
        String fileName = resource.getFileName();
        NavigableMap<Version, Set<WebResourceImpl>> resourcesByVersion = versionedResourcesByName.get(fileName);
        if (resourcesByVersion == null) {
            resourcesByVersion = new ConcurrentSkipListMap<>();
            versionedResourcesByName.put(fileName, resourcesByVersion);
        }
        Version version = resource.getVersion();
        Set<WebResourceImpl> resources = resourcesByVersion.get(version);
        if (resources == null) {
            resources = Collections.newSetFromMap(new ConcurrentHashMap<WebResourceImpl, Boolean>());
            resourcesByVersion.put(version, resources);
        }
        resources.add(resource);

    }

    public WebResource findWebResource(final String resourceName, final VersionRange versionRange) {
        NavigableMap<Version, Set<WebResourceImpl>> resourceByVersion = versionedResourcesByName.get(resourceName);
        if ((resourceByVersion == null) || (resourceByVersion.size() == 0)) {
            // There is no resource by the name
            return null;
        }
        if ((versionRange == null) || versionRange.getCeiling().equals(VersionRange.INFINITE_VERSION)) {
            // Selecting the highest version of the resource
            WebResource webResource = selectResourceWithHighestVersion(resourceByVersion);
            Version version = webResource.getVersion();
            if (versionRange.contains(version)) {
                return webResource;
            } else {
                return null;
            }
        }

        if (versionRange.isPointVersion()) {
            // Selecting an exact version of resource. Normally comes with expression [x, x] where x is the same.
            Set<WebResourceImpl> resources = resourceByVersion.get(versionRange.getFloor());
            return selectResourceFromSet(resources);
        }

        WebResource result = null;
        Version ceilingVersion = versionRange.getCeiling();
        Entry<Version, Set<WebResourceImpl>> potentialEntry = null;
        if (!versionRange.isOpenCeiling()) {
            potentialEntry = resourceByVersion.floorEntry(ceilingVersion);
        } else {
            potentialEntry = resourceByVersion.lowerEntry(ceilingVersion);
        }
        if ((potentialEntry != null) && versionRange.contains(potentialEntry.getKey())) {
            result = selectResourceFromSet(potentialEntry.getValue());
        }

        return result;
    }

    Map<String, NavigableMap<Version, Set<WebResourceImpl>>> getVersionedResourcesByName() {
        return versionedResourcesByName;
    }

    public boolean isEmpty() {
        return versionedResourcesByName.size() == 0;
    }

    public synchronized void removeWebResource(final WebResource resource) {
        String fileName = resource.getFileName();
        NavigableMap<Version, Set<WebResourceImpl>> resourcesByVersion = versionedResourcesByName.get(fileName);
        Version version = resource.getVersion();
        Set<WebResourceImpl> resources = resourcesByVersion.get(version);
        resources.remove(resource);
        if (resources.size() == 0) {
            resourcesByVersion.remove(version);
            if (resourcesByVersion.size() == 0) {
                versionedResourcesByName.remove(fileName);
            }
        }
    }

    private WebResource selectResourceFromSet(final Set<WebResourceImpl> resources) {
        if (resources == null) {
            return null;
        }
        // TODO what if the set gets empty in the moment where the iterator is requested.
        Iterator<WebResourceImpl> iterator = resources.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    private WebResource selectResourceWithHighestVersion(
            final NavigableMap<Version, Set<WebResourceImpl>> resourceByVersion) {
        Entry<Version, Set<WebResourceImpl>> lastEntry = resourceByVersion.lastEntry();
        if (lastEntry != null) {
            return selectResourceFromSet(lastEntry.getValue());
        } else {
            // This could happen if the resource is removed on a parallel thread after the size is checked.
            return null;
        }
    }
}
