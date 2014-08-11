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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.everit.osgi.webresource.ContentEncoding;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class WebResourceImpl implements WebResource {

    private final Bundle bundle;

    private final Map<ContentEncoding, byte[]> cache = new ConcurrentHashMap<ContentEncoding, byte[]>();

    private final String contentType;

    private final String etag;

    private final String fileName;

    private final long lastModified;

    private final String library;

    private final int rawLength;

    private final URL resourceURL;

    private final Version version;

    public WebResourceImpl(final Bundle bundle, final String library, final String fileName, final URL resourceURL,
            final Version version) {
        this.resourceURL = resourceURL;
        this.bundle = bundle;
        contentType = WebResourceUtil.resolveContentType(resourceURL);
        try {
            URLConnection urlConnection = resourceURL.openConnection();
            lastModified = urlConnection.getLastModified();
            rawLength = urlConnection.getContentLength();
        } catch (IOException e) {
            // TODO
            throw new RuntimeException(e);
        }

        this.fileName = fileName;
        this.version = version;
        this.library = library;
        etag = resolveETag();
    }

    public void destroy() {
        cache.clear();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WebResourceImpl other = (WebResourceImpl) obj;
        if (bundle == null) {
            if (other.bundle != null) {
                return false;
            }
        } else if (!bundle.equals(other.bundle)) {
            return false;
        }
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!fileName.equals(other.fileName)) {
            return false;
        }
        if (library == null) {
            if (other.library != null) {
                return false;
            }
        } else if (!library.equals(other.library)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.everit.osgi.webresource.internal.WebResource#getBundle()
     */
    @Override
    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public Map<ContentEncoding, Integer> getCacheState() {
        Map<ContentEncoding, Integer> result = new HashMap<ContentEncoding, Integer>();
        ContentEncoding[] contentEncodings = ContentEncoding.values();
        for (ContentEncoding contentEncoding : contentEncodings) {
            byte[] cachedData = cache.get(contentEncoding);
            if (cachedData != null) {
                result.put(contentEncoding, cachedData.length);
            }
        }
        return result;
    }

    private byte[] getContentData(final ContentEncoding contentEncoding) {
        byte[] contentData = cache.get(contentEncoding);
        if (contentData == null) {
            contentData = readContentIntoCache(contentEncoding);
        }
        return contentData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.everit.osgi.webresource.internal.WebResource#getContentLength(org.everit.osgi.webresource.internal.
     * ContentEncoding)
     */
    @Override
    public long getContentLength(final ContentEncoding contentEncoding) {
        return getContentData(contentEncoding).length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.everit.osgi.webresource.internal.WebResource#getContentType()
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getEtag() {
        return etag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.everit.osgi.webresource.internal.WebResource#getFileName()
     */
    @Override
    public String getFileName() {
        return fileName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.everit.osgi.webresource.internal.WebResource#getInputStream(org.everit.osgi.webresource.internal.ContentEncoding
     * , int)
     */
    @Override
    public InputStream getInputStream(final ContentEncoding contentEncoding, final int beginIndex) throws IOException {
        byte[] contentData = getContentData(contentEncoding);
        ByteArrayInputStream bin = new ByteArrayInputStream(contentData);
        bin.skip(beginIndex);
        return bin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.everit.osgi.webresource.internal.WebResource#getLastModified()
     */
    @Override
    public long getLastModified() {
        return lastModified;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.everit.osgi.webresource.internal.WebResource#getLibrary()
     */
    @Override
    public String getLibrary() {
        return library;
    }

    public int getRawLength() {
        return rawLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.everit.osgi.webresource.internal.WebResource#getVersion()
     */
    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((bundle == null) ? 0 : bundle.hashCode());
        result = (prime * result) + ((fileName == null) ? 0 : fileName.hashCode());
        result = (prime * result) + ((library == null) ? 0 : library.hashCode());
        result = (prime * result) + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    private byte[] longToBytes(final long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(x);
        return buffer.array();
    }

    private synchronized byte[] readContentIntoCache(final ContentEncoding contentEncoding) {
        byte[] contentData = cache.get(contentEncoding);
        if (contentData == null) {
            try (InputStream inputStream = resourceURL.openStream();) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                OutputStream out;

                // TODO store the deflate and gzip compressed format in the way that the common parts are not
                // calculated and stored twice (as the compression part is the same, only the header and tail is
                // different).
                if (ContentEncoding.GZIP.equals(contentEncoding)) {
                    out = new GZIPOutputStream(bout);
                } else if (ContentEncoding.DEFLATE.equals(contentEncoding)) {
                    out = new DeflaterOutputStream(bout);
                } else {
                    out = bout;
                }
                byte[] buf = new byte[1024];
                int r = inputStream.read(buf);
                while (r > -1) {
                    out.write(buf, 0, r);
                    r = inputStream.read(buf);
                }
                out.close();
                contentData = bout.toByteArray();
                cache.put(contentEncoding, contentData);
            } catch (IOException e) {
                // TODO
                throw new RuntimeException(e);
            }
        }
        return contentData;
    }

    private String resolveETag() {
        try (InputStream in = resourceURL.openStream()) {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(library.getBytes());
            messageDigest.update(fileName.getBytes());
            messageDigest.update(version.toString().getBytes());
            byte[] buf = new byte[1024];
            int r = in.read(buf);
            while (r > -1) {
                messageDigest.update(buf, 0, r);
                r = in.read(buf);
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bout.write(messageDigest.digest());
            bout.write(longToBytes(lastModified));
            return String.format("%x", new BigInteger(1, bout.toByteArray()));
        } catch (NoSuchAlgorithmException | IOException e) {
            // TODO
            throw new RuntimeException(e);
        }
    }
}
