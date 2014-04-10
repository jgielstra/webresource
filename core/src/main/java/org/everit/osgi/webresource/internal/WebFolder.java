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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.osgi.framework.Bundle;

public class WebFolder {

    private final Map<String, List<WebResource>> files = new HashMap<String, List<WebResource>>();

    private final ReentrantReadWriteLock filesRWLock = new ReentrantReadWriteLock(false);

    private final String folderName;

    private final Set<Long> ownerBundleIds = new ConcurrentSkipListSet<Long>();

    private final Map<String, WebFolder> subFolders = new HashMap<String, WebFolder>();

    private final ReentrantReadWriteLock subFoldersRWLock = new ReentrantReadWriteLock(false);

    public WebFolder(String folderName) {
        this.folderName = folderName;
    }

    public void addWebResource(Bundle bundle, String[] splittedFolderPath, int position, WebResource webResource,
            Properties mimeMapping) {
        ownerBundleIds.add(bundle.getBundleId());
        if (splittedFolderPath.length - 1 == position) {
            if (webResource != null) {
                // Add the webresource to the current folder
                WriteLock filesWriteLock = filesRWLock.writeLock();
                filesWriteLock.lock();
                try {
                    String fileName = webResource.getFileName();
                    List<WebResource> webResourceList = files.get(fileName);
                    if (webResourceList == null) {
                        webResourceList = new ArrayList<WebResource>();
                        files.put(fileName, webResourceList);
                    }
                    webResourceList.add(webResource);
                } finally {
                    filesWriteLock.unlock();
                }
            }
        } else {
            WriteLock subFoldersWriteLock = subFoldersRWLock.writeLock();
            subFoldersWriteLock.lock();
            try {
                String subFolderName = splittedFolderPath[position + 1];
                WebFolder subFolder = subFolders.get(subFolderName);
                if (subFolder == null) {
                    subFolder = new WebFolder(splittedFolderPath[position + 1]);
                    subFolders.put(subFolderName, subFolder);
                }
                subFolder.addWebResource(bundle, splittedFolderPath, position + 1, webResource, mimeMapping);
            } finally {
                subFoldersWriteLock.unlock();
            }
        }
    }

    public String getFolderName() {
        return folderName;
    }

    public Set<Long> getOwnerBundleIds() {
        return ownerBundleIds;
    }

    public Map<String, WebFolder> getSubFolders() {
        ReadLock readLock = subFoldersRWLock.readLock();
        readLock.lock();
        TreeMap<String, WebFolder> clone = new TreeMap<String, WebFolder>(subFolders);
        readLock.unlock();
        return clone;
    }

    public void removeByBundleId(long bundleId) {
        boolean wasOwner = ownerBundleIds.remove(bundleId);
        if (wasOwner) {
            // Remove the webResources where the bundle id is the same
            WriteLock filesWriteLock = filesRWLock.writeLock();
            Iterator<Entry<String, List<WebResource>>> filesIterator = files.entrySet().iterator();
            while (filesIterator.hasNext()) {
                Entry<String, List<WebResource>> fileEntry = filesIterator.next();
                List<WebResource> webResources = fileEntry.getValue();
                Iterator<WebResource> webResourceIterator = webResources.iterator();
                while (webResourceIterator.hasNext()) {
                    WebResource webResource = webResourceIterator.next();
                    if (webResource.getBundle().getBundleId() == bundleId) {
                        webResourceIterator.remove();
                    }
                }
                if (webResources.isEmpty()) {
                    filesIterator.remove();
                }
            }
            filesWriteLock.unlock();

            // Remove the subFolders recursively
            WriteLock subFoldersWriteLock = subFoldersRWLock.writeLock();
            subFoldersWriteLock.lock();
            Iterator<Entry<String, WebFolder>> subFolderIterator = subFolders.entrySet().iterator();
            while (subFolderIterator.hasNext()) {
                Entry<String, WebFolder> subFolderEntry = subFolderIterator.next();
                WebFolder subFolder = subFolderEntry.getValue();
                subFolder.removeByBundleId(bundleId);
                if (subFolder.getOwnerBundleIds().size() == 0) {
                    subFolderIterator.remove();
                }
            }
            subFoldersWriteLock.unlock();
        }
    }
}
