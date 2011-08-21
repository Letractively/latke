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
package org.b3log.latke;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.action.util.PageCaches;
import org.b3log.latke.util.Strings;

/**
 * Latke framework configuration utility facade.
 *
 * <p>
 * If the application runs on {@linkplain  RuntimeEnv#LOCAL local environment},
 * please set {@linkplain #repositoryPath} before setting up your application.
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.4, Aug 8, 2011
 */
public final class Latkes {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(Latkes.class.getName());
    /**
     * Locale. Initializes this by
     * {@link #setLocale(java.util.Locale)}.
     */
    private static Locale locale;
    /**
     * Local repository path.
     */
    private static String repositoryPath;
    /**
     * Where Latke runs on?.
     */
    private static RuntimeEnv runtimeEnv;
    /**
     * Which mode Latke runs in?
     */
    private static RuntimeMode runtimeMode;
    /**
     * Is the page cache enabled?
     */
    private static boolean pageCacheEnabled;

    /**
     * Disables the page cache.
     * 
     * <p>
     * Invokes this method will remove all cached pages and templates.
     * </p>
     */
    public static void disablePageCache() {
        pageCacheEnabled = false;
        PageCaches.removeAll();
        LOGGER.log(Level.FINER, "Disabled page cache");
    }

    /**
     * Enables the page cache.
     */
    public static void enablePageCache() {
        pageCacheEnabled = true;
        LOGGER.log(Level.FINER, "Enabled page cache");
    }

    /**
     * Is the page cache enabled?
     * 
     * @return {@code true} if it is enabled, returns {@code false} otherwise
     */
    public static boolean isPageCacheEnabled() {
        return pageCacheEnabled;
    }

    /**
     * Initializes {@linkplain RuntimeEnv runtime environment}.
     * 
     * <p>
     * If the GAERepository class (org.b3log.latke.repository.gae.GAERepository)
     * is on the classpath, considered Latke is running on 
     * <a href="http://code.google.com/appengine">Google App Engine</a>,
     * otherwise, considered Latke is running on standard Servlet container.
     * </p>
     * 
     * @see RuntimeEnv
     */
    public static void initRuntimeEnv() {
        try {
            Class.forName("org.b3log.latke.repository.gae.GAERepository");
            runtimeEnv = RuntimeEnv.GAE;
            LOGGER.log(Level.INFO, "Latke is running on [GAE]",
                       Latkes.getRuntimeEnv());
        } catch (final ClassNotFoundException e) {
            runtimeEnv = RuntimeEnv.LOCAL;
            LOGGER.log(Level.INFO, "Latke is running on [Local]",
                       Latkes.getRuntimeEnv());
        }

        // TODO: getRepositoryPath();
    }

    /**
     * Gets the runtime environment.
     *
     * @return runtime environment
     */
    public static RuntimeEnv getRuntimeEnv() {
        if (null == Latkes.runtimeEnv) {
            throw new RuntimeException(
                    "Runtime enviornment has not been initialized!");
        }

        return Latkes.runtimeEnv;
    }

    /**
     * Sets the runtime mode with the specified mode.
     *
     * @param runtimeMode the specified mode
     */
    public static void setRuntimeMode(final RuntimeMode runtimeMode) {
        Latkes.runtimeMode = runtimeMode;
    }

    /**
     * Gets the runtime mode.
     *
     * @return runtime mode
     */
    public static RuntimeMode getRuntimeMode() {
        if (null == Latkes.runtimeMode) {
            throw new RuntimeException(
                    "Runtime mode has not been initialized!");
        }

        return Latkes.runtimeMode;
    }

    /**
     * Sets the repository path with the specified repository path.
     *
     * @param repositoryPath the specified repository path
     */
    public static void setRepositoryPath(final String repositoryPath) {
        Latkes.repositoryPath = repositoryPath;
    }

    /**
     * Gets the repository path.
     *
     * @return repository path
     */
    public static String getRepositoryPath() {
        if (Strings.isEmptyOrNull(repositoryPath)) {
            throw new RuntimeException(
                    "Repository path has not been initialized!");
        }

        return repositoryPath;
    }

    /**
     * Sets the locale with the specified locale.
     *
     * @param locale the specified locale
     */
    public static void setLocale(final Locale locale) {
        Latkes.locale = locale;
    }

    /**
     * Gets the locale. If the {@link #locale} has not been
     * initialized, invoking this method will throw {@link RuntimeException}.
     *
     * @return the locale
     */
    public static Locale getLocale() {
        if (null == locale) {
            throw new RuntimeException(
                    "Default locale has not been initialized!");
        }

        return locale;
    }

    /**
     * Private constructor.
     */
    private Latkes() {
    }
}
