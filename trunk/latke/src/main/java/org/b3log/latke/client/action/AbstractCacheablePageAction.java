/*
 * Copyright (C) 2009, 2010, B3log Team
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
package org.b3log.latke.client.action;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.b3log.latke.util.cache.Cache;
import org.b3log.latke.util.cache.CacheFactory;

/**
 * Abstract cacheable page action.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 26, 2010
 */
public abstract class AbstractCacheablePageAction extends AbstractAction {

    /**
     * Default serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(
            AbstractCacheablePageAction.class);
    /**
     * Maximum count of cacheable pages.
     */
    private static final int MAX_CACHEABLE_PAGE_CNT = 1024;
    /**
     * Cache.
     */
    public static final Cache<String, Object> PAGE_CACHE;

    /**
     * Initializes cache.
     */
    static {
        PAGE_CACHE =
                CacheFactory.getCache(CacheFactory.CACHE_LRU_MEMORY_CACHE);

        PAGE_CACHE.setMaxCount(MAX_CACHEABLE_PAGE_CNT);

        LOGGER.info("Initialized page cache[maxCount="
                + MAX_CACHEABLE_PAGE_CNT + "]");
    }

    /**
     * Processes FreeMarker request for the specified request and response.
     *
     * @param request the specified request
     * @param response the specified response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     */
    @Override
    protected final void processFreemarkRequest(final HttpServletRequest request,
                                                final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            final Template template =
                    beforeDoFreeMarkerAction(request, response);
            final Map<?, ?> dataModel = doFreeMarkerAction(template,
                                                           request, response);
            afterDoFreeMarkerTemplateAction(request, response, dataModel,
                                            template);
        } catch (final ActionException e) {
            LOGGER.trace(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                               e.getMessage());

            return;
        }
    }

    /**
     * Processes FreeMarker template with the specified request, data model,
     * template and response.
     *
     * @param request the specified request
     * @param response the specified response
     * @param dataModel the specified data model
     * @param template the specified template
     * @throws ActionException action exception
     */
    @Override
    @SuppressWarnings("unchecked")
    protected final void afterDoFreeMarkerTemplateAction(
            final HttpServletRequest request, final HttpServletResponse response,
            final Map<?, ?> dataModel, final Template template)
            throws ActionException {
        try {
            final StringWriter stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            final PrintWriter writer = response.getWriter();
            final String cachedPageKey = request.getRequestURL().toString()
                    + request.getQueryString();
            LOGGER.trace("Caching page[cachedPageKey=" + cachedPageKey + "]");

            final String pageContent = stringWriter.toString();

            writer.write(pageContent);
            writer.close();

            PAGE_CACHE.put(cachedPageKey, new String(pageContent.getBytes(),
                                                     "UTF-8"));
            LOGGER.trace("Cached page[cachedPageKey=" + cachedPageKey + "]");
        } catch (final TemplateException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ActionException(e);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ActionException(e);
        }
    }
}
