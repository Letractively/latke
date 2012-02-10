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
package org.b3log.latke.servlet;

import java.io.File;
import org.b3log.latke.servlet.renderer.AbstractHTTPResponseRenderer;
import java.io.InputStream;
import org.b3log.latke.Keys;
import org.b3log.latke.action.util.PageCaches;
import org.b3log.latke.util.Strings;
import java.util.logging.Level;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.servlet.renderer.HTTP404Renderer;
import org.json.JSONException;
import org.json.JSONObject;
import static org.b3log.latke.action.AbstractCacheablePageAction.*;

/**
 * Front controller for HTTP request dispatching.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.1.2, Dec 22, 2011
 */
public final class HTTPRequestDispatcher extends HttpServlet {

    /**
     * Default serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(HTTPRequestDispatcher.class.getName());
    /**
     * Event manager.
     */
    private EventManager eventManager = EventManager.getInstance();

    static {
        LOGGER.info("Discovering request processors....");
        RequestProcessors.discover();
        LOGGER.info("Discovered request processors");
    }

    /**
     * Serves.
     * 
     * @param request
     *            the specified HTTP servlet request
     * @param response
     *            the specified HTTP servlet response
     * @throws ServletException
     *             servlet exception
     * @throws IOException
     *             io exception
     */
    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String resourcePath = request.getPathTranslated();

        if ((!request.getRequestURI().equals("/") && new File(resourcePath).isDirectory()) || resourcePath.endsWith(".ftl")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/css/") || requestURI.startsWith("/images/") || requestURI.startsWith("/js/")
                || requestURI.startsWith("/skins/") || requestURI.startsWith("/plugins/") || requestURI.endsWith(".png")
                || requestURI.endsWith(".ico") || requestURI.endsWith(".txt") || requestURI.equals("/403.html")) {
            // TODO: 1. Reads these from appengine-web.xml?
            // 2. Etag/Expires/Last-Modified/Cache-Control
            // 3. Content-Encoding, etc headers
            final InputStream staticResourceInputStream = getServletContext().getResourceAsStream(requestURI);
            if (null == staticResourceInputStream) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            final String mimeType = getServletContext().getMimeType(resourcePath);
            response.setContentType(mimeType);
            IOUtils.copy(staticResourceInputStream, response.getOutputStream());

            return;
        }

        final long startTimeMillis = System.currentTimeMillis();
        request.setAttribute(START_TIME_MILLIS, startTimeMillis);

        if (Latkes.isPageCacheEnabled()) {
            final String queryString = request.getQueryString();
            String pageCacheKey = (String) request.getAttribute(Keys.PAGE_CACHE_KEY);
            if (Strings.isEmptyOrNull(pageCacheKey)) {
                pageCacheKey = PageCaches.getPageCacheKey(requestURI, queryString);
                request.setAttribute(Keys.PAGE_CACHE_KEY, pageCacheKey);
            }
        }

        request.setCharacterEncoding("UTF-8");

        response.setCharacterEncoding("UTF-8");

        final HTTPRequestContext context = new HTTPRequestContext();
        context.setRequest(request);
        context.setResponse(response);

        dispatch(context);
    }

    /**
     * Dispatches with the specified context.
     * 
     * @param context
     *            the specified specified context
     * @throws ServletException
     *             servlet exception
     * @throws IOException
     *             io exception
     */
    public static void dispatch(final HTTPRequestContext context) throws ServletException, IOException {
        final HttpServletRequest request = context.getRequest();

        final Integer sc = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (null != sc) {
            request.setAttribute("requestURI", "/error.do");
        }

        String requestURI = (String) request.getAttribute("requestURI");
        if (Strings.isEmptyOrNull(requestURI)) {
            requestURI = request.getRequestURI();
        }

        String method = (String) request.getAttribute("method");
        if (Strings.isEmptyOrNull(method)) {
            method = request.getMethod();
        }

        LOGGER.log(Level.FINER, "Request[requestURI={0}, method={1}]", new Object[] { requestURI, method });

        try {
            final Object processorMethodRet = RequestProcessors.invoke(requestURI, method, context);
        } catch (final Exception e) {
            final String exceptionTypeName = e.getClass().getName();
            LOGGER.log(Level.FINER,
                    "Occured error while processing request[requestURI={0}, method={1}, exceptionTypeName={2}, errorMsg={3}]",
                    new Object[] { requestURI, method, exceptionTypeName, e.getMessage() });
            if ("com.google.apphosting.api.ApiProxy$OverQuotaException".equals(exceptionTypeName)) {
                PageCaches.removeAll();

                context.getResponse().sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }

            throw new ServletException(e);
        } catch (final Error e) {
            final Runtime runtime = Runtime.getRuntime();
            LOGGER.log(Level.FINER, "Memory status[total={0}, max={1}, free={2}]",
                    new Object[] { runtime.totalMemory(), runtime.maxMemory(), runtime.freeMemory() });

            LOGGER.log(Level.SEVERE, e.getMessage(), e);

            throw e;
        }
        // XXX: processor method ret?

        AbstractHTTPResponseRenderer renderer = context.getRenderer();
        if (null == renderer) {
            renderer = new HTTP404Renderer();
        }

        renderer.render(context);
    }

    /**
     * Gets the query string(key1=value2&key2=value2&....) for the specified
     * HTTP servlet request.
     * 
     * @param request
     *            the specified HTTP servlet request
     * @return a json object converts from query string, if can't convert the
     *         query string, returns an empty json object;
     * @throws JSONException
     *             json exception
     */
    private JSONObject getQueryStringJSONObject(final HttpServletRequest request) throws JSONException {
        JSONObject ret = null;
        final String tmp = request.getQueryString();
        if (null == tmp) {
            return new JSONObject();
        }

        LOGGER.log(Level.FINEST, "Client is using QueryString[{0}]", tmp);
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        final String[] split = tmp.split("&");
        for (int i = 0; i < split.length; i++) {
            final String query = split[i];
            final String[] kv = query.split("=");
            if (kv.length != 2) {
                return new JSONObject();
            }

            final String key = kv[0];
            final String value = kv[1];
            sb.append("\"");
            sb.append(key);
            sb.append("\":");
            sb.append("\"");
            sb.append(value);
            sb.append("\"");
            if (i < split.length - 1) {
                sb.append(",");
            }
        }
        sb.append("}");

        ret = new JSONObject(sb.toString());

        return ret;
    }
}
