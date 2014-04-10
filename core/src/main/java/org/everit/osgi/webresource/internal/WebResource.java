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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.everit.osgi.webresource.WebResourceConstants;
import org.osgi.framework.Bundle;

public class WebResource {

    private final Bundle bundle;

    private final Map<ContentEncoding, byte[]> cache = new ConcurrentHashMap<ContentEncoding, byte[]>();

    private final Map<ContentEncoding, Long> contentLengths = new ConcurrentHashMap<ContentEncoding, Long>();

    private final String contentType;

    private final ContentEncoding fileFormat;

    private final String fileName;

    private final long lastModified;

    private final URL resourceURL;

    public WebResource(Bundle bundle, String fileName, long lastModified, URL resourceURL,
            String contentType, ContentEncoding contentEncoding) {
        this.resourceURL = resourceURL;
        // URLConnection urlConnection = resourceURL.openConnection();
        // if (urlConnection instanceof JarURLConnection) {
        // JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
        // JarEntry jarEntry = jarURLConnection.getJarEntry();
        // lastModified = jarEntry.getTime();
        // size = jarEntry.getSize();
        // String name = jarEntry.getName();
        // fileName = name.substring(name.lastIndexOf('/') + 1);
        // }
        this.bundle = bundle;
        this.contentType = contentType;
        this.fileName = fileName;
        this.lastModified = lastModified;
        this.fileFormat = contentEncoding;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public long getContentLength(ContentEncoding contentEncoding) {

    }

    public String getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getInputStream(int beginIndex) throws IOException {
        return resourceURL.openStream();
    }

    public long getLastModified() {
        return lastModified;
    }

    private String resolveMime(Properties mimeMapping) {
        String result = null;
        String fileNamePart = getFileName();
        int indexOfDot = fileNamePart.indexOf('.');
        while (result == null && indexOfDot >= 0) {
            fileNamePart = fileNamePart.substring(indexOfDot);
            result = mimeMapping.getProperty(fileNamePart);
            if (contentType == null) {
                indexOfDot = fileNamePart.indexOf('.');
            }
        }

        if (result == null) {
            result = mimeMapping.getProperty(getFileName());
            if (result == null) {
                result = WebResourceConstants.MIME_TYPE_UNKNOWN;
            }
        }

        return result;
    }
}
