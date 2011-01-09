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
import java.util.logging.Logger;
import org.b3log.latke.Latkes;
import org.b3log.latke.RunsOnEnv;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;

/**
 * Utilities of <a href="http://www.freemarker.org">FreeMarker</a> template
 * engine.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.7, Jan 9, 2011
 */
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
    private static final Cache<String, Object> CACHE;
    /**
     * Template cache name.
     */
    public static final String TEMPLATE_CACHE_NAME = "template";
    /**
     * Maximum count of cacheable template.
     */
    private static final int MAX_CACHEABLE_TEMPLATE_CNT = 1024;

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

        CACHE = CacheFactory.getCache(TEMPLATE_CACHE_NAME);
        final RunsOnEnv runsOnEnv = Latkes.getRunsOnEnv();
        if (runsOnEnv.equals(RunsOnEnv.LOCALE)) {
            CACHE.setMaxCount(MAX_CACHEABLE_TEMPLATE_CNT);
            LOGGER.log(Level.INFO, "Initialized template cache[maxCount={0}]",
                       MAX_CACHEABLE_TEMPLATE_CNT);
        }
    }

    /**
     * Private default constructor.
     */
    private Templates() {
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
        Template ret = (Template) CACHE.get(templateName);
        if (null != ret) {
            LOGGER.log(Level.FINEST, "Get template[templateName={0}] from cache",
                       templateName);
        } else {
            ret = CONFIGURATION.getTemplate(templateName);
            CACHE.put(templateName, ret);
            LOGGER.log(Level.FINEST,
                       "Get template[templateName={0}], then put it into template cache",
                       templateName);
        }

        return ret;
    }
}
