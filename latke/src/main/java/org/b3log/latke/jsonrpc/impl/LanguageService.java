/*
 * Copyright (c) 2009, 2010, B3log Team
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

package org.b3log.latke.jsonrpc.impl;

import java.util.logging.Level;
import org.b3log.latke.Keys;
import org.b3log.latke.action.ActionException;
import org.b3log.latke.jsonrpc.AbstractJSONRpcService;
import org.b3log.latke.service.ServiceException;
import com.google.inject.Inject;
import java.util.Locale;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.util.Locales;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Language service for JavaScript client.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.7, Aug 15, 2010
 */
public final class LanguageService extends AbstractJSONRpcService {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(LanguageService.class.getName());
    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

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
            LOGGER.severe(e.getMessage());

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
            LOGGER.log(Level.FINER, "Got all labels[locale={0}, sessionId={1}]",
                       new Object[]{locale, request.getSession().getId()});

            return ret;
        } catch (final ServiceException e) {
            LOGGER.severe(e.getMessage());
            throw new ActionException(e);
        } catch (final JSONException e) {
            LOGGER.severe(e.getMessage());
            throw new ActionException(e);
        }
    }
}
