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

import java.util.logging.Level;
import org.b3log.latke.util.Strings;
import org.b3log.latke.util.freemarker.Templates;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.model.Plugin;
import org.b3log.latke.plugin.ViewLoadEventData;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Abstract action.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.3.3, Jun 11, 2011
 * @see #doFreeMarkerAction(freemarker.template.Template,
 *                        HttpServletRequest, HttpServletResponse)
 * @see #doAjaxAction(org.json.JSONObject,
 *                     HttpServletRequest, HttpServletResponse)
 * @see Templates
 */
public abstract class AbstractAction extends HttpServlet {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractAction.class.getName());
    /**
     * Event manager.
     */
    private EventManager eventManager = EventManager.getInstance();
    /**
     * Indicates the event of invoked {@link #doFreeMarkerAction(freemarker.template.Template, 
     * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    public static final String FREEMARKER_ACTION = "FreeMarkerAction";

    /**
     * Performs the FreeMarker template action.
     * 
     * @param template request FreeMarker template
     * @param request the specified http servlet request
     * @param response the specified http servlet response
     * @return data model for FreeMarker template
     * @throws ActionException action exception
     */
    protected abstract Map<?, ?> doFreeMarkerAction(
            final Template template,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws ActionException;

    /**
     * Performs the ajax action.
     *
     * @param requestJSONObject request data
     * @param request the specified http servlet request
     * @param response the specified http servlet response
     * @return a JSONObject for responsing
     * @throws ActionException action exception
     */
    protected abstract JSONObject doAjaxAction(
            final JSONObject requestJSONObject,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws ActionException;

    /**
     * Sets the character encoding of the specified http servlet request and the
     * specified http servlet response to "UTF-8", sets the content type of the
     * specified http servlet response to "text/html".
     *
     * @param request the specified http servlet request
     * @param response the specified http servlet response
     * @throws UnsupportedEncodingException if can not set the character
     * encoding of the specified http servlet request
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
     * specified http servlet request and the specified http servlet response
     * for {@literal HTTP GET} method.
     *
     * @param request the specified http servlet request
     * @param response the specified http servlet response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     * @see #processFreemarkRequest(javax.servlet.http.HttpServletRequest,
     *                              javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            init(request, response);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e);
        }

        processFreemarkRequest(request, response);
    }

    /**
     * Process the specified http servlet request and the specified http servlet 
     * response for {@literal HTTP POST} method.
     *
     * @param request the specified http servlet request
     * @param response the specified http servlet response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     * @see #processAjaxRequest(javax.servlet.http.HttpServletRequest,
     *                              javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            init(request, response);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e);
        }

        processAjaxRequest(request, response);
    }

    /**
     * Converts the specified json string to a {@link JSONObject}.
     *
     * @param jsonString the specified json string
     * @return a json object
     * @throws JSONException json exception
     * @see JSONObject#JSONObject(java.lang.String) 
     */
    private JSONObject toJSONObject(final String jsonString)
            throws JSONException {
        return new JSONObject(jsonString);
    }

    /**
     * Converts the specified http servlet request to a json string.
     *
     * @param request the specified http servlet request
     * @return a json string if the specified http servlet request could convert,
     *         otherwise, returns "{}"
     * @throws IOException io exception
     * @throws JSONException json exception
     */
    private String toJSONString(final HttpServletRequest request)
            throws IOException, JSONException {
        final StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = request.getReader();
        } catch (final IllegalStateException e) {
            reader = new BufferedReader(new InputStreamReader(
                    request.getInputStream()));
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);

            return "{}";
        }

        String line = reader.readLine();
        while (null != line) {
            sb.append(line);
            line = reader.readLine();
        }
        reader.close();

        String tmp = sb.toString();
        if (Strings.isEmptyOrNull(tmp)) {
            tmp = "{}";
        }

        return tmp;
    }

    /**
     * Gets the query string(key1=value2&key2=value2&....) for the
     * specified http servlet request.
     *
     * @param request the specified http servlet request
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
     * Gets the name of <a href="http://www.freemarker.org">FreeMarker</a>
     * template with the specified request URI.
     *
     * @param requestURI the specified request URI
     * @return the name of FreeMarker template corresponding to request URI,
     * returns {@literal index.do} if not exists such a FreeMarker template
     */
    protected String getPageName(final String requestURI) {
        int idx = requestURI.lastIndexOf("/");

        String ret = requestURI.substring(idx + 1, requestURI.length());
        if (Strings.isEmptyOrNull(ret)) {
            ret = "index.ftl";
        } else if (ret.endsWith(".do")) {
            idx = ret.lastIndexOf(".do");
            ret = ret.substring(0, idx);
            ret += ".ftl";
        } else {
            ret += ".ftl";
        }

        LOGGER.log(Level.FINER, "Request[pageName={0}]", ret);

        return ret;
    }

    /**
     * Processes FreeMarker request for the specified request and response.
     *
     * @param request the specified request
     * @param response the specified response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     */
    @SuppressWarnings("unchecked")
    protected void processFreemarkRequest(final HttpServletRequest request,
                                          final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            LOGGER.log(Level.FINE, "Action[{0}]", getClass());
            final Template template =
                    beforeDoFreeMarkerAction(request, response);
            if (null == template) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);

                return;
            }

            final Map<String, Object> dataModel =
                    (Map<String, Object>) doFreeMarkerAction(template,
                                                             request, response);
            try {
                final ViewLoadEventData data = new ViewLoadEventData();
                data.setViewName(template.getName());
                data.setDataModel(dataModel);
                eventManager.fireEventSynchronously(
                        new Event<ViewLoadEventData>(FREEMARKER_ACTION, data));
                if (Strings.isEmptyOrNull((String) dataModel.get(Plugin.PLUGINS))) {
                    // There is no plugin for this template, fill ${plugins} with blank.
                    dataModel.put(Plugin.PLUGINS, "");
                }
            } catch (final EventException e) {
                LOGGER.log(Level.WARNING,
                           "Event[FREEMARKER_ACTION] handle failed, ignores this exception for kernel health",
                           e);
            }

            afterDoFreeMarkerTemplateAction(request, response, dataModel,
                                            template);
        } catch (final ActionException e) {
            LOGGER.warning(e.getMessage());

            return;
        }
    }

    /**
     * Processes ajax request for the specified request and response.
     *
     * @param request the specified request
     * @param response the specified response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     */
    private void processAjaxRequest(final HttpServletRequest request,
                                    final HttpServletResponse response)
            throws ServletException, IOException {
        JSONObject result = null;
        try {
            final JSONObject data = beforeDoAjaxAction(request, response);
            result = doAjaxAction(data, request, response);
            afterDoAjaxAction(request, response, result);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e);
        } catch (final JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e);
        } catch (final ActionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e);
        }
    }

    /**
     * Gets FreeMarker template with the specified request and response.
     *
     * @param request the specified request
     * @param response the specified response
     * @return a FreeMarker template, returns {@code null} if not found
     * @throws ActionException action exception
     * @throws IOException io exception
     */
    protected Template beforeDoFreeMarkerAction(
            final HttpServletRequest request, final HttpServletResponse response)
            throws ActionException, IOException {
        final String pageName = getPageName(request.getRequestURI());

        try {
            LOGGER.log(Level.FINER,
                       "Template request[pageName={0}]", pageName);

            return Templates.getTemplate(pageName);
        } catch (final FileNotFoundException e) {
            LOGGER.warning(e.getMessage());
            return null;
        }
    }

    /**
     * Processes FreeMarker template with the specified request, data model,
     * template and response.
     *
     * <p>
     *   <b>Note</b>: If the specified response has been committed, flush response
     *   writer and return.
     * </p>
     *
     * @param request the specified request
     * @param response the specified response
     * @param dataModel the specified data model
     * @param template the specified template
     * @throws ActionException action exception
     */
    protected void afterDoFreeMarkerTemplateAction(
            final HttpServletRequest request, final HttpServletResponse response,
            final Map<?, ?> dataModel, final Template template)
            throws ActionException {
        try {
            if (response.isCommitted()) { // response has been sent redirect
                return;
            }

            final PrintWriter writer = response.getWriter();
            template.process(dataModel, writer);
        } catch (final TemplateException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ActionException(e);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ActionException(e);
        }
    }

    /**
     * Gets the request json object with the specified request.
     *
     * @param request the specified request
     * @param response response
     * @return a json object
     * @throws ServletException servlet exception
     * @throws IOException io exception
     * @throws JSONException json exception
     */
    private JSONObject beforeDoAjaxAction(final HttpServletRequest request,
                                          final HttpServletResponse response)
            throws ServletException, IOException, JSONException {
        response.setContentType("application/json");

        final String requestJSONString = toJSONString(request);
        LOGGER.log(Level.FINER, "AJAX request[string={0}]", requestJSONString);

        return toJSONObject(requestJSONString);
    }

    /**
     * Writes the specified response json object to response.
     *
     * @param request the specified request
     * @param response response
     * @param responseJSONObject the specified response json object
     * @throws ServletException servlet exception
     * @throws IOException io exception
     * @throws JSONException json exception
     */
    private void afterDoAjaxAction(final HttpServletRequest request,
                                   final HttpServletResponse response,
                                   final JSONObject responseJSONObject)
            throws ServletException, IOException, JSONException {
        if (response.isCommitted()) { // response has been sent redirect
            return;
        }

        final PrintWriter writer = response.getWriter();

        try {
            writer.println(responseJSONObject);
        } finally {
            writer.close();
        }
    }
}
