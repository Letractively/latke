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
import java.io.BufferedReader;
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
 * @version 1.0.3.6, Jul 19, 2011
 * @see #doFreeMarkerAction(freemarker.template.Template,
 *                        HttpServletRequest, HttpServletResponse)
 * @see #doAjaxAction(org.json.JSONObject,
 *                     HttpServletRequest, HttpServletResponse)
 * @see Templates
 */
// TODO: request mapping processing (Process HTTP GET/POST/etc should be configurable 
//       rather than HTTP GET to FreeMarker and HTTP POST to AJAX at present). Refers 
//       spring mvc request dispathing and servlet bean initailization.
// TODO: include request processing
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
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
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
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @return a JSONObject for responsing
     * @throws ActionException action exception
     */
    protected abstract JSONObject doAjaxAction(
            final JSONObject requestJSONObject,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws ActionException;

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
     * Process the specified HTTP servlet request and the specified HTTP servlet 
     * response for {@literal HTTP POST} method.
     *
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
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
     * Gets the name of <a href="http://www.freemarker.org">FreeMarker</a>
     * template with the specified request URI.
     * 
     * <p>
     * This method will get template name from the specified request URI with 
     * the following steps: <br/>
     * Gets a substring from the last "/" of the specified request URI, if the 
     * obtained substring:
     *   <ul>
     *     <li>is empty or null, returns "index.ftl"</li>
     *     <li>else if it ends with ".do", replace ".do" with ".ftl" then return it</li>
     *     <li>else appends ".ftl" on it then return it</li>
     *   </ul>
     * </p>
     *
     * @param requestURI the specified request URI
     * @return the name of FreeMarker template corresponding to request URI,
     * returns {@literal index.ftl} if not exists such a FreeMarker template
     */
    protected String getTemplateName(final String requestURI) {
        int idx = requestURI.lastIndexOf("/");

        String ret = requestURI.substring(idx + 1, requestURI.length());
        if (Strings.isEmptyOrNull(ret)) { //   -> "index.ftl"
            ret = "index.ftl";
        } else if (ret.endsWith(".do")) { // "xxx.do" -> "xxx.ftl"
            idx = ret.lastIndexOf(".do");
            ret = ret.substring(0, idx);
            ret += ".ftl";
        } else { // "xxx" -> "xxx.ftl"
            ret += ".ftl";
        }

        LOGGER.log(Level.FINER, "Request[templateName={0}]", ret);

        return ret;
    }

    /**
     * Processes a FreeMarker request for the specified request and response.
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
        LOGGER.log(Level.FINER, "Action[className={0}, requestURI={1}]",
                   new Object[]{getClass().getName(), request.getRequestURI()});

        try {
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
            LOGGER.log(Level.WARNING, "Process FreeMarker request failed", e);

            return;
        }
    }

    /**
     * Fires FreeMarker action event with the host template name and data model.
     * 
     * @param hostTemplateName the specified host template name
     * @param dataModel the specified data model
     */
    protected void fireFreeMarkerActionEvent(final String hostTemplateName,
                                             final Map<String, Object> dataModel) {
        try {
            final ViewLoadEventData data = new ViewLoadEventData();
            data.setViewName(hostTemplateName);
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
    }

    /**
     * Processes an ajax request for the specified request and response.
     *
     * @param request the specified request
     * @param response the specified response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     */
    private void processAjaxRequest(final HttpServletRequest request,
                                    final HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.log(Level.FINER, "Action[className={0}, requestURI={1}]",
                   new Object[]{getClass().getName(), request.getRequestURI()});

        JSONObject result = null;
        try {
            final JSONObject requestJSONObject = beforeDoAjaxAction(request,
                                                                    response);
            LOGGER.log(Level.FINER, "Request json object[{0}]",
                       requestJSONObject);
            result = doAjaxAction(requestJSONObject, request, response);
            afterDoAjaxAction(request, response, result);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e);
        }
    }

    /**
     * Before do FreeMarker action.
     *
     * @param request the specified request
     * @param response the specified response
     * @throws ActionException action exception
     * @throws IOException io exception
     */
    protected void beforeDoFreeMarkerAction(
            final HttpServletRequest request, final HttpServletResponse response)
            throws ActionException, IOException {
        LOGGER.log(Level.FINER, "Processing before do FreeMarker action");
    }

    /**
     * Gets a template with the specified HTTP servlet request.
     * 
     * @param request the specified HTTP servlet request
     * @return template, returns {@code null} if not found
     */
    protected Template getTemplate(final HttpServletRequest request) {
        final String templateName = getTemplateName(request.getRequestURI());

        try {
            return Templates.getTemplate(templateName);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Can't find template by the specified request[URI="
                                     + request.getRequestURI() + "]",
                       e.getMessage());
            return null;
        }
    }

    /**
     * Processes the specified FreeMarker template with the specified request, 
     * data model and response.
     *
     * <p>
     *   <b>Note</b>: If the specified response has been committed, flush response
     *   writer and return.
     * </p>
     *
     * @param request the specified request
     * @param response the specified response
     * @param dataModel the specified data model
     * @param template the specified  FreeMarker template
     * @throws ActionException action exception
     */
    protected void afterDoFreeMarkerTemplateAction(
            final HttpServletRequest request, final HttpServletResponse response,
            final Map<?, ?> dataModel, final Template template)
            throws ActionException {
        try {
//            if (response.isCommitted()) { // response has been sent redirect
//                return;
//            }
            final PrintWriter writer = response.getWriter();
            template.process(dataModel, writer);
        } catch (final Exception e) {
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
     */
    private JSONObject beforeDoAjaxAction(final HttpServletRequest request,
                                          final HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        final Map<?, ?> parameterMap = request.getParameterMap();

        try {
            // Parses with parameter map
            for (Map.Entry<?, ?> entry : parameterMap.entrySet()) {
                LOGGER.log(Level.FINER,
                           "AJAX request paramter[key={0}, value={1}]",
                           new Object[]{entry.getKey(), entry.getValue()});
                // XXX: "(GAE/J 1.5.0 and above) Why the ajax request hold arguments in key????
                return new JSONObject(entry.getKey().toString());
            }

            return new JSONObject();
        } catch (final JSONException e) {
            // Parses with request reader (GAE/J 1.4.3 and below)
            final StringBuilder sb = new StringBuilder();
            BufferedReader reader = null;

            final String errMsg = "Can not parse request[requestURI=" + request.
                    getRequestURI() + ", method=" + request.getMethod()
                                  + "], returns an empty json object";
            try {
                try {
                    reader = request.getReader();
                } catch (final IllegalStateException illegalStateException) {
                    reader = new BufferedReader(new InputStreamReader(
                            request.getInputStream()));
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

                return new JSONObject(tmp);
            } catch (final Exception ex) {
                LOGGER.log(Level.SEVERE, errMsg, ex);

                return new JSONObject();
            }
        }
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
