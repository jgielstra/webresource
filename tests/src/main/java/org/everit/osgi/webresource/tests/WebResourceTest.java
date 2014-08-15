/**
 * This file is part of Everit - WebResource Tests.
 *
 * Everit - WebResource Tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - WebResource Tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - WebResource Tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.webresource.tests;

import javax.servlet.Servlet;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.everit.osgi.dev.testrunner.TestRunnerConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

@Component(immediate = true, policy = ConfigurationPolicy.OPTIONAL)
@Properties({
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TESTRUNNER_ENGINE_TYPE, value = "junit4"),
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TEST_ID, value = "WebResourceTest")
})
@Service(value = WebResourceTest.class)
public class WebResourceTest {

    private Server server;
    private ServiceRegistration<Servlet> servletSR;
    @Reference(bind = "setWebResourceServlet", target = "(" + Constants.SERVICE_DESCRIPTION
            + "=Everit WebResource Servlet)")
    private Servlet webResourceServlet;

    @Activate
    public void activate(BundleContext context) {
        server = new Server(8888);
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        server.setHandler(servletContextHandler);

        servletContextHandler.addServlet(new ServletHolder("myServlet", webResourceServlet), "/*");

        try {
            server.start();
            System.out.println("JETTY STARTED AT PORT " + server.getConnectors()[0].getPort());
        } catch (Exception e) {
            try {
                server.stop();
            } catch (Exception e1) {
                e.addSuppressed(e1);
            }
            throw new RuntimeException(e);
        }

    }

    @Deactivate
    public void deactivate() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setWebResourceServlet(Servlet webResourceServlet) {
        this.webResourceServlet = webResourceServlet;
    }
}
