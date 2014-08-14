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
import java.util.Collection;
import java.util.Objects;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.osgi.webresource.WebResourceContainer;

public class WebResourceServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -5267972580396643677L;

    private final WebResourceContainer resourceContainer;

    private final WebResourceUtil webResourceUtil;

    public WebResourceServlet(WebResourceContainer webResourceContainer, WebResourceUtil webResourceUtil) {
        resourceContainer = webResourceContainer;
        this.webResourceUtil = webResourceUtil;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        webResourceUtil.findWebResourceAndWriteResponse(req, resp, pathInfo);

    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doHead(req, resp);
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
        System.out.println("Servlet mappings: " + mappings);
        System.out.println("Servlet context path: " + servletContextPath);

        if (servletContext.getMajorVersion() > 3
                || (servletContext.getMajorVersion() == 3 && servletContext.getMinorVersion() > 1)) {

            System.out.println("Virtual host : " + servletContext.getVirtualServerName());
        }

    }
}
