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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.model.Plugin;
import org.b3log.latke.servlet.AbstractServletListener;

/**
 * Plugin manager.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jun 11, 2011
 */
public final class PluginManager {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(PluginManager.class.getName());
    /**
     * Plugins.
     */
    private static final Map<String, List<Pluginable>> PLUGINS =
            new HashMap<String, List<Pluginable>>();
    /**
     * Plugin root directory.
     */
    public static final String PLUGIN_ROOT = AbstractServletListener.getWebRoot()
                                             + Plugin.PLUGINS;

    /**
     * Loads plugins from directory {@literal ${webRoot}/plugins/}.
     */
    public static void load() {
        final File[] pluginsDirs = new File(PLUGIN_ROOT).listFiles();

        for (int i = 0; i < pluginsDirs.length; i++) {
            final File pluginDir = pluginsDirs[i];
            if (pluginDir.isDirectory() && !pluginDir.isHidden()
                && !pluginDir.getName().startsWith(".")) {
                try {
                    load(pluginDir);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING,
                               "Load plugin under directory[" + pluginDir.
                            getName() + "] failed", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "It[{0}] is not a directory under "
                                          + "directory plugins, ignored",
                           pluginDir.getName());
            }

        }
    }

    /**
     * Gets a plugin by the specified view name.
     * 
     * @param viewName the specified view name
     * @return a plugin, returns an empty list if not found
     */
    public static List<Pluginable> getPlugins(final String viewName) {
        final List<Pluginable> ret = PLUGINS.get(viewName);
        if (null == ret) {
            return Collections.emptyList();
        }

        return ret;
    }

    /**
     * Registers the specified plugin.
     * 
     * @param plugin the specified plugin
     */
    private static void register(final Pluginable plugin) {
        final String viewName = plugin.getViewName();
        List<Pluginable> list = PLUGINS.get(viewName);
        if (null == list) {
            list = new ArrayList<Pluginable>();
            PLUGINS.put(viewName, list);
        }

        list.add(plugin);

        LOGGER.log(Level.FINER,
                   "Registered plugin[name={0}, version={1}] for view[name={2}], "
                   + "[{3}] plugins totally",
                   new Object[]{plugin.getName(), plugin.getVersion(), viewName,
                                PLUGINS.size()});
    }

    /**
     * Loads a plugin by the specified plugin directory.
     * 
     * @param pluginDir the specified plugin directory
     * @throws Exception exception 
     */
    private static void load(final File pluginDir) throws Exception {
        final File classesFileDir = new File(pluginDir.getPath()
                                             + File.separator + "classes");
        final URL url = classesFileDir.toURI().toURL();
        LOGGER.log(Level.FINEST, "Loading class from URL[path={0}]",
                   url.getPath());
        final URLClassLoader classLoader = new URLClassLoader(new URL[]{url});

        final Properties props = new Properties();
        props.load(new FileInputStream(
                pluginDir.getPath() + File.separator + "plugin.properties"));

        final String className = props.getProperty(Plugin.PLUGIN_CLASS);
        final Class<?> pluginClass = classLoader.loadClass(className);

        final AbstractPlugin plugin = (AbstractPlugin) pluginClass.newInstance();
        setPluginProps(pluginDir, plugin, props);

        PluginManager.register(plugin);
    }

    /**
     * Sets the specified plugin's properties from the specified properties file 
     * under the specified plugin directory.
     * 
     * @param pluginDir the specified plugin directory
     * @param plugin the specified plugin
     * @param props the specified properties file
     * @throws Exception exception
     */
    private static void setPluginProps(final File pluginDir,
                                       final AbstractPlugin plugin,
                                       final Properties props)
            throws Exception {
        final String author = props.getProperty(Plugin.PLUGIN_AUTHOR);
        final String name = props.getProperty(Plugin.PLUGIN_NAME);
        final String version = props.getProperty(Plugin.PLUGIN_VERSION);
        final String types = props.getProperty(Plugin.PLUGIN_TYPES);
        LOGGER.log(Level.FINEST,
                   "Plugin[name={0}, author={1}, version={2}, types={3}]",
                   new Object[]{name, author, version, types});
        plugin.setAuthor(author);
        plugin.setName(name);
        plugin.setVersion(version);
        plugin.setDir(pluginDir.getPath());
        plugin.readLangs();
        final String[] typeArray = types.split(",");
        for (int i = 0; i < typeArray.length; i++) {
            final PluginType type = PluginType.valueOf(typeArray[i]);
            plugin.addType(type);
        }
    }

    /**
     * Private default constructor.
     */
    private PluginManager() {
    }
}
