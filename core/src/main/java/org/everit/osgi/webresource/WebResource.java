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
package org.everit.osgi.webresource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public interface WebResource {

    Bundle getBundle();

    long getContentLength(ContentEncoding contentEncoding);

    String getContentType();

    String getFileName();

    InputStream getInputStream(ContentEncoding contentEncoding, int beginIndex) throws IOException;

    long getLastModified();

    Version getVersion();

    String getLibrary();

    Map<ContentEncoding, Integer> getCacheState();

}
