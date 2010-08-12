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
package org.b3log.latke.servlet;

import java.util.List;
import org.b3log.latke.util.Strings;
import org.b3log.latke.util.cache.Cache;
import org.b3log.latke.util.cache.memory.LruMemoryCache;
import org.b3log.latke.util.jabsorb.serializer.FwkStatusCodesSerializer;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.GuiceServletContextListener;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.log4j.Logger;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.event.util.EventListenerClassLoader;
import org.jabsorb.JSONRPCBridge;

/**
 * Abstract servlet listener.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.2.1, Aug 12, 2010
 */
public abstract class AbstractServletListener
        extends GuiceServletContextListener
        implements ServletContextListener,
                   ServletRequestListener,
                   HttpSessionListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractServletListener.class);
    /**
     * Web root.
     */
    private static String webRoot;
    /**
     * Postfix exception paths.
     */
    private static Set<String> postfixExceptionPaths;
    /**
     * Guice injector.
     */
    private Injector injector;
    /**
     * Event manager.
     */
    private EventManager eventManager = EventManager.getInstance();
    /**
     * The directory of client remote service(via JSON-RPC) implementation
     * package.
     */
    private static String clientRemoteServicePackage;
    /**
     * The directory of event listener implementation package.
     */
    private static String eventListenerPackage;
    /**
     * Maximum count of cacheable objects.
     */
    private static final int MAX_CACHEABLE_OBJECT_CNT = 1024;

    /**
     * Initializes context, {@linkplain #webRoot web root},
     * {@linkplain #postfixExceptionPaths postfix exception paths}, registers
     * remote JavaScript services and remote JavaScript service serializers.
     * 
     * @param servletContextEvent servlet context event
     */
    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        LOGGER.info("Initializing the context....");
        super.contextInitialized(servletContextEvent);

        Latkes.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        LOGGER.info("Default locale[" + Latkes.getDefaultLocale() + "]");

        final ServletContext servletContext =
                servletContextEvent.getServletContext();
        webRoot = servletContext.getRealPath("") + File.separator;
        final String catalinaBase = System.getProperty("catalina.base");
        LOGGER.info("[Web root[path=" + webRoot + ", catalina.base="
                    + catalinaBase + "]");

        final String postfixExceptionPathsString =
                servletContext.getInitParameter("postfixExceptionPaths");
        final String[] paths = Strings.trimAll(
                postfixExceptionPathsString.split(","));
        postfixExceptionPaths = org.b3log.latke.util.Collections.arrayToSet(
                paths);
        LOGGER.info("[postfixExceptionPath=" + postfixExceptionPaths + "]");

        initCache();
        registerRemoteJSServiceSerializers();

        initEventManagement();
    }

    /**
     * Initializes cache.
     */
    private void initCache() {
        final Cache<?, ?> cache = getInjector().getInstance(Key.get(
                new TypeLiteral<LruMemoryCache<String, ?>>() {
                }));

        cache.setMaxCount(MAX_CACHEABLE_OBJECT_CNT);

        LOGGER.info("Initialized cache[maxCount=" + MAX_CACHEABLE_OBJECT_CNT
                    + "]");
    }

    /**
     * Destroys the context, unregisters remote JavaScript services.
     *
     * @param servletContextEvent servlet context event
     */
    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        LOGGER.info("Destroying the context....");
        super.contextDestroyed(servletContextEvent);
    }

    @Override
    public abstract void requestDestroyed(
            final ServletRequestEvent servletRequestEvent);

    @Override
    public abstract void requestInitialized(
            final ServletRequestEvent servletRequestEvent);

    @Override
    public abstract void sessionCreated(final HttpSessionEvent httpSessionEvent);

    @Override
    public abstract void sessionDestroyed(
            final HttpSessionEvent httpSessionEvent);

    /**
     * Gets postfix exception paths configured in web.xml.
     *
     * @return a set of postfix exception paths
     */
    public static Set<String> getPostfixExceptionPaths() {
        return Collections.unmodifiableSet(postfixExceptionPaths);
    }

    /**
     * Gets client remote JavaScript service package.
     *
     * @return the client remote service package
     */
    public static String getClientRemoteServicePackage() {
        if (Strings.isEmptyOrNull(clientRemoteServicePackage)) {
            throw new RuntimeException("Please override "
                                       + "clientRemoteServicePackage field!");
        }

        return clientRemoteServicePackage;
    }

    /**
     * Gets event listener package.
     *
     * @return the event listener package
     */
    public static String getEventListenerPackage() {
        if (Strings.isEmptyOrNull(eventListenerPackage)) {
            throw new RuntimeException("Please override "
                                       + "eventListenerPackage field!");
        }

        return eventListenerPackage;
    }

    /**
     * Sets client remote JavaScript service package.
     *
     * @param clientRemoteServicePackage the specified client remote service
     * package
     */
    public static void setClientRemoteServicePackage(
            final String clientRemoteServicePackage) {
        AbstractServletListener.clientRemoteServicePackage =
                clientRemoteServicePackage;
    }

    /**
     * Sets event listener package.
     *
     * @param eventListenerPackage the specified event listener package
     */
    public static void setEventListenerPackage(
            final String eventListenerPackage) {
        AbstractServletListener.eventListenerPackage =
                eventListenerPackage;
    }

    /**
     * Gets the absolute file path of web root directory on the server's
     * file system.
     *
     * @return the directory file path(tailing with {@link File#separator}).
     */
    public static String getWebRoot() {
        return webRoot;
    }

    /**
     * Sets the injector.
     *
     * @param injector the specified injector
     */
    public void setInjector(final Injector injector) {
        this.injector = injector;
    }

    /**
     * Gets the injector.
     *
     * @return injector
     */
    @Override
    public Injector getInjector() {
        return injector;
    }

    /**
     * Registers remote JavaScript service serializers.
     */
    private void registerRemoteJSServiceSerializers() {
        final JSONRPCBridge jsonRpcBridge = JSONRPCBridge.getGlobalBridge();

        try {
            jsonRpcBridge.registerSerializer(new FwkStatusCodesSerializer());
        } catch (final Exception e) {
            LOGGER.fatal(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes event management.
     */
    private void initEventManagement() {
        String eventListenerDirPath = null;
        try {
            final ClassLoader classLoader = getClass().getClassLoader();
            eventListenerDirPath =
                    classLoader.getResource(
                    getEventListenerPackage()).toURI().getPath();
            LOGGER.debug("Event listeners path " + "of application is ["
                         + eventListenerDirPath + "]");
        } catch (final URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(
                    "Event listeners package path configure error!");
        }

        try {
            final List<Class<AbstractEventListener<?>>> loadEventListenerClasses =
                    EventListenerClassLoader.loadEventListenerClasses(
                    eventListenerDirPath);
            for (final Class<AbstractEventListener<?>> listenerClass
                    : loadEventListenerClasses) {
                final AbstractEventListener<?> eventListener =
                        listenerClass.newInstance();
                eventManager.registerListener(eventListener);
                LOGGER.info("Registered an event listener[className=" + listenerClass.
                        getName() + "]");
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("Event listeners load error!");
        }
    }
}
