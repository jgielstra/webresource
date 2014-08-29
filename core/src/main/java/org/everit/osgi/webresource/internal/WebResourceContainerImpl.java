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
import java.util.Map;
import java.util.Objects;
//import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.felix.utils.version.VersionRange;
import org.everit.osgi.webresource.WebResource;
import org.everit.osgi.webresource.WebResourceContainer;
import org.osgi.framework.Bundle;

import com.google.common.base.Optional;

public class WebResourceContainerImpl implements WebResourceContainer {

    private final Map<String, LibContainer> libContainersByName = new ConcurrentSkipListMap<>();

    private final Map<Bundle, Set<WebResource>> webResourcesByBundles = new ConcurrentHashMap<>();

    public synchronized void addWebResource(final WebResourceImpl webResource) {
        Bundle bundle = webResource.getBundle();
        Set<WebResource> resources = webResourcesByBundles.get(bundle);
        if (resources == null) {
            resources = Collections.newSetFromMap(new ConcurrentHashMap<WebResource, Boolean>());
            webResourcesByBundles.put(bundle, resources);
        }
        resources.add(webResource);

        String library = webResource.getLibrary();
        LibContainer libContainer = libContainersByName.get(library);
        if (libContainer == null) {
            libContainer = new LibContainer();
            libContainersByName.put(library, libContainer);
        }
        libContainer.addWebResource(webResource);
    }

    @Override
    public Optional<WebResource> findWebResource(final String lib, final String resourceName, final String version) {
        Objects.requireNonNull(lib, "WebResource library must not be null");
        Objects.requireNonNull(lib, "WebResource name must not be null");

        LibContainer libContainer = libContainersByName.get(lib);
        if (libContainer == null) {
            return Optional.absent();
        }

        VersionRange versionRange = VersionRange.parseVersionRange(version);
        return libContainer.findWebResource(resourceName, versionRange);

    }

    Map<String, LibContainer> getLibContainersByName() {
        return libContainersByName;
    }

    public synchronized void removeBundle(final Bundle bundle) {
        Set<WebResource> webResources = webResourcesByBundles.remove(bundle);
        for (WebResource webResource : webResources) {
            String library = webResource.getLibrary();
            LibContainer libContainer = libContainersByName.get(library);
            libContainer.removeWebResource(webResource);
            if (libContainer.isEmpty()) {
                libContainersByName.remove(library);
            }
        }
    }
}
