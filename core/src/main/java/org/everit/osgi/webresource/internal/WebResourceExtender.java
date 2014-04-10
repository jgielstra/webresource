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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.utils.filter.FilterImpl;
import org.apache.felix.utils.manifest.Clause;
import org.apache.felix.utils.manifest.Parser;
import org.everit.osgi.webresource.WebResourceConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentException;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;

@Component(name = "org.everit.osgi.webresource.WebResourceExtender", configurationFactory = true, immediate = true)
@Properties({ @Property(name = "clause"), @Property(name = "logService.target") })
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

    private final WebFolder rootFolder = new WebFolder(null);

    private String servicePid;

    private BundleTracker<Bundle> webResourceTracker;

    @Activate
    public void activate(BundleContext context, Map<String, Object> configuration) {
        this.servicePid = (String) configuration.get(Constants.SERVICE_PID);

        Object clausePropObject = configuration.get(WebResourceConstants.PROP_CLAUSE);

        if (clausePropObject == null) {
            throw new ComponentException("Property clause must be defined");
        }

        Clause[] clauses = null;
        if (clausePropObject instanceof String) {

        }

        webResourceTracker = new WebResourceBundleTracker(context);
        webResourceTracker.open();
    }

    private Configuration[] convertClauseString(String clauseString) {
        try {
            Clause[] lClauses = Parser.parseClauses(new String[] { clauseString });
            List<Configuration> configs = new ArrayList<Configuration>();
            for (Clause clause : lClauses) {
                String filterString = clause.getDirective(WebResourceConstants.CAPABILITY_DIRECTIVE_FILTER);
                if (filterString == null) {
                    throw new ComponentException(servicePid + ": filter directive is not defined in clause: " + clause);
                }
                Filter filter;
                try {
                    filter = FilterImpl.newInstance(filterString);
                } catch (InvalidSyntaxException e) {
                    throw new ComponentException(servicePid + ": Cannot parse filter directive: " + clause, e);
                }

                String path = clause.getAttribute(WebResourceConstants.CAPABILITY_ATTRIBUTE_PATH);

                Configuration config = new Configuration(clause.getName(), filter, path);
                configs.add(config);
            }
            return configs.toArray(new Configuration[0]);
        } catch (IllegalArgumentException e) {
            throw new ComponentException("Error in configuration: " + servicePid, e);
        }

    }

    @Deactivate
    public void deactivate() {
        webResourceTracker.close();
    }
}
