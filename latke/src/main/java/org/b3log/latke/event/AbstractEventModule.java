/*
 * Copyright (c) 2009, 2010, B3log Team
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

package org.b3log.latke.event;

import com.google.inject.AbstractModule;
import java.util.logging.Logger;

/**
 * Abstract event module for IoC
 * environment(<a href="http://code.google.com/p/google-guice/">Guice</a>)
 * configurations.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 12, 2010
 */
public abstract  class AbstractEventModule extends AbstractModule {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractEventModule.class.getName());

    /**
     * Public default constructor.
     */
    public AbstractEventModule() {
    }

    /**
     * Configures event manager.
     */
    @Override
    protected void configure() {
        bind(EventManager.class).toInstance(EventManager.getInstance());
    }
}
