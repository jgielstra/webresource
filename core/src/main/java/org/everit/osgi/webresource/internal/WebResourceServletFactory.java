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

import org.everit.osgi.service.servlet.ServletFactory;
import org.everit.osgi.webresource.WebResourceContainer;

public class WebResourceServletFactory implements ServletFactory {

    private final WebResourceContainer resourceContainer;
    private final WebResourceUtil webResourceUtil;

    public WebResourceServletFactory(WebResourceContainer resourceContainer, WebResourceUtil webResourceUtil) {
        this.resourceContainer = resourceContainer;
        this.webResourceUtil = webResourceUtil;
    }

    @Override
    public Servlet createServlet() {
        return new WebResourceServlet(resourceContainer, webResourceUtil);
    }

}
