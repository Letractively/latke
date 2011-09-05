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
package org.b3log.latke.servlet;

import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.action.ActionException;
import org.b3log.latke.action.util.PageCaches;
import org.json.JSONObject;
import freemarker.template.Template;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.util.freemarker.Templates;
import static org.b3log.latke.action.AbstractCacheablePageAction.*;

/**
 * <a href="http://freemarker.org">FreeMarker</a> HTTP response renderer.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Sep 5, 2011
 */
// TODO: i18n handling
public final class FreeMarkerResponseRenderer extends AbstractHTTPResponseRenderer {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(FreeMarkerResponseRenderer.class.getName());
    /**
     * Template name.
     */
    private String templateName;
    /**
     * Data model.
     */
    private Map<String, Object> dataModel = new HashMap<String, Object>();

    @Override
    public void render(final HTTPRequestContext context) {
        final HttpServletResponse response = context.getResponse();
        try {
            final PrintWriter writer = response.getWriter();
            // TODO: caching
            final Template template =
                    Templates.CONFIGURATION.getTemplate(templateName);

            render(context.getRequest(), response, dataModel, template);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "FreeMarker renders error", e);

            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, "Can not sned error 500!", ex);
            }
        }
    }

    /**
     * Processes the specified FreeMarker template with the specified request, 
     * data model and response. 
     * 
     * <p>
     * Puts the page response contents into cache with the key getting from 
     * request attribute specified by <i>page cache key</i>.
     * </p>
     * 
     * <p>
     *   <b>Note</b>: This method will write page content to the writer of the
     *   specified response without flush/close it.
     * </p>
     *
     * @param request the specified request
     * @param response the specified response
     * @param dataModel the specified data model
     * @param template the specified FreeMarker template
     * @throws ActionException action exception
     * @see #beforeDoFreeMarkerAction(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse) 
     */
    @SuppressWarnings("unchecked")
    private void render(final HttpServletRequest request,
                        final HttpServletResponse response,
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
            final StringBuilder pageContentBuilder =
                    new StringBuilder(stringWriter.toString());

            final long endimeMillis = System.currentTimeMillis();
            final String dateString = DateFormatUtils.format(
                    endimeMillis, "yyyy/MM/dd HH:mm:ss");
            final long startTimeMillis =
                    (Long) request.getAttribute(START_TIME_MILLIS);
            final String msg = String.format(
                    "<!-- Generated by B3log Solo(%1$d ms), %2$s -->",
                    endimeMillis - startTimeMillis, dateString);
            pageContentBuilder.append(msg);

            final String pageContent = pageContentBuilder.toString();
            check(request, pageContent);

            cachedValue.put(CACHED_CONTENT, pageContent);
            cachedValue.put(CACHED_TYPE, request.getAttribute(CACHED_TYPE));
            cachedValue.put(CACHED_OID, request.getAttribute(CACHED_OID));
            cachedValue.put(CACHED_TITLE, request.getAttribute(CACHED_TITLE));
            cachedValue.put(CACHED_LINK, request.getAttribute(CACHED_LINK));

            writer.write(pageContent);
            writer.flush();
            writer.close();

            if (Latkes.isPageCacheEnabled()) {
                PageCaches.put(cachedPageKey, cachedValue);
                LOGGER.log(Level.FINEST, "Cached page[cachedPageKey={0}]",
                           cachedPageKey);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ActionException(e);
        }
    }

    /**
     * Gets the data model.
     * 
     * @return data model
     */
    public Map<String, Object> getDataModel() {
        return dataModel;
    }

    /**
     * Gets the template name.
     * 
     * @return template name
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Sets the template name with the specified template name.
     * 
     * @param templateName the specified template name
     */
    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }
}
