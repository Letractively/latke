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
package org.b3log.latke.client;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.b3log.latke.jsonrpc.JSONRpcServiceModule;
import org.jabsorb.JSONRPCServlet;

/**
 * Abstract client-side module for IoC 
 * environment(<a href="http://code.google.com/p/google-guice/">Guice</a>)
 * configurations in servlet container.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.9, Sep 2, 2010
 * @see AbstractClientModule#configureServlets() 
 */
public abstract class AbstractClientModule extends ServletModule {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractClientModule.class.getName());
    /**
     * <a href="http://jabsorb.org/">jabsorb</a>(JSON-RPC) initialize
     * parameters. Override this configuration in subclass' constructor.
     */
    private Map<String, String> jabsorbInitParam;

    /**
     * Public default constructor.
     */
    public AbstractClientModule() {
        jabsorbInitParam = new HashMap<String, String>();
        jabsorbInitParam.put("gzip_threshold", "200");
    }

    /**
     * Configures some filters, servlets, remote JavaScript module for
     * <a href="http://code.google.com/p/google-guice/">Guice</a>.
     *
     * Servlets:
     * <ul>
     *   <li>{@link JSONRPCServlet}</li>
     * </ul>
     * JSON RPC Module:
     * <ul>
     *   <li>{@link JSONRpcServiceModule}</li>
     * </ul>
     */
    @Override
    protected void configureServlets() {
        // servlets
        bind(JSONRPCServlet.class).in(Scopes.SINGLETON);
        serve("/json-rpc.do").with(JSONRPCServlet.class, jabsorbInitParam);

        // json RPC services
        install(new JSONRpcServiceModule());
    }
}
