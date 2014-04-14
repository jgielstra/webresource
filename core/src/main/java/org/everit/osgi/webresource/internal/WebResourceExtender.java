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
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.webresource.ContentEncoding;
import org.everit.osgi.webresource.WebResource;
import org.everit.osgi.webresource.WebResourceConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentException;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;

@Component(name = "org.everit.osgi.webresource.WebResourceExtender", configurationFactory = true, immediate = true,
        policy = ConfigurationPolicy.REQUIRE, metatype = true)
@Properties({ @Property(name = "alias"), @Property(name = "logService.target") })
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

            boolean webResourceAdded = false;
            for (BundleCapability capability : capabilities) {
                Map<String, Object> attributes = capability.getAttributes();
                String libraryPrefix = (String) attributes
                        .get(WebResourceConstants.CAPABILITY_ATTRIBUTE_LIBRARY_PREFIX);

                if (libraryPrefix == null) {
                    libraryPrefix = "";
                } else if (libraryPrefix.endsWith("/")) {
                    logService.log(LogService.LOG_WARNING, "'"
                            + WebResourceConstants.CAPABILITY_ATTRIBUTE_LIBRARY_PREFIX + "' attribute of capability "
                            + WebResourceConstants.CAPABILITY_NAMESPACE + " should not end with '/' character: "
                            + capability.toString());
                    libraryPrefix = libraryPrefix.substring(0, libraryPrefix.length() - 1);
                }

                String resourceFolder = (String) attributes
                        .get(WebResourceConstants.CAPABILITY_ATTRIBUTE_RESOURCE_FOLDER);
                String versionString = (String) attributes.get(WebResourceConstants.CAPABILITY_ATTRIBUTE_VERSION);
                Version version = (versionString != null) ? new Version(versionString) : bundle.getVersion();

                if (resourceFolder == null) {
                    logService.log(LogService.LOG_WARNING,
                            "Capability attribute " + WebResourceConstants.CAPABILITY_ATTRIBUTE_RESOURCE_FOLDER
                                    + " is missing in bundle " + bundle.toString() + ": " + capability.toString());
                } else {
                    Collection<String> entries = bundleWiring.listResources(resourceFolder, "*",
                            BundleWiring.LISTRESOURCES_RECURSE);
                    for (String entry : entries) {
                        if (!entry.endsWith("/")) {
                            URL resourceURL = bundleWiring.getClassLoader().getResource(entry);

                            String fileName = resolveFileName(resourceURL);
                            String library = entry.substring(resourceFolder.length(),
                                    entry.length() - fileName.length());
                            if (library.endsWith("/")) {
                                library = library.substring(0, library.length() - 1);
                            }
                            if (library.startsWith("/")) {
                                library = libraryPrefix + library;
                            } else {
                                library = libraryPrefix + "/" + library;
                            }
                            if (library.startsWith("/")) {
                                library = library.substring(1);
                            }

                            WebResourceImpl webResource = new WebResourceImpl(bundle, library, fileName, resourceURL,
                                    version);
                            resourceContainer.addWebResource(webResource);
                            webResourceAdded = true;
                        }
                    }
                }
            }

            if (webResourceAdded) {
                return bundle;
            } else {
                return null;
            }
        }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
            resourceContainer.removeBundle(bundle);
        }
    }

    private String resolveFileName(URL resourceURL) {
        String externalForm = resourceURL.toExternalForm();

        int indexOfLastSlash = externalForm.lastIndexOf('/');
        if (indexOfLastSlash >= 0) {
            return externalForm.substring(indexOfLastSlash + 1);
        } else {
            return externalForm;
        }

    }

    /**
     * Serial version.
     */
    private static final long serialVersionUID = 1L;

    @Reference
    private LogService logService;

    private String servicePid;

    private BundleTracker<Bundle> webResourceTracker;

    private WebResourceContainer resourceContainer = new WebResourceContainer();

    private ServiceRegistration<Servlet> pluginSR;

    @Activate
    public void activate(BundleContext context, Map<String, Object> configuration) {
        this.servicePid = (String) configuration.get(Constants.SERVICE_PID);

        String alias = (String) configuration.get(WebResourceConstants.PROP_ALIAS);
        if (alias == null || alias.trim().equals("")) {
            throw new ComponentException(servicePid + " - Property alias must be defined");
        }

        webResourceTracker = new WebResourceBundleTracker(context);
        webResourceTracker.open();

        WebResourceWebConsolePlugin webConsolePlugin = new WebResourceWebConsolePlugin(resourceContainer);
        Dictionary<String, Object> serviceProps = new Hashtable<>();
        serviceProps.put("felix.webconsole.label", "everit-webresources");
        serviceProps.put("felix.webconsole.title", "Everit Webresource");
        pluginSR = context.registerService(Servlet.class, webConsolePlugin, serviceProps);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        ContentEncoding contentEncoding = ContentEncoding.resolveEncoding(req);
        String pathInfo = req.getPathInfo();
        int lastIndexOfSlash = pathInfo.lastIndexOf('/');

        if (lastIndexOfSlash == pathInfo.length() - 1) {
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
        if (!ContentEncoding.RAW.equals(contentEncoding)) {
            resp.setHeader("Content-Encoding", contentEncoding.getHeaderValue());
        }
        resp.setContentLength((int) webResource.getContentLength(contentEncoding));

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

    private void http404(HttpServletResponse resp) {
        try {
            resp.sendError(404, "Resource cannot found");
        } catch (IOException e) {
            // TODO
            throw new RuntimeException(e);
        }
    }

    @Deactivate
    public void deactivate() {
        webResourceTracker.close();
        pluginSR.unregister();
    }
}
