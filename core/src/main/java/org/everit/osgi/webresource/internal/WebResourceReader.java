package org.everit.osgi.webresource.internal;

import java.io.InputStream;

public abstract class WebResourceReader {

    public abstract String getFileName();

    public abstract InputStream getInputStream(int beginIndex);

    public abstract long getLastModified();

    public abstract long getLength();
}
