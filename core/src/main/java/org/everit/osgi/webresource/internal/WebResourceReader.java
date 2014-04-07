package org.everit.osgi.webresource.internal;

import java.io.InputStream;

public interface WebResourceReader {

    String getFileName();

    InputStream getInputStream(int beginIndex);

    long getLastModified();

    long getLength();
}
