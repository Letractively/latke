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

package org.b3log.latke.util;

import org.b3log.latke.Keys;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 * Locale utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.6, Jun 15, 2010
 */
public final class Locales {

    /**
     * Generated serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Locales.class);
    /**
     * Language start description in Accept-Language of request header.
     */
    private static final int LANG_START = 0;
    /**
     * Language end description in Accept-Language of request header.
     */
    private static final int LANG_END = 2;
    /**
     * Country start description in Accept-Language of request header.
     */
    private static final int COUNTRY_START = 3;
    /**
     * Country end description in Accept-Language of request header.
     */
    private static final int COUNTRY_END = 5;

    /**
     * Private default constructor.
     */
    private Locales() {
    }

    /**
     * Gets locale with the specified request.
     *
     * By the following steps:
     * <ol>
     *     <li>Gets from session of the specified request</li>
     *     <li>Gets from the specified request header</li>
     *     <li>Using {@link Keys#getDefaultLocale() server configuration}</li>
     * </ol>
     *
     * @param request the specified request
     * @return locale
     */
    public static Locale getLocale(final HttpServletRequest request) {
        // get from session
        final HttpSession session = request.getSession();
        Locale locale = (Locale) session.getAttribute(Keys.LOCALE);
        if (null == locale) {
            // get from request header
            final String languageHeader = request.getHeader("Accept-Language");
            LOGGER.debug("[Accept-Language=" + languageHeader + "]");

            String language = null;
            String country = null;
            if (!Strings.isEmptyOrNull(languageHeader)) {
                language = getLanguage(languageHeader);
                country = getCountry(languageHeader);
            }

            locale = new Locale(language, country);

            if (!hasLocale(locale)) {
                // using default
                locale = Keys.getDefaultLocale();
                LOGGER.debug("Using the default locale["
                        + locale.toString() + "]");
            } else {
                LOGGER.debug("Got locale[" + locale.toString()
                        + "] from request.");
            }
        } else {
            LOGGER.debug("Got locale[" + locale.toString() + "] from session.");
        }

        return locale;
    }

    /**
     * Determines whether the server has the specified locale configuration or
     * not.
     *
     * @param locale the specified locale
     * @return {@code true} if the server has the specified locale,
     * {@code false} otherwise
     */
    public static boolean hasLocale(final Locale locale) {
        try {
            ResourceBundle.getBundle(Keys.LANGUAGE, locale);

            return true;
        } catch (final MissingResourceException e) {
            return false;
        }
    }

    /**
     * Sets the locale.
     *
     * @param request the specified request
     * @param locale a new locale
     */
    public static void setLocale(final HttpServletRequest request,
                                 final Locale locale) {
        final HttpSession session = request.getSession();
        session.setAttribute(Keys.LOCALE, locale);
        LOGGER.debug("Client[sessionId=" + session.getId()
                + "] sets locale to [" + locale.toString() + "]");
    }

    /**
     * Gets country from the specified locale string.
     *
     * @param localeString the specified locale string
     * @return country, if the length of specified locale string less than 5,
     * returns ""
     */
    public static String getCountry(final String localeString) {
        if (localeString.length() >= COUNTRY_END) {
            return localeString.substring(COUNTRY_START, COUNTRY_END);
        }

        return "";
    }

    /**
     * Gets language from the specified locale string.
     *
     * @param localeString the specified locale string
     * @return language, if the length of specified locale string less than 2,
     * returns ""
     */
    public static String getLanguage(final String localeString) {
        if (localeString.length() >= LANG_END) {
            return localeString.substring(LANG_START, LANG_END);
        }

        return "";
    }
}
