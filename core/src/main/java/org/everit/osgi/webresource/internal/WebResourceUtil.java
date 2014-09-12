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
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.osgi.webresource.ContentEncoding;
import org.everit.osgi.webresource.WebResource;
import org.everit.osgi.webresource.WebResourceConstants;
import org.everit.osgi.webresource.WebResourceContainer;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Optional;

public class WebResourceUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat
            .forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
            .withLocale(Locale.US).withZone(DateTimeZone.forID("GMT"));

    private static final String UNKNOWN_CONTENT_TYPE = "application/octet-stream";

    private final Properties DEFAULT_CONTENT_TYPES;

    private final WebResourceContainer resourceContainer;

    public WebResourceUtil(WebResourceContainer resourceContainer) {
        this.resourceContainer = resourceContainer;
        DEFAULT_CONTENT_TYPES = new Properties();
        try (InputStream inputStream = WebResourceUtil.class
                .getResourceAsStream("/META-INF/default-content-types.properties")) {
            DEFAULT_CONTENT_TYPES.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean etagMatchFound(final HttpServletRequest request, final WebResource webResource) {
        String ifNoneMatchHeader = request.getHeader("If-None-Match");
        if (ifNoneMatchHeader == null) {
            return false;
        }
        String[] etags = ifNoneMatchHeader.split(",");
        int i = 0;
        int n = etags.length;
        boolean matchFound = false;
        while (!matchFound && (i < n)) {
            String etag = etags[i].trim();
            if (etag.equals("\"" + webResource.getEtag() + "\"")) {
                matchFound = true;
            } else {
                i++;
            }
        }
        return matchFound;

    }

    public void findWebResourceAndWriteResponse(HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
        int lastIndexOfSlash = pathInfo.lastIndexOf('/');

        if (lastIndexOfSlash == (pathInfo.length() - 1)) {
            http404(resp);
            return;
        }

        String resourceName = pathInfo.substring(lastIndexOfSlash + 1);

        String lib = "";
        if (lastIndexOfSlash > 0) {
            lib = pathInfo.substring(1, lastIndexOfSlash);
        }

        String version = req.getParameter(WebResourceConstants.PARAM_VERSION);

        Optional<WebResource> optionalWebResource = resourceContainer.findWebResource(lib, resourceName, version);

        writeWebResourceToResponse(req, resp, optionalWebResource);
    }

    private void http404(final HttpServletResponse resp) {
        try {
            resp.sendError(404, "Resource cannot be found");
        } catch (IOException e) {
            // TODO
            throw new RuntimeException(e);
        }
    }

    public String resolveContentType(final URL url) {
        String extension = url.toExternalForm();
        int lastIndexOfSlash = extension.lastIndexOf('/');

        if (lastIndexOfSlash > 0) {
            if (lastIndexOfSlash < (extension.length() - 1)) {
                extension = extension.substring(lastIndexOfSlash + 1);
            } else {
                return UNKNOWN_CONTENT_TYPE;
            }
        }

        int indexOfExtensionSeparator = extension.indexOf('.');
        String contentType = null;
        while ((indexOfExtensionSeparator >= 0) && (contentType == null)) {
            if (indexOfExtensionSeparator == (extension.length() - 1)) {
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

    private void writeWebResourceToResponse(HttpServletRequest req, HttpServletResponse resp,
            Optional<WebResource> optionalwebResource) {
        if (!optionalwebResource.isPresent()) {
            http404(resp);
            return;
        }

        WebResource webResource = optionalwebResource.get();
        resp.setContentType(webResource.getContentType());
        resp.setHeader("Last-Modified", DATE_TIME_FORMATTER.print(webResource.getLastModified()));
        resp.setHeader("ETag", "\"" + webResource.getEtag() + "\"");

        ContentEncoding contentEncoding = ContentEncoding.resolveEncoding(req);
        resp.setContentLength((int) webResource.getContentLength(contentEncoding));

        if (!ContentEncoding.RAW.equals(contentEncoding)) {
            resp.setHeader("Content-Encoding", contentEncoding.getHeaderValue());
        }

        if (etagMatchFound(req, webResource)) {
            resp.setStatus(304);
            return;
        }

        try {
            ServletOutputStream out = resp.getOutputStream();
            InputStream in = webResource.getInputStream(contentEncoding, 0);

            byte[] buf = new byte[1024];
            int r = in.read(buf);
            while (r > -1) {
                out.write(buf, 0, r);
                r = in.read(buf);
            }

            out.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
    }
}
