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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderContent {

    private final Map<String, List<WebResource>> files = new HashMap<String, List<WebResource>>();

    private long folderSize;

    private final Map<String, FolderContent> subFolders = new HashMap<String, FolderContent>();

    public void addWebResource(String subFolderPath, URL webResourceURL) {

    }
}