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
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.osgi.webresource.ContentEncoding;
import org.everit.osgi.webresource.WebResource;
import org.everit.osgi.webresource.WebResourceConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class WebResourceServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -5267972580396643677L;

    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
            .withLocale(Locale.US).withZone(DateTimeZone.forID("GMT"));

    private final WebResourceContainer resourceContainer;

    public WebResourceServlet(WebResourceContainer webResourceContainer) {
        resourceContainer = webResourceContainer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        int lastIndexOfSlash = pathInfo.lastIndexOf('/');

        if (lastIndexOfSlash == (pathInfo.length() - 1)) {
            http404(resp);
            return;
        }

        String fileName = pathInfo.substring(lastIndexOfSlash + 1);

        String lib = "";
        if (lastIndexOfSlash > 0) {
            lib = pathInfo.substring(1, lastIndexOfSlash);
        }

        String version = req.getParameter(WebResourceConstants.PARAM_VERSION);

        // TODO handle invalid syntax of version range (not 500 but 400 should be thrown with a nice error message in
        // the body)
        WebResource webResource = resourceContainer.findWebResource(lib, fileName, version);
        if (webResource == null) {
            http404(resp);
            return;
        }
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

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doHead(req, resp);
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

    private void http404(final HttpServletResponse resp) {
        try {
            resp.sendError(404, "Resource cannot found");
        } catch (IOException e) {
            // TODO
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String servletName = config.getServletName();
        Objects.requireNonNull(servletName, "Servlet name must not be null!");
        ServletContext servletContext = config.getServletContext();
        String servletContextPath = servletContext.getContextPath();
        ServletRegistration servletRegistration = servletContext.getServletRegistration(servletName);
        Collection<String> mappings = servletRegistration.getMappings();

    }
}
