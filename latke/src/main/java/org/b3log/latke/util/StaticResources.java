/*
 * Copyright (c) 2009, 2010, 2011, 2012, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.latke.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.servlet.AbstractServletListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Static resource utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Feb 27, 2012
 */
public final class StaticResources {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(StaticResources.class.getName());
    /**
     * Static resource postfix.
     * 
     * <p>
     * Initializes from  file appengine-web.xml.
     * </p>
     */
    private static final Set<String> STATIC_RESOURCE_POSTFIX = new HashSet<String>();

    static {
        final String webRoot = AbstractServletListener.getWebRoot();
        final File appengineWeb = new File(webRoot + File.separator + "WEB-INF" + File.separator + "appengine-web.xml");

        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.parse(appengineWeb);
            final Element root = document.getDocumentElement();
            root.normalize();

            final Element staticFiles = (Element) root.getElementsByTagName("static-files").item(0);
            final NodeList includes = staticFiles.getElementsByTagName("include");

            LOGGER.log(Level.CONFIG, "Reading static files: ");
            for (int i = 0; i < includes.getLength(); i++) {
                final Element include = (Element) includes.item(i);
                final String path = include.getAttribute("path");
                LOGGER.log(Level.CONFIG, "path=[{0}]", path);
                final String postfix = StringUtils.substringAfterLast(path, ".");

                STATIC_RESOURCE_POSTFIX.add(postfix);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Reads appengine-web.xml failed", e);
            throw new RuntimeException(e);
        }

        LOGGER.log(Level.INFO, "Static files: {0}", STATIC_RESOURCE_POSTFIX);
    }

    /**
     * Determines whether the specified request URI points to a static resource.
     * 
     * @param requestURI the specified request URI
     * @return {@code true} if the specified request URI points to a static 
     * resource, returns {@code false} otherwise
     */
    public static boolean isStatic(final String requestURI) {
        final String requestURIPostfix = StringUtils.substringAfterLast(requestURI, ".");

        return STATIC_RESOURCE_POSTFIX.contains(requestURIPostfix);
    }

    /**
     * Private constructor.
     */
    private StaticResources() {
    }
}
