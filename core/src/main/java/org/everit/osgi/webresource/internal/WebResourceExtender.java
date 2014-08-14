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

import java.net.URL;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.everit.osgi.service.servlet.ServletFactory;
import org.everit.osgi.webresource.WebResourceConstants;
import org.everit.osgi.webresource.WebResourceContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;

@Component(name = "org.everit.osgi.webresource.WebResourceExtender", configurationFactory = false, immediate = true,
        policy = ConfigurationPolicy.OPTIONAL, metatype = true)
@Properties({ @Property(name = "logService.target") })
public class WebResourceExtender {

    private class WebResourceBundleTracker extends BundleTracker<Bundle> {

        public WebResourceBundleTracker(final BundleContext context) {
            super(context, Bundle.ACTIVE, null);
        }

        @Override
        public Bundle addingBundle(final Bundle bundle, final BundleEvent event) {
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
                            if (library.endsWith("/")) {
                                library = library.substring(0, library.length() - 1);
                            }

                            String contentType = webResourceUtil.resolveContentType(resourceURL);
                            WebResourceImpl webResource = new WebResourceImpl(bundle, library, fileName, resourceURL,
                                    version, contentType);
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
        public void removedBundle(final Bundle bundle, final BundleEvent event, final Bundle object) {
            resourceContainer.removeBundle(bundle);
        }
    }

    private BundleContext bundleContext;

    private Map<String, Object> componentConfiguration;

    @Reference
    private LogService logService;

    private ServiceRegistration<Servlet> pluginSR;

    private final WebResourceContainerImpl resourceContainer = new WebResourceContainerImpl();

    private ServiceRegistration<WebResourceContainer> resourceContainerSR;

    private ServiceRegistration<ServletFactory> servletFactorySR;

    private BundleTracker<Bundle> webResourceTracker;

    private WebResourceUtil webResourceUtil;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> configuration) {
        this.bundleContext = context;
        this.componentConfiguration = configuration;

        registerWebResourceContainer();

        this.webResourceUtil = new WebResourceUtil(resourceContainer);

        webResourceTracker = new WebResourceBundleTracker(context);
        webResourceTracker.open();

        registerServletFactory();

        registerWebConsolePlugin();
    }

    @Deactivate
    public void deactivate() {
        webResourceTracker.close();
        if (resourceContainerSR != null) {
            resourceContainerSR.unregister();
        }
        if (pluginSR != null) {
            pluginSR.unregister();
        }
        if (servletFactorySR != null) {
            servletFactorySR.unregister();
        }
    }

    private void registerServletFactory() {
        WebResourceServletFactory webResourceServletFactory = new WebResourceServletFactory(resourceContainer,
                webResourceUtil);
        Dictionary<String, Object> serviceProps = new Hashtable<>(componentConfiguration);
        serviceProps.put(Constants.SERVICE_DESCRIPTION, "Everit WebResource ServletFactory");
        servletFactorySR = bundleContext.registerService(ServletFactory.class, webResourceServletFactory, serviceProps);
    }

    private void registerWebConsolePlugin() {
        WebResourceWebConsolePlugin webConsolePlugin = new WebResourceWebConsolePlugin(resourceContainer,
                webResourceUtil);
        Dictionary<String, Object> serviceProps = new Hashtable<>(componentConfiguration);
        serviceProps.put("felix.webconsole.label", "everit-webresources");
        serviceProps.put("felix.webconsole.title", "Everit Webresource");
        serviceProps.put(Constants.SERVICE_DESCRIPTION, "Everit WebResource WebConsole plugin");
        pluginSR = bundleContext.registerService(Servlet.class, webConsolePlugin, serviceProps);
    }

    private void registerWebResourceContainer() {
        Dictionary<String, Object> serviceProps = new Hashtable<>(componentConfiguration);
        serviceProps.put(Constants.SERVICE_DESCRIPTION, "Everit WebResource Container (read-only)");
        resourceContainerSR = bundleContext
                .registerService(WebResourceContainer.class, resourceContainer, serviceProps);
    }

    private String resolveFileName(final URL resourceURL) {
        String externalForm = resourceURL.toExternalForm();

        int indexOfLastSlash = externalForm.lastIndexOf('/');
        if (indexOfLastSlash >= 0) {
            return externalForm.substring(indexOfLastSlash + 1);
        } else {
            return externalForm;
        }

    }
}
