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

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.b3log.latke.model.Plugin;

/**
 * Abstract plugin.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jun 12, 2011
 */
public abstract class AbstractPlugin implements Pluginable {

    /**
     * Name of this plugin.
     */
    private String name;
    /**
     * Author of this author.
     */
    private String author;
    /**
     * Version of this plugin.
     */
    private String version;
    /**
     * Main view content.
     */
    private String mainViewContent;
    /**
     * Directory of this plugin.
     */
    private String dir;
    /**
     * Status of this plugin.
     */
    private PluginStatus status = PluginStatus.ENABLED;

    /**
     * Gets the directory of this plugin.
     * 
     * @return directory of this plugin
     */
    public String getDir() {
        return dir;
    }

    /**
     * Sets the directory of this plugin with the specified directory.
     * 
     * @param dir the specified directory
     */
    public void setDir(final String dir) {
        this.dir = dir;
    }

    @Override
    public String getMainViewContent() {
        if (null == mainViewContent) {
            final File mainView = new File(dir + File.separator + Plugin.PLUGIN
                                           + ".ftl");

            String ret = null;
            try {
                ret = IOUtils.toString(new FileInputStream(mainView));
            } catch (final Exception e) {
                Logger.getLogger(getClass().getName()).
                        log(Level.SEVERE,
                            "Get plugin[name=" + name
                            + "]'s main view failed, will return null", e);
            }

            return ret;
        } else {
            return mainViewContent;
        }
    }

    /**
     * Sets the status with the specified status.
     * 
     * @param status the specified status
     */
    public void setStatus(final PluginStatus status) {
        this.status = status;
    }

    @Override
    public PluginStatus getStatus() {
        return status;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of this plugin with the specified author.
     * 
     * @param author the specified author
     */
    public void setAuthor(final String author) {
        this.author = author;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this plugin with the specified name.
     * 
     * @param name the specified name
     */
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of this plugin with the specified version.
     * 
     * @param version the specified version
     */
    public void setVersion(final String version) {
        this.version = version;
    }
}
