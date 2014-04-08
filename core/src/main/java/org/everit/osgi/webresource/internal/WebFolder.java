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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.osgi.framework.Bundle;

public class WebFolder {

    private final Map<String, List<WebResource>> files = new HashMap<String, List<WebResource>>();

    private final ReentrantReadWriteLock filesRWLock = new ReentrantReadWriteLock(false);

    private final String folderName;

    private final Set<Long> ownerBundleIds = new HashSet<Long>();

    private final Map<String, WebFolder> subFolders = new HashMap<String, WebFolder>();

    private final ReentrantReadWriteLock subFoldersRWLock = new ReentrantReadWriteLock(false);

    public WebFolder(String folderName) {
        this.folderName = folderName;
    }

    public void addWebResource(Bundle bundle, String[] splittedFolderPath, int position, URL webResourceURL,
            Properties mimeMapping) {
        if (splittedFolderPath.length - 1 == position) {
            // Add the webresource to the current folder
            WriteLock filesWriteLock = filesRWLock.writeLock();
            filesWriteLock.lock();
            try {
                // TODO Handle compressed param
                WebResource webResource = new WebResource(bundle, webResourceURL, mimeMapping);
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
                subFolder.addWebResource(bundle, splittedFolderPath, position + 1, webResourceURL, mimeMapping);
            } finally {
                subFoldersWriteLock.unlock();
            }
        }
    }

    public void removeWebResourcesByBundleId(long bundleId) {

    }
}
