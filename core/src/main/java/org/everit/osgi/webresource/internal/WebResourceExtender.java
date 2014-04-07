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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.webresource.WebResourceConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;

@Component(name = "org.everit.osgi.webresource.WebResourceExtender", configurationFactory = true)
@Properties({ @Property(name = "clauses"), @Property(name = "logService.target") })
@Service(value = { Servlet.class })
public class WebResourceExtender extends HttpServlet {

    private class WebResourceBundleTracker extends BundleTracker<Bundle> {

        public WebResourceBundleTracker(BundleContext context) {
            super(context, Bundle.ACTIVE, null);
        }

        @Override
        public Bundle addingBundle(Bundle bundle, BundleEvent event) {
            BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);

            List<BundleCapability> capabilities = bundleWiring
                    .getCapabilities(WebResourceConstants.CAPABILITY_NAMESPACE);

            if (capabilities.size() == 0) {
                return null;
            }

            return null;
        }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
        }
    }

    /**
     * Serial version.
     */
    private static final long serialVersionUID = 1L;

    @Reference
    private LogService logService;

    private LinkedHashSet<WebResource> cachedResources = new LinkedHashSet<WebResource>();

    private long currentCacheSize = 0;

    private Map<String, FolderContent> rootFolders = new TreeMap<String, FolderContent>();

    private BundleTracker<Bundle> webResourceTracker;

    @Activate
    public void activate(BundleContext context) {
        webResourceTracker = new WebResourceBundleTracker(context);
        webResourceTracker.open();
    }

    @Deactivate
    public void deactivate() {
        webResourceTracker.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

    }
}
