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

import com.google.common.base.Optional;

//import java.util.Optional;

public interface WebResourceContainer {

    /**
     *
     * @param lib
     *            Name of the library where the resource is located.
     * @param resourceName
     *            Name of the resource / file.
     * @param versionRange
     *            A version range to identify the web resource exactly.
     * @return The web resource if found.
     * @throws NullPointerException
     *             if lib or resourceName is null.
     * @throws IllegalArgumentException
     *             if the version range is not in the expected format.
     */
    Optional<WebResource> findWebResource(String lib, String resourceName, String versionRange)
            throws IllegalArgumentException;

}
