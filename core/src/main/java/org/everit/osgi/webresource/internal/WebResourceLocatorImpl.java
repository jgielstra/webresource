package org.everit.osgi.webresource.internal;

import java.util.Collection;
import java.util.Objects;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.everit.osgi.webresource.WebResourceLocator;

public class WebResourceLocatorImpl implements WebResourceLocator {

    private String servletName;

    private String servletContextPath;

    public WebResourceLocatorImpl(ServletConfig servletConfig) {
        this.servletName = servletConfig.getServletName();
        Objects.requireNonNull(this.servletName, "Servlet name must not be null!");
        ServletContext servletContext = servletConfig.getServletContext();
        this.servletContextPath = servletContext.getContextPath();
        ServletRegistration servletRegistration = servletContext.getServletRegistration(servletName);
        Collection<String> mappings = servletRegistration.getMappings();
        System.out.println("--------- Mappings: " + mappings);

        // TODO Auto-generated constructor stub
    }

    @Override
    public String resolveWebResourcePath(String lib, String file, String version, boolean appendLastModifiedParameter) {
        // TODO Auto-generated method stub
        return null;
    }

}
