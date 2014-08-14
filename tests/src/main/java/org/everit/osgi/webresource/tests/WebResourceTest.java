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

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Servlet;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.dev.testrunner.TestRunnerConstants;
import org.everit.osgi.service.servlet.ServletFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@Component(immediate = true, policy = ConfigurationPolicy.OPTIONAL)
@Properties({
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TESTRUNNER_ENGINE_TYPE, value = "junit4"),
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TEST_ID, value = "WebResourceTest")
})
@Service(value = WebResourceTest.class)
public class WebResourceTest {

    @Reference(bind = "setWebResourceServletFactory")
    private ServletFactory webResourceServletFactory;
    private ServiceRegistration<Servlet> servletSR;

    @Activate
    public void activate(BundleContext context) {
        Servlet servlet = webResourceServletFactory.createServlet();
        Dictionary<String, Object> serviceProps = new Hashtable<String, Object>();
        serviceProps.put("alias", "/static");
        servletSR = context.registerService(Servlet.class, servlet, serviceProps);
    }

    @Deactivate
    public void deactivate() {
        servletSR.unregister();
    }

    public void setWebResourceServletFactory(ServletFactory webResourceServletFactory) {
        this.webResourceServletFactory = webResourceServletFactory;
    }
}
