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
 * @version 1.0.0.3, Jul 9, 2011
 */
public final class Latkes {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(Latkes.class.getName());
    /**
     * Default locale. Initializes this by
     * {@link #setDefaultLocale(java.util.Locale)}.
     */
    private static Locale defaultLocale;
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
     * Checks environment settings.
     */
    public static void check() {
        final RuntimeEnv env = getRuntimeEnv();
        if (env.equals(RuntimeEnv.LOCAL)) {
            getRepositoryPath();
        }
    }

    /**
     * Sets the runtime environment with the specified environment.
     *
     * @param runtimeEnv the specified environment
     */
    public static void setRuntimeEnv(final RuntimeEnv runtimeEnv) {
        Latkes.runtimeEnv = runtimeEnv;
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
     * Sets the default locale.
     *
     * @param locale a new default locale
     */
    public static void setDefaultLocale(final Locale locale) {
        defaultLocale = locale;
    }

    /**
     * Gets the default locale. If the {@link #defaultLocale} has not been
     * initialized, invoking this method will throw {@link RuntimeException}.
     *
     * @return the default locale
     */
    public static Locale getDefaultLocale() {
        if (null == defaultLocale) {
            throw new RuntimeException(
                    "Default locale has not been initialized!");
        }

        return defaultLocale;
    }

    /**
     * Private constructor.
     */
    private Latkes() {
    }
}
