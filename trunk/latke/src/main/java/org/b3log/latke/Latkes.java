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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;
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
 * @version 1.0.1.0, Dec 29, 2011
 * @see #initRuntimeEnv() 
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
     * Local properties (local.properties).
     */
    private static final Properties LOCAL_PROPS = new Properties();
    /**
     * Static resource version.
     */
    private static String staticResourceVersion;
    /**
     * Latke configurations (latke.properties).
     */
    private static final Properties LATKE_PROPS = new Properties();

    static {
        try {
            final InputStream resourceAsStream =
                    Latkes.class.getResourceAsStream("/local.properties");
            if (null != resourceAsStream) {
                LOCAL_PROPS.load(resourceAsStream);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.CONFIG, "Not found local configuration file");
            // Ignores....
        }

        try {
            final InputStream resourceAsStream =
                    Latkes.class.getResourceAsStream("/latke.properties");
            if (null != resourceAsStream) {
                LATKE_PROPS.load(resourceAsStream);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.CONFIG, "Not found Latke configuration file");
        }
    }

    /**
     * Gets static resource (JS, CSS files) version.
     * 
     * <p>
     * For different {@link #getRuntimeEnv() runtime environment}s: 
     *   <ul>
     *     <li>{@link RuntimeEnv#GAE GAE}</li>
     *     Returns GAE system property 
     *     <a href="https://code.google.com/appengine/docs/java/javadoc/
     *com/google/appengine/api/utils/SystemProperty.html#applicationVersion">
     *     application version</a>.
     *     <li>{@link RuntimeEnv#LOCAL LOCAL}</li>
     *     Returns the value of "staticResourceVersion" property in 
     *     {@link #getLocalProps() local configurations}.
     *   </ul>
     * </p>
     * 
     * @return static resource version
     */
    public static String getStaticResourceVersion() {
        if (null == staticResourceVersion) {

            switch (Latkes.getRuntimeEnv()) {
                case GAE:
                    staticResourceVersion =
                            System.getProperty(
                            "com.google.appengine.application.version");
                    break;
                case LOCAL:
                    staticResourceVersion =
                            LOCAL_PROPS.getProperty("staticResourceVersion");
                    break;
                default:
                    throw new RuntimeException(
                            "Runtime enviornment has not been initialized!");
            }
        }

        return staticResourceVersion;
    }

    /**
     * Gets Latke configurations.
     * 
     * @return Latke properties
     */
    public static Properties getProperties() {
        return LATKE_PROPS;
    }

    /**
     * Gets local (standard Servlet container) configurations.
     * 
     * @return local properties
     */
    public static Properties getLocalProps() {
        return LOCAL_PROPS;
    }

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
     * <p>
     * If the Latke runs on the standard Servlet container (local environment),
     * Latke will read database configurations from file "local.properties".
     * </p>
     * 
     * <p>
     * Sets the current {@link RuntimeMode runtime mode} to 
     * {@link RuntimeMode#DEVELOPMENT development mode}.
     * </p>
     * 
     * @see RuntimeEnv
     */
    public static void initRuntimeEnv() {
        setRuntimeMode(RuntimeMode.DEVELOPMENT); // Defaults to dev mode

        try {
            runtimeEnv = RuntimeEnv.GAE;
            Class.forName("org.b3log.latke.repository.gae.GAERepository");
            LOGGER.log(Level.INFO, "Latke is running on [GAE]",
                       Latkes.getRuntimeEnv());
        } catch (final ClassNotFoundException e) {
            runtimeEnv = RuntimeEnv.LOCAL;
            LOGGER.log(Level.INFO, "Latke is running on [Local]",
                       Latkes.getRuntimeEnv());
        }

        if (RuntimeEnv.LOCAL == runtimeEnv) {
            // Read local database configurations
            final RuntimeDatabase runtimeDatabase = getRuntimeDatabase();
            LOGGER.log(Level.INFO, "Runtime database is [{0}]",
                       runtimeDatabase);
        }
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
     * Gets the runtime database.
     * 
     * @return runtime database
     */
    public static RuntimeDatabase getRuntimeDatabase() {
        if (RuntimeEnv.LOCAL != getRuntimeEnv()) {
            throw new RuntimeException(
                    "Underlying database can be specified when Latke runs on Local environment only");
        }

        final String runtimeDatabase =
                LOCAL_PROPS.getProperty("runtimeDatabase");

        final RuntimeDatabase ret = RuntimeDatabase.valueOf(runtimeDatabase);
        if (null == ret) {
            throw new RuntimeException(
                    "Please configures runtime database in local.properties!");
        }

        return ret;
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
        if (RuntimeDatabase.SLEEPYCAT == getRuntimeDatabase()) {
            if (Strings.isEmptyOrNull(repositoryPath)) {
                throw new RuntimeException(
                        "Repository path has not been initialized!");
            }
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
     * Determines whether Latkes runs with a JDBC database.
     * 
     * @return {@code true} if Latkes runs with a JDBC database, returns 
     * {@code false} otherwise
     */
    public static boolean runsWithJDBCDatabase() {
        return RuntimeEnv.LOCAL == Latkes.getRuntimeEnv()
               && RuntimeDatabase.SLEEPYCAT != Latkes.getRuntimeDatabase();
    }

    /**
     * Shutdowns Latkes.
     */
    public static void shutdown() {
        try {
            if (RuntimeEnv.LOCAL != getRuntimeEnv()) {
                return;
            }

            final RuntimeDatabase runtimeDatabase = getRuntimeDatabase();
            switch (runtimeDatabase) {
                case SLEEPYCAT:
                    final Class<?> sleepycat =
                            Class.forName(
                            "org.b3log.latke.repository.sleepycat.Sleepycat");
                    final Method shutdown = sleepycat.getMethod("shutdown");
                    shutdown.invoke(sleepycat);

                    break;
                default:
                    break;
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Shutdowns Latke failed", e);
        }
    }

    /**
     * Private constructor.
     */
    private Latkes() {
    }
}
