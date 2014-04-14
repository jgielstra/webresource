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
import java.util.Properties;

public class WebResourceUtil {

    private static final Properties DEFAULT_CONTENT_TYPES;

    private static final String UNKNOWN_CONTENT_TYPE = "application/octet-stream";

    static {
        DEFAULT_CONTENT_TYPES = new Properties();
        try (InputStream inputStream = WebResourceUtil.class
                .getResourceAsStream("/META-INF/default-content-types.properties")) {
            DEFAULT_CONTENT_TYPES.load(inputStream);
        } catch (IOException e) {
            // TODO
            throw new RuntimeException(e);
        }
    }

    public static String resolveContentType(URL url) {
        String extension = url.toExternalForm();
        int lastIndexOfSlash = extension.lastIndexOf('/');

        if (lastIndexOfSlash > 0) {
            if (lastIndexOfSlash < extension.length() - 1) {
                extension = extension.substring(lastIndexOfSlash + 1);
            } else {
                return UNKNOWN_CONTENT_TYPE;
            }
        }

        int indexOfExtensionSeparator = extension.indexOf('.');
        String contentType = null;
        while (indexOfExtensionSeparator >= 0 && contentType == null) {
            if (indexOfExtensionSeparator == extension.length() - 1) {
                contentType = UNKNOWN_CONTENT_TYPE;
            } else {
                extension = extension.substring(indexOfExtensionSeparator + 1);
                contentType = DEFAULT_CONTENT_TYPES.getProperty(extension);
                if (contentType == null) {
                    indexOfExtensionSeparator = extension.indexOf('.');
                }
            }
        }

        if (contentType == null) {
            return UNKNOWN_CONTENT_TYPE;
        } else {
            return contentType;
        }
    }
}
