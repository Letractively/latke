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

package org.b3log.latke.client.remote.impl;

import org.b3log.latke.Keys;
import org.b3log.latke.client.action.ActionException;
import org.b3log.latke.client.remote.AbstractRemoteService;
import org.b3log.latke.service.ServiceException;
import com.google.inject.Inject;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.util.Locales;
import org.apache.log4j.Logger;
import org.jabsorb.JSONRPCBridge;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Language service for JavaScript client.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.6, Jun 16, 2010
 */
public final class LanguageService extends AbstractRemoteService {

    /**
     * Default serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LanguageService.class);
    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Public constructor with parameter. Invokes constructor of superclass.
     *
     * @param jsonRpcBridge the specified json rpc bridge.
     */
    @Inject
    public LanguageService(final JSONRPCBridge jsonRpcBridge) {
        super(jsonRpcBridge);
    }

    /**
     * Gets locale of the specified request.
     *
     * @param request the specified
     * @return for example:
     * <pre>
     * {
     *      "locale": "zh_CN"
     * }
     * </pre>
     * @throws ActionException action exception
     */
    public JSONObject getLocale(final HttpServletRequest request)
            throws ActionException {
        final Locale locale = Locales.getLocale(request);
        final JSONObject ret = new JSONObject();

        try {
            ret.put(Keys.LOCALE, locale);
        } catch (final JSONException e) {
            LOGGER.error(e.getMessage(), e);
            
            throw new ActionException(e);
        }

        return ret;
    }
    /**
     * Gets all labels for multi-languages by locale of the specified request
     * json object.
     *
     * @param requestJSONObject the specified request json object, for example:
     * <pre>
     * {
     *     "locale": "zh_CN"
     * }
     * </pre>
     * @param request the specified http servlet request
     * @return for example:
     * <pre>
     * {
     *     "htmlTitle": "Co-soft",
     *     "labels": [
     *         {
     *             "labelId": "companyAddress",
     *             "labelText": "公司地址"
     *         }, ....
     *     ],
     *     "sc": "CHANGE_LOCALE_SUCC"
     * }
     * </pre>
     * @throws ActionException action exception
     */
    public JSONObject getLabels(final JSONObject requestJSONObject,
                                   final HttpServletRequest request)
            throws ActionException {
        try {
            final String localeString = requestJSONObject.getString(Keys.LOCALE);
            final Locale locale = new Locale(
                    Locales.getLanguage(localeString),
                    Locales.getCountry(localeString));
            
            Locales.setLocale(request, locale);

            final JSONObject ret = langPropsService.getLabels(locale);
            LOGGER.debug("Got all labels[locale=" + locale + ", sessionId="
                    + request.getSession().getId() + "]");

            return ret;
        } catch (final ServiceException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ActionException(e);
        } catch (final JSONException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ActionException(e);
        }
    }
}
