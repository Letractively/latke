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
import org.b3log.latke.util.Strings;

/**
 * Latke framework configuration utility facade.
 *
 * <p>
 * If the application runs on {@linkplain  RunsOnEnv#LOCAL local environment},
 * please set {@linkplain #repositoryPath} before setting up your application.
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Jun 18, 2011
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
     * Where latke runs on?.
     */
    private static RunsOnEnv runsOnEnv;
    /**
     * Is the page cache enabled?
     */
    private static boolean pageCacheEnabled;

    /**
     * Disables the page cache.
     */
    public static void disablePageCache() {
        pageCacheEnabled = false;
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
        final RunsOnEnv runsOn = getRunsOnEnv();
        if (runsOn.equals(RunsOnEnv.LOCAL)) {
            getRepositoryPath();
        }
    }

    /**
     * Sets the runs on with the specified environment.
     *
     * @param runsOnEnv the specified environment
     */
    public static void setRunsOnEnv(final RunsOnEnv runsOnEnv) {
        Latkes.runsOnEnv = runsOnEnv;
    }

    /**
     * Gets the runs on environment.
     *
     * @return runs on environment
     */
    public static RunsOnEnv getRunsOnEnv() {
        if (null == Latkes.runsOnEnv) {
            throw new RuntimeException(
                    "Runs on enviornment has not been initialized!");
        }

        return Latkes.runsOnEnv;
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
