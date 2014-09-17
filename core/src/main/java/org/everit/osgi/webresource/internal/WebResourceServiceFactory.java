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

import javax.servlet.Servlet;

import org.everit.osgi.webresource.WebResourceContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class WebResourceServiceFactory implements ServiceFactory<Servlet> {

    private final WebResourceContainer webResourceContainer;
    private final WebResourceUtil webResourceUtil;

    public WebResourceServiceFactory(WebResourceContainer webResourceContainer,
            WebResourceUtil webResourceUtil) {
        this.webResourceContainer = webResourceContainer;
        this.webResourceUtil = webResourceUtil;
    }

    public Servlet getService(Bundle bundle, ServiceRegistration<Servlet> registration) {
        return new WebResourceServlet(webResourceContainer, webResourceUtil);
    }

    public void ungetService(Bundle bundle, ServiceRegistration<Servlet> registration, Servlet service) {
        // Do nothing as the lifecycle of the servlet is handled in its init and destroy method.
    }



}
