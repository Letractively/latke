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

package org.b3log.latke.action;

import freemarker.template.Template;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.action.util.PageCaches;
import org.json.JSONObject;

/**
 * Abstract cacheable page action.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.8, Dec 22, 2010
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
            AbstractCacheablePageAction.class.getName());
    /**
     * Key of cached type.
     */
    public static final String CACHED_TYPE = "cachedType";
    /**
     * Key of cached HTML content.
     */
    public static final String CACHED_CONTENT = "cachedContent";
    /**
     * Key of cached oid.
     */
    public static final String CACHED_OID = "cachedOid";

    /**
     * Processes FreeMarker template with the specified request, data model,
     * template and response. Then puts the page response contents into cache
     * with the key getting from request attribute specified by
     * {@linkplain org.b3log.latke.Keys#PAGE_CACHE_KEY}.
     *
     * <p>
     *   <b>Note</b>: This method will write page content to the writer of the
     *   specified response without flush/close it.
     * </p>
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
            final PrintWriter writer = response.getWriter();
            if (response.isCommitted()) { // response has been sent redirect
                writer.flush();

                return;
            }

            final StringWriter stringWriter = new StringWriter();
            template.setOutputEncoding("UTF-8");
            template.process(dataModel, stringWriter);

            final String cachedPageKey =
                    (String) request.getAttribute(Keys.PAGE_CACHE_KEY);

            LOGGER.log(Level.FINEST, "Caching page[cachedPageKey={0}]",
                       cachedPageKey);

            final JSONObject cachedValue = new JSONObject();
            final String pageContent = stringWriter.toString();
            cachedValue.put(CACHED_CONTENT, pageContent);
            cachedValue.put(CACHED_TYPE, request.getAttribute(CACHED_TYPE));
            cachedValue.put(CACHED_OID, request.getAttribute(CACHED_OID));

            writer.write(pageContent);

            PageCaches.put(cachedPageKey, cachedValue);
            LOGGER.log(Level.FINEST, "Cached page[cachedPageKey={0}]",
                       cachedPageKey);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ActionException(e);
        }
    }
}
