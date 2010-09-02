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
package org.b3log.latke.jsonrpc;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.jsonrpc.impl.LanguageService;
import org.b3log.latke.client.util.RemoteJSServiceClassLoader;
import org.b3log.latke.servlet.AbstractServletListener;

/**
 * Client-side json RPC service module for IoC
 * environment(<a href="http://code.google.com/p/google-guice/">Guice</a>)
 * configurations in servlet container.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Sep 2, 2010
 */
public final class JSONRpcServiceModule extends AbstractModule {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(JSONRpcServiceModule.class.getName());

    @Override
    protected void configure() {
        LOGGER.finer("Initializing remote JavaScirpt services....");
        String clientRemoteServicesPath = null;
        try {
            final ClassLoader classLoader = AbstractServletListener.class.
                    getClassLoader();
            clientRemoteServicesPath =
                    classLoader.getResource(AbstractServletListener.
                    getClientRemoteServicePackage()).toURI().getPath();
            LOGGER.log(Level.FINER,
                       "Client remote JavaScirpt services path of application is [{0}]",
                       clientRemoteServicesPath);
        } catch (final URISyntaxException e) {
            LOGGER.severe(e.getMessage());
            throw new RuntimeException(e);
        }

        final List<Class<?>> serviceClasses =
                RemoteJSServiceClassLoader.loadServiceClasses(
                clientRemoteServicesPath);
        serviceClasses.add(LanguageService.class); // XXX: one by one manually?

        for (final Class<?> serviceClass : serviceClasses) {
            bind(serviceClass).in(Scopes.SINGLETON);
        }
    }
}
