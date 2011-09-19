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
import org.b3log.latke.Latkes;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.servlet.renderer.DoNothingRenderer;
import org.json.JSONException;
import org.json.JSONObject;
import static org.b3log.latke.action.AbstractCacheablePageAction.*;

/**
 * Front controller for HTTP request dispatching.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.5, Sep 19, 2011
 */
public final class HTTPRequestDispatcher extends HttpServlet {

    /**
     * Default serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(HTTPRequestDispatcher.class.getName());
    /**
     * Event manager.
     */
    private EventManager eventManager = EventManager.getInstance();

    static {
        RequestProcessors.discover();
    }

    /**
     * Serves.
     *
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     */
    @Override
    protected void service(final HttpServletRequest request,
                           final HttpServletResponse response)
            throws ServletException, IOException {
        final long startTimeMillis = System.currentTimeMillis();
        request.setAttribute(START_TIME_MILLIS, startTimeMillis);

        if (Latkes.isPageCacheEnabled()) {
            final String requestURI = request.getRequestURI();
            final String queryString = request.getQueryString();
            String pageCacheKey =
                    (String) request.getAttribute(Keys.PAGE_CACHE_KEY);
            if (Strings.isEmptyOrNull(pageCacheKey)) {
                pageCacheKey = PageCaches.getPageCacheKey(requestURI,
                                                          queryString);
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
     * @param context the specified specified context
     */
    public static void dispatch(final HTTPRequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        String requestURI = (String) request.getAttribute("requestURI");
        if (Strings.isEmptyOrNull(requestURI)) {
            requestURI = request.getRequestURI();
        }

        String method = (String) request.getAttribute("method");
        if (Strings.isEmptyOrNull(method)) {
            method = request.getMethod();
        }

        LOGGER.log(Level.FINER, "Request[requestURI={0}, method={1}]",
                   new Object[]{requestURI, method});

        final Object processorMethodRet =
                RequestProcessors.invoke(requestURI, method, context);
        // XXX: processor method ret?

        AbstractHTTPResponseRenderer renderer = context.getRenderer();
        if (null == renderer) {
            renderer = new DoNothingRenderer();
        }

        renderer.render(context);
    }

    /**
     * Gets the query string(key1=value2&key2=value2&....) for the
     * specified HTTP servlet request.
     *
     * @param request the specified HTTP servlet request
     * @return a json object converts from query string, if can't convert the
     * query string, returns an empty json object;
     * @throws JSONException json exception
     */
    private JSONObject getQueryStringJSONObject(
            final HttpServletRequest request) throws JSONException {
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
