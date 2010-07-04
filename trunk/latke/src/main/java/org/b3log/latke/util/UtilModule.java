/*
 * Copyright 2009, 2010, B3log Team
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

package org.b3log.latke.util;

import org.b3log.latke.util.cache.Cache;
import org.b3log.latke.util.cache.memory.LruMemoryCache;
import org.b3log.latke.util.cache.qualifier.LruMemory;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

/**
 * Abstract server-side module for
 * <a href="http://code.google.com/p/google-guice/">Guice</a> configurations.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jun 24, 2010
 */
public class UtilModule extends AbstractModule {

    /**
     * Configures utilities for
     * <a href="http://code.google.com/p/google-guice/">Guice</a>.
     * <ul>
     *   <li>{@link Cache}</li>
     * </ul>
     */
    @Override
    protected void configure() {
        bind(new TypeLiteral<Cache<String, ?>>() {
        }).annotatedWith(LruMemory.class).to(
                new TypeLiteral<LruMemoryCache<String, ?>>() {
                }).asEagerSingleton();
    }
}
