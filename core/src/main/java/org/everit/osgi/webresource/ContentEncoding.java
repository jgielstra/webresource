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

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public enum ContentEncoding {

    DEFLATE("deflate"), GZIP("gzip"), RAW("raw");

    public static ContentEncoding resolveEncoding(final HttpServletRequest request) {
        String acceptEncodingHeader = request.getHeader("Accept-Encoding");
        if (acceptEncodingHeader == null) {
            return RAW;
        }
        String[] encodings = acceptEncodingHeader.split(",");
        List<String> encodingList = Arrays.asList(encodings);
        if (encodingList.contains(GZIP.getHeaderValue())) {
            return GZIP;
        }
        if (encodingList.contains(DEFLATE.getHeaderValue())) {
            return DEFLATE;
        }
        return RAW;
    }

    private final String headerValue;

    private ContentEncoding(final String headerValue) {
        this.headerValue = headerValue;
    }

    public String getHeaderValue() {
        return headerValue;
    }
}
