/*
 * Copyright (c) 2009, 2010, 2011, B3log Team
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

package org.b3log.latke.util.freemarker;

import java.util.logging.Level;
import org.b3log.latke.servlet.AbstractServletListener;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utilities of <a href="http://www.freemarker.org">FreeMarker</a> template
 * engine.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.8, Jan 24, 2011
 */
// XXX: thread-safe?
public final class Templates {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(Templates.class.getName());
    /**
     * FreeMarker {@linkplain  Configuration configuration}.
     */
    public static final Configuration CONFIGURATION;
    /**
     * Template cache.
     * <p>
     * &lt;templateName, template&gt;
     * </p>
     */
    public static final Map<String, Template> CACHE =
            new HashMap<String, Template>();
    /**
     * Enables the {@linkplain #CACHE cache}? Default to {@code true}.
     */
    private static boolean cacheEnabled = true;
    /**
     * Template cache name.
     */
    public static final String TEMPLATE_CACHE_NAME = "template";

    static {
        CONFIGURATION = new Configuration();
        CONFIGURATION.setDefaultEncoding("UTF-8");
        try {
            final String webRootPath = AbstractServletListener.getWebRoot();
            LOGGER.log(Level.FINEST, "Web root[path={0}]", webRootPath);

            CONFIGURATION.setDirectoryForTemplateLoading(new File(webRootPath));
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Private default constructor.
     */
    private Templates() {
    }

    /**
     * Enables or disables the template cache.
     *
     * @param enabled {@code true} to enable, disable otherwise
     */
    public static void enableCache(final boolean enabled) {
        cacheEnabled = enabled;
    }

    /**
     * Gets a FreeMarker {@linkplain Template template} with the specified
     * template name.
     *
     * @param templateName the specified template name
     * @return a template
     * @throws IOException io exception
     */
    public static Template getTemplate(final String templateName)
            throws IOException {
        Template ret = null;

        if (cacheEnabled) {
            ret = CACHE.get(templateName);
        }

        if (null != ret) {
            LOGGER.log(Level.FINEST, "Got template[templateName={0}] from cache",
                       templateName);
        } else {
            ret = CONFIGURATION.getTemplate(templateName);

            if (cacheEnabled) {
                CACHE.put(templateName, ret);
                LOGGER.log(Level.FINEST,
                           "Got template[templateName={0}], then put it into template cache",
                           templateName);
            }
        }

        return ret;
    }
}
