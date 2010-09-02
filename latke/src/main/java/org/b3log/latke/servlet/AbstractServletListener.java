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

import java.util.logging.Level;
import org.b3log.latke.util.Strings;
import org.b3log.latke.util.jabsorb.serializer.FwkStatusCodesSerializer;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.b3log.latke.Latkes;
import org.b3log.latke.jsonrpc.AbstractJSONRpcService;
import org.b3log.latke.jsonrpc.impl.LanguageService;
import org.b3log.latke.jsonrpc.util.JSONRPCServiceClassLoader;
import org.jabsorb.JSONRPCBridge;

/**
 * Abstract servlet listener.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.2.4, Sep 2, 2010
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
            Logger.getLogger(AbstractServletListener.class.getName());
    /**
     * Web root.
     */
    private static String webRoot;
    /**
     * Guice injector.
     */
    private Injector injector;
    /**
     * The directory of client remote service(via JSON-RPC) implementation
     * package.
     */
    private static String clientRemoteServicePackage;

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
        LOGGER.log(Level.INFO, "Default locale[{0}]", Latkes.getDefaultLocale());

        final ServletContext servletContext =
                servletContextEvent.getServletContext();
        webRoot = servletContext.getRealPath("") + File.separator;
        final String catalinaBase = System.getProperty("catalina.base");
        LOGGER.log(Level.INFO, "[Web root[path={0}, catalina.base={1}]",
                   new Object[]{webRoot, catalinaBase});

        registerRemoteJSServices();
        registerRemoteJSServiceSerializers();
    }

    /**
     * Registers remote JavaScript services.
     */
    private void registerRemoteJSServices() {
        try {
            final ClassLoader classLoader = AbstractServletListener.class.
                    getClassLoader();
            final String clientRemoteServicesPath =
                    classLoader.getResource(AbstractServletListener.
                    getClientRemoteServicePackage()).toURI().getPath();
            final List<Class<?>> serviceClasses =
                    JSONRPCServiceClassLoader.loadServiceClasses(
                    clientRemoteServicesPath);
            serviceClasses.add(LanguageService.class); // XXX: one by one manually?

            for (final Class<?> serviceClass : serviceClasses) {
                final AbstractJSONRpcService serviceObject =
                        (AbstractJSONRpcService) injector.getInstance(
                        serviceClass);
                JSONRPCBridge.getGlobalBridge().registerObject(serviceObject.
                        getServiceObjectName(), serviceObject);
            }
        } catch (final Exception e) {
            LOGGER.severe("Register remote JavaScript service error");
            throw new RuntimeException(e);
        }
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
            LOGGER.severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
