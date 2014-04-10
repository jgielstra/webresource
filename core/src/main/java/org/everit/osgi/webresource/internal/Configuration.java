package org.everit.osgi.webresource.internal;

import org.osgi.framework.Filter;

public class Configuration {

    private final Filter filter;

    private final String name;

    private final String path;

    public Configuration(String name, Filter filter, String path) {
        this.name = name;
        this.filter = filter;
        this.path = path;
    }

    public Filter getFilter() {
        return filter;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

}
