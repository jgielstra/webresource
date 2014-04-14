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
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.osgi.webresource.ContentEncoding;
import org.osgi.framework.Version;

public class WebResourceWebConsolePlugin extends HttpServlet {

    private final WebResourceContainer resourceContainer;

    public WebResourceWebConsolePlugin(WebResourceContainer resourceContainer) {
        this.resourceContainer = resourceContainer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        writer.write("<table class='content'><tr><th class='content container' colspan='8'>Web resources</th></tr>");
        writer.write("<tr><th class='content'>Library</th>");
        writer.write("<th class='content'>File</th>");
        writer.write("<th class='content'>Version</th>");
        writer.write("<th class='content'>Content type</th>");
        writer.write("<th class='content'>Size</th>");
        writer.write("<th class='content'>Bundle</th>");
        writer.write("<th class='content'>Raw</th>");
        writer.write("<th class='content'>Deflate</th>");
        writer.write("<th class='content'>GZip</th></tr>");

        int rawCacheSizeSum = 0;
        int deflateCacheSizeSum = 0;
        int gzipCacheSizeSum = 0;
        Map<String, LibContainer> libContainersByName = resourceContainer.getLibContainersByName();
        DecimalFormat format = new DecimalFormat();
        for (Entry<String, LibContainer> libContainersByNameEntry : libContainersByName.entrySet()) {
            String library = libContainersByNameEntry.getKey();
            LibContainer libContainer = libContainersByNameEntry.getValue();

            Map<String, NavigableMap<Version, Set<WebResourceImpl>>> versionedResourcesByName = libContainer
                    .getVersionedResourcesByName();

            for (Entry<String, NavigableMap<Version, Set<WebResourceImpl>>> versionedResourcesByNameEntry : versionedResourcesByName
                    .entrySet()) {

                String fileName = versionedResourcesByNameEntry.getKey();
                NavigableMap<Version, Set<WebResourceImpl>> resourcesByVersion = versionedResourcesByNameEntry
                        .getValue();

                for (Entry<Version, Set<WebResourceImpl>> resourcesByVersionEntry : resourcesByVersion.entrySet()) {
                    Version version = resourcesByVersionEntry.getKey();
                    Set<WebResourceImpl> resources = resourcesByVersionEntry.getValue();
                    for (WebResourceImpl resource : resources) {
                        writer.write("<tr><td class='content'>" + library + "</td>");
                        writer.write("<td class='content'>" + fileName + "</td>");
                        writer.write("<td class='content'>" + version + "</td>");
                        writer.write("<td class='content'>" + resource.getContentType() + "</td>");
                        writer.write("<td class='content'>" + resource.getRawLength() + "</td>");
                        writer.write("<td class='content'>" + resource.getBundle().toString() + "</td>");
                        Map<ContentEncoding, Integer> cacheState = resource.getCacheState();

                        Integer rawCacheSize = cacheState.get(ContentEncoding.RAW);
                        rawCacheSizeSum += (rawCacheSize == null) ? 0 : rawCacheSize;
                        writer.write("<td class='content'>" + getStringValue(rawCacheSize) + "</td>");

                        Integer deflateCacheSize = cacheState.get(ContentEncoding.DEFLATE);
                        deflateCacheSizeSum += (deflateCacheSize == null) ? 0 : deflateCacheSize;
                        writer.write("<td class='content'>" + getStringValue(deflateCacheSize) + "</td>");

                        Integer gzipCacheSize = cacheState.get(ContentEncoding.GZIP);
                        gzipCacheSizeSum += (gzipCacheSize == null) ? 0 : gzipCacheSize;
                        writer.write("<td class='content'>" + getStringValue(gzipCacheSize) + "</td></tr>");
                    }
                }
            }
        }
        writer.write("</table>");

        writer.write("<table class='content'><tr><th class='content container' colspan='2'>Cache state</th></tr>");
        writer.write("<tr><td class='content'>Raw</td><td class='content'>" + format.format(rawCacheSizeSum)
                + "</td></tr>");
        writer.write("<tr><td class='content'>Deflate</td><td class='content'>" + format.format(deflateCacheSizeSum)
                + "</td></tr>");
        writer.write("<tr><td class='content'>GZip</td><td class='content'>" + format.format(gzipCacheSizeSum)
                + "</td></tr>");
        writer.write("<tr><td class='content'>Sum</td><td class='content'>"
                + format.format(rawCacheSizeSum + deflateCacheSizeSum + gzipCacheSizeSum) + "</td></tr>");
        writer.write("</table>");
    }

    public String getLabel()
    {
        return "everit-webresource";
    }

    public String getTitle()
    {
        return "Everit Webresource";
    }

    private String getStringValue(Integer value) {
        if (value == null) {
            return "empty";
        } else {
            return value.toString();
        }
    }
}
