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

package org.b3log.latke.plugin;

import java.util.Map;

/**
 * Pluginable.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Jun 12, 2011
 */
public interface Pluginable {
    
    /**
     * Gets the template content of main view.
     * 
     * <p>
     *   Main view is the template file named "plugin.ftl".
     * </p>
     * 
     * @return template content
     */
    String getMainViewContent();
    
    /**
     * Gets the author of this plugin.
     * 
     * @return  author of this plugin
     */
    String getAuthor();
    
    /**
     * Gets the version of this plugin.
     * 
     * @return version of this plugin
     */
    String getVersion();

    /**
     * Gets the name of this plugin.
     * 
     * @return name of this plugin
     */
    String getName();

    /**
     * Gets the current status.
     * 
     * @return status
     */
    PluginStatus getStatus();

    /**
     * Plugs.
     * 
     * @param dataModel the specified data model
     */
    void plug(final Map<String, Object> dataModel);

    /**
     * Unplugs.
     */
    void unplug();

    /**
     * Gets the template file name of an existing view that this plugin want to
     * plug.
     * 
     * @return a template file name
     */
    String getViewName();
}
