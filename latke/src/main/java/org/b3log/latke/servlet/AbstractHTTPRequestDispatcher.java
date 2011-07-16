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

import java.util.logging.Level;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.event.EventManager;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Abstract front controller for HTTP request dispatching.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Jul 16, 2011
 */
public abstract class AbstractHTTPRequestDispatcher extends HttpServlet {

    /**
     * Default serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractHTTPRequestDispatcher.class.getName());
    /**
     * Event manager.
     */
    private EventManager eventManager = EventManager.getInstance();

    /**
     * Sets the character encoding of the specified HTTP servlet request and the
     * specified HTTP servlet response to "UTF-8", sets the content type of the
     * specified HTTP servlet response to "text/html".
     *
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @throws UnsupportedEncodingException if can not set the character
     * encoding of the specified HTTP servlet request
     */
    protected void init(final HttpServletRequest request,
                        final HttpServletResponse response)
            throws UnsupportedEncodingException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        request.setCharacterEncoding("UTF-8");
    }

    /**
     * Using <a href="http://www.freemarker.org">FreeMarker</a> to process the
     * specified HTTP servlet request and the specified HTTP servlet response
     * for {@literal HTTP GET} method.
     *
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     * @see #processFreemarkRequest(javax.servlet.http.HttpServletRequest,
     *                              javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(final HttpServletRequest request,
                           final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            init(request, response);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e);
        }

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
    protected abstract void dispatch(final HTTPRequestContext context);

    /**
     * Gets the query string(key1=value2&key2=value2&....) for the
     * specified HTTP servlet request.
     *
     * @param request the specified HTTP servlet request
     * @return a json object converts from query string, if can't convert the
     * query string, returns an empty json object;
     * @throws JSONException json exception
     */
    protected final JSONObject getQueryStringJSONObject(
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

    /**
     * Matches.
     * 
     * @param requestKey request key
     * @param method method
     * @param requestURI request URI
     * @return {@code true} if matches, returns {@code false} otherwise
     */
    // TODO: match strategy
    protected boolean match(final RequestKey requestKey,
                            final String method,
                            final String requestURI) {
        final String[] acceptedMethods = method.split(",");
        boolean methodAccepted = false;
        for (int i = 0; i < acceptedMethods.length; i++) {
            final String acceptedMethod = acceptedMethods[i];
            if (requestKey.getMethod().equals(acceptedMethod)) {
                methodAccepted = true;
                break;
            }
        }

        boolean uriAccepted = false;
        final String[] acceptedURIs = requestURI.split(",");
        for (int i = 0; i < acceptedURIs.length; i++) {
            final String acceptedURI = acceptedURIs[i];
            if (requestKey.getRequestURI().equals(acceptedURI)) {
                uriAccepted = true;
                break;
            }
        }

        return methodAccepted && uriAccepted;
    }
}
