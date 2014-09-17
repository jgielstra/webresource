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
import org.everit.osgi.dev.testrunner.TestRunnerConstants;
import org.osgi.framework.ServiceFactory;

@Component(immediate = true, policy = ConfigurationPolicy.OPTIONAL)
@Properties({
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TESTRUNNER_ENGINE_TYPE, value = "junit4"),
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TEST_ID, value = "WebResourceTest")
})
@Service(value = WebResourceTest.class)
public class WebResourceTest {

    @Reference(bind = "setWebResourceServletFactory")
    private ServiceFactory webResourceServletFactory;

    @Activate
    public void activate() {

    }

    @Deactivate
    public void deactivate() {

    }

    public void setWebResourceServletFactory(ServiceFactory webResourceServletFactory) {
        this.webResourceServletFactory = webResourceServletFactory;
    }
}
