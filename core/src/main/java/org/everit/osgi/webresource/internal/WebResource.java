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
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.everit.osgi.webresource.WebResourceConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleCapability;

public class WebResource {

    private Bundle bundle;

    private AtomicReference<byte[]> cachedValue;

    private final String fileName;

    private final long lastModified;

    private final long length;

    private final String mimeType;

    private WebResourceReader webResourceReader;

    public WebResource(BundleCapability bundleCapability, URL resourceURL, Properties mimeMapping) {
        this.lastModified = webResourceReader.getLastModified();
        this.length = webResourceReader.getLength();
        this.fileName = webResourceReader.getFileName();
        this.mimeType = resolveMime(mimeMapping);

    }

    public Bundle getBundle() {
        return bundle;
    }

    public String getFileName() {
        return fileName;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getLength() {
        return length;
    }

    private String resolveMime(Properties mimeMapping) {
        String result = null;
        String fileNamePart = this.fileName;
        int indexOfDot = fileNamePart.indexOf('.');
        while (result == null && indexOfDot >= 0) {
            fileNamePart = fileNamePart.substring(indexOfDot);
            result = mimeMapping.getProperty(fileNamePart);
            if (mimeType == null) {
                indexOfDot = fileNamePart.indexOf('.');
            }
        }

        if (result == null) {
            result = mimeMapping.getProperty(this.fileName);
            if (result == null) {
                result = WebResourceConstants.MIME_TYPE_UNKNOWN;
            }
        }

        return result;
    }
}
