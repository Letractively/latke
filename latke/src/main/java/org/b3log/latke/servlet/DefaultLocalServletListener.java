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

package org.b3log.latke.servlet;

import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSessionEvent;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeEnv;

/**
 * Default GAE servlet listener for the application runs on local environment.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Feb 24, 2011
 */
public class DefaultLocalServletListener extends AbstractServletListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(DefaultLocalServletListener.class.getName());

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        Latkes.setRuntimeEnv(RuntimeEnv.LOCAL);
        Latkes.setRepositoryPath(""); // TODO: Generates repository directory
        LOGGER.info("Latke is running on local.");

        super.contextInitialized(servletContextEvent);

        LOGGER.info("Initialized the context");
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);

        LOGGER.info("Destroyed the context");
    }

    @Override
    public void sessionCreated(final HttpSessionEvent httpSessionEvent) {
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent httpSessionEvent) {
    }

    @Override
    public void requestInitialized(final ServletRequestEvent servletRequestEvent) {
    }

    @Override
    public void requestDestroyed(final ServletRequestEvent servletRequestEvent) {
    }
}
