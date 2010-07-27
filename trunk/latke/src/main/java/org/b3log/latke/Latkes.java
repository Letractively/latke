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

package org.b3log.latke;

import java.util.Locale;
import org.b3log.latke.util.Strings;

/**
 * Latke framework configuration utility facade. All public setters of this
 * class MUST be invoked before setting up your application.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jul 21, 2010
 */
public final class Latkes {

    /**
     * Default locale. Initializes this by
     * {@link #setDefaultLocale(java.util.Locale)}.
     */
    private static Locale defaultLocale;
    /**
     * Repository path.
     */
    private static String repositoryPath;

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
