/*
 * Copyright 2009, 2010, B3log Team
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
package org.b3log.latke.service;

import org.b3log.latke.FwkStatusCodes;
import org.b3log.latke.Keys;
import org.b3log.latke.model.AbstractLang;
import org.b3log.latke.model.AbstractMessage;
import org.b3log.latke.model.Label;
import org.b3log.latke.util.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import org.b3log.latke.util.cache.qualifier.LruMemory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Language service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.4, Jul 8, 2010
 */
public final class LangPropsService implements Serializable {

    /**
     * Default serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(LangPropsService.class);
    /**
     * Injector.
     */
    @Inject
    private transient Injector injector;

    /**
     * Gets all language properties as a map by the specified locale.
     *
     * @param locale the specified locale
     * @return a map of language configurations
     * @throws ServiceException service exception
     */
    public Map<String, String> getAll(final Locale locale) throws
            ServiceException {
        @SuppressWarnings("unchecked")
        final Cache<String, HashMap<String, String>> cache =
                (Cache<String, HashMap<String, String>>) injector.getInstance(Key.
                get(new TypeLiteral<Cache<String, ?>>() {
        }, LruMemory.class));

        HashMap<String, String> ret = cache.get(Keys.LOCALE);
        if (null != ret) {
            LOGGER.trace("Got language configurations from cache");

            return ret;
        }

        ret = new HashMap<String, String>();
        ResourceBundle langBundle = null;
        try {
            langBundle = ResourceBundle.getBundle(Keys.LANGUAGE, locale);
        } catch (final MissingResourceException e) {
            LOGGER.warn(e.getMessage() + ", using default locale["
                    + Keys.getDefaultLocale() + "]  instead");

            langBundle = ResourceBundle.getBundle(Keys.LANGUAGE,
                                                  Keys.getDefaultLocale());
        }

        final Enumeration<String> keys = langBundle.getKeys();
        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();
            final String value = langBundle.getString(key);
            ret.put(key, value);
        }

        cache.put(Keys.LOCALE, ret);

        return ret;
    }

    /**
     * Gets all language properties as labels from lang_(by the specified
     * locale).properties file. If not found lang_(locale).properties
     * configurations, using {@link Keys#getDefaultLocale()} instead.
     *
     * @param locale the specified locale
     * @return for example:
     * <pre>
     * {
     *     "sc": "CHANGE_LOCALE_FAIL_NOT_FOUND",
     *     "msgs": {
     *         "localeNotFound":
     *             "Unsupported locale, using default locale(zh_CN) instead."
     *     },
     *     "htmlTitle": "B3log",
     *     "labels": [
     *         {"labelId": "projectName", "labelText": "B3log Web"},
     *         ....
     *     ]
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public JSONObject getLabels(final Locale locale) throws ServiceException {
        final JSONObject ret = new JSONObject();
        ResourceBundle langBundle = null;
        ResourceBundle messageBundle = null;

        try {
            messageBundle = ResourceBundle.getBundle(Keys.MESSAGES, locale);
        } catch (final MissingResourceException e) {
            messageBundle = ResourceBundle.getBundle(Keys.MESSAGES,
                                                     Keys.getDefaultLocale());
        }

        try {
            langBundle = ResourceBundle.getBundle(Keys.LANGUAGE, locale);
        } catch (final MissingResourceException e) {
            LOGGER.warn(e.getMessage() + ", using default locale["
                    + Keys.getDefaultLocale() + "]  instead");

            langBundle = ResourceBundle.getBundle(Keys.LANGUAGE,
                                                  Keys.getDefaultLocale());
            try {
                ret.put(Keys.STATUS_CODE,
                        FwkStatusCodes.CHANGE_LOCALE_FAIL_NOT_FOUND);

                final JSONObject localeNotFound = new JSONObject();
                localeNotFound.put(AbstractMessage.LOCALE_NOT_FOUND,
                                   messageBundle.getString(
                        AbstractMessage.LOCALE_NOT_FOUND));
                ret.put(Keys.MESSAGES, localeNotFound);
            } catch (final JSONException ex) {
                LOGGER.error(ex.getMessage(), e);
            }
        }

        final Enumeration<String> keys = langBundle.getKeys();
        final JSONArray labels = new JSONArray();

        try {
            ret.put(Label.LABELS, labels);

            while (keys.hasMoreElements()) {
                final JSONObject label = new JSONObject();
                final String key = keys.nextElement();
                label.put(Label.LABEL_ID, key);
                label.put(Label.LABEL_TEXT, langBundle.getString(key));

                labels.put(label);
            }

            ret.put(Label.HTML_TITLE,
                    langBundle.getString(AbstractLang.HTML_TITLE));

            ret.put(Keys.STATUS_CODE, FwkStatusCodes.CHANGE_LOCALE_SUCC);
        } catch (final JSONException e) {
            LOGGER.error(e.getMessage(), e);

            throw new ServiceException(e);
        }

        return ret;
    }

    /**
     * Gets a value from baseName_locale.properties file with the specified key.
     * If not found baseName_(locale).properties configurations, using
     * {@link Keys#getDefaultLocale()} instead.
     *
     * @param baseName base name of resource bundle, options as the following:
     * <ul>
     *   <li>{@link Keys#LANGUAGE}</li>
     *   <li>{@link Keys#MESSAGES}</li>
     * </ul>
     * @param key the specified key
     * @param locale the specified locale
     * @return the value of the specified key
     */
    public String get(final String baseName, final String key,
                      final Locale locale) {
        if (!Keys.LANGUAGE.equals(baseName) && !Keys.MESSAGES.equals(baseName)) {
            final RuntimeException e =
                    new RuntimeException("i18n resource[baseName="
                    + baseName + "] not found");
            LOGGER.error(e.getMessage(), e);

            throw e;
        }

        try {
            return ResourceBundle.getBundle(baseName, locale).
                    getString(key);
        } catch (final MissingResourceException e) {
            LOGGER.warn(e.getMessage() + ", get it from default locale["
                    + Keys.getDefaultLocale() + "]");

            return ResourceBundle.getBundle(
                    baseName, Keys.getDefaultLocale()).getString(key);
        }
    }
}
