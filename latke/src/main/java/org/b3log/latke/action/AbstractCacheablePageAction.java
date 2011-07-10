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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.action.util.PageCaches;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.util.Strings;
import org.json.JSONObject;

/**
 * Abstract cacheable page action.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.1.1, Jul 3, 2011
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
     * Key of cached object id.
     */
    public static final String CACHED_OID = "cachedOid";
    /**
     * Key of cached title.
     */
    public static final String CACHED_TITLE = "cachedTitle";
    /**
     * Key of start time millis.
     */
    private static final String START_TIME_MILLIS = "startTimeMillis";

    /**
     * Performs the FreeMarker template action.
     * 
     * <p>
     *   <b>Note</b>: Method implementation MUST set attributes of the 
     *   specified HTTP servlet request with attribute names and values, 
     *   respectively: 
     *   <ul>
     *     <li>"cachedType": ""</li>
     *     <li>"cachedContent": ""</li>
     *     <li>"cachedOid": ""</li>
     *     <li>"cachedTitle": ""</li>
     *   </ul>
     * </p>
     *
     * @param template request FreeMarker template
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @return data model for FreeMarker template
     * @throws ActionException action exception
     */
    @Override
    protected abstract Map<?, ?> doFreeMarkerAction(
            final Template template,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws ActionException;

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Invoking this method will set an attribute into the specified request 
     * withe "pageCacheKey" as name and <i>page cache key</i> as 
     * value.
     * </p>
     * 
     * @see Keys#PAGE_CACHE_KEY
     * @see PageCaches#getPageCacheKey(java.lang.String, java.lang.String) 
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void processFreemarkRequest(final HttpServletRequest request,
                                          final HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.log(Level.FINER, "Action[{0}]", getClass());

        try {
            final long startTimeMillis = System.currentTimeMillis();
            request.setAttribute(START_TIME_MILLIS, startTimeMillis);

            final String requestURI = request.getRequestURI();
            final String queryString = request.getQueryString();
            final String pageCacheKey =
                    PageCaches.getPageCacheKey(requestURI, queryString);

            request.setAttribute(Keys.PAGE_CACHE_KEY, pageCacheKey);

            if (Latkes.isPageCacheEnabled()) {
                if (writeResponseFromCache(request, response, pageCacheKey)) {
                    return;
                }
            }

            final Template template = getTemplate(request);
            if (null == template) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);

                return;
            }

            beforeDoFreeMarkerAction(request, response);
            final Map<String, Object> dataModel =
                    (Map<String, Object>) doFreeMarkerAction(template,
                                                             request, response);

            fireFreeMarkerActionEvent(template.getName(), dataModel);

            afterDoFreeMarkerTemplateAction(request, response, dataModel,
                                            template);
        } catch (final ActionException e) {
            LOGGER.log(Level.WARNING,
                       "Process cacheable FreeMarker request failed", e);

            return;
        }

        // TODO: statistics.incBlogViewCount();
    }

    /**
     * Tries to write the specified HTTP servlet response from cache.
     * 
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @param pageCacheKey the specified page cache key
     * @return {@code true} if wrote, otherwise return {@code false}
     */
    private boolean writeResponseFromCache(final HttpServletRequest request,
                                           final HttpServletResponse response,
                                           final String pageCacheKey) {
        final Cache<String, Object> cache = PageCaches.getCache();
        LOGGER.log(Level.FINER, "Request[pageCacheKey={0}]", pageCacheKey);
        LOGGER.log(Level.FINEST, "Page cache[cachedCount={0}, maxCount={1}]",
                   new Object[]{cache.getCachedCount(), cache.getMaxCount()});
        final JSONObject cachedPageContentObject =
                (JSONObject) cache.get(pageCacheKey);

        if (null == cachedPageContentObject) { // Miss
            LOGGER.log(Level.FINER, "Page cache miss");
            return false;
        }

        // Process page cache hit
        try {
            LOGGER.log(Level.FINEST,
                       "Writes resposne for page[pageCacheKey={0}] from cache",
                       pageCacheKey);
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            final PrintWriter writer = response.getWriter();
            String cachedPageContent =
                    cachedPageContentObject.getString(
                    AbstractCacheablePageAction.CACHED_CONTENT);
            final String cachedType = cachedPageContentObject.optString(
                    AbstractCacheablePageAction.CACHED_TYPE);
            final String cachedTitle = cachedPageContentObject.getString(
                    AbstractCacheablePageAction.CACHED_TITLE);
            LOGGER.log(Level.FINEST,
                       "Cached value[key={0}, type={1}, title={2}]",
                       new Object[]{pageCacheKey, cachedType, cachedTitle});

            processPageCacheHit(cachedPageContentObject);

            final long endimeMillis = System.currentTimeMillis();
            final String dateString = DateFormatUtils.format(
                    endimeMillis, "yyyy/MM/dd HH:mm:ss");
            final long startTimeMillis =
                    (Long) request.getAttribute(START_TIME_MILLIS);
            final String msg = String.format(
                    "<!-- Cached by B3log Solo(%1$d ms), %2$s -->",
                    endimeMillis - startTimeMillis, dateString);
            LOGGER.finer(msg);
            cachedPageContent += "\r\n" + msg;
            writer.write(cachedPageContent);
            writer.flush();
            writer.close();

            return true;
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);

            return false;
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
    @Override
    @SuppressWarnings("unchecked")
    protected void afterDoFreeMarkerTemplateAction(
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
     * Processes page cache hit with the specified cached page content object.
     * 
     * @param cachedPageContentObject the specified cached page content object,
     * for example, 
     * <p>
     * {
     *     "cachedType": "",
     *     "cachedContent": "",
     *     "cachedOid": "",
     *     "cachedTitle": ""
     * }
     * </p>
     */
    protected void processPageCacheHit(final JSONObject cachedPageContentObject) {
        LOGGER.log(Level.FINEST, "Processing page cache hit");
    }

    /**
     * Checks if all conditions for caching page are ready by the specified 
     * request and content.
     * 
     * @param request the specified request
     * @param content the specified content
     */
    private void check(final HttpServletRequest request, final String content) {
        if (Strings.isEmptyOrNull(content)
            || Strings.isEmptyOrNull((String) request.getAttribute(CACHED_TYPE))
            || Strings.isEmptyOrNull((String) request.getAttribute(CACHED_OID))
            || Strings.isEmptyOrNull((String) request.getAttribute(CACHED_TITLE))) {
            throw new IllegalArgumentException("Illegal arguments for caching page, "
                                               + "resolve this bug first!");
        }
    }
}
