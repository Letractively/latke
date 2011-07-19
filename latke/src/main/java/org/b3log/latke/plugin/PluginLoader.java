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
import java.lang.reflect.Method;
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
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.jsonrpc.AbstractJSONRpcService;
import org.b3log.latke.model.Plugin;
import org.b3log.latke.servlet.AbstractServletListener;
import org.b3log.latke.util.Strings;
import org.jabsorb.JSONRPCBridge;

/**
 * Plugin loader.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.6, Jul 19, 2011
 */
public final class PluginLoader {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PluginLoader.class.
            getName());
    /**
     * Name of plugin cache.
     */
    private static final String PLUGIN_CACHE_NAME = "pluginCache";
    /**
     * Plugins cache.
     * 
     * <p>
     * Caches plugins with the key "plugins" and its value is the real holder, 
     * a map:
     * &lt;"hosting view name", plugins&gt;
     * </p>
     */
    private static final Cache<String, Map<String, List<AbstractPlugin>>> PLUGINS =
            CacheFactory.getCache(PLUGIN_CACHE_NAME);
    /**
     * Plugin root directory.
     */
    public static final String PLUGIN_ROOT =
            AbstractServletListener.getWebRoot() + Plugin.PLUGINS;

    /**
     * Gets all plugins.
     * 
     * @return all plugins, returns an empty list if not found
     */
    public static List<AbstractPlugin> getPlugins() {
        final List<AbstractPlugin> ret = new ArrayList<AbstractPlugin>();

        Map<String, List<AbstractPlugin>> pluginMap =
                PLUGINS.get(Plugin.PLUGINS);
        if (null == pluginMap || pluginMap.isEmpty()) {
            LOGGER.info("Loads plugins....");
            load();

            pluginMap = PLUGINS.get(Plugin.PLUGINS);
        }

        for (final Map.Entry<String, List<AbstractPlugin>> entry : pluginMap.
                entrySet()) {
            ret.addAll(entry.getValue());
        }

        return ret;
    }

    /**
     * Gets a plugin by the specified view name.
     * 
     * @param viewName the specified view name
     * @return a plugin, returns an empty list if not found
     */
    public static List<AbstractPlugin> getPlugins(final String viewName) {
        final Map<String, List<AbstractPlugin>> pluginMap = getPluginsMap();

        final List<AbstractPlugin> ret = pluginMap.get(viewName);
        if (null == ret) {
            return Collections.emptyList();
        }

        return ret;
    }

    /**
     * Loads plugins from directory {@literal webRoot/plugins/}.
     */
    private static void load() {
        final File[] pluginsDirs = new File(PLUGIN_ROOT).listFiles();

        for (int i = 0; i < pluginsDirs.length; i++) {
            final File pluginDir = pluginsDirs[i];
            if (pluginDir.isDirectory() && !pluginDir.isHidden() && !pluginDir.
                    getName().startsWith(".")) {
                try {
                    LOGGER.log(Level.INFO, "Loading plugin under directory[{0}]",
                               pluginDir.getName());
                    load(pluginDir);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING,
                               "Load plugin under directory["
                               + pluginDir.getName() + "] failed", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "It[{0}] is not a directory under "
                                          + "directory plugins, ignored",
                           pluginDir.getName());
            }

        }
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
        props.load(new FileInputStream(pluginDir.getPath() + File.separator
                                       + "plugin.properties"));

        final String pluginClassName = props.getProperty(Plugin.PLUGIN_CLASS);
        final Class<?> pluginClass = classLoader.loadClass(pluginClassName);
        final AbstractPlugin plugin = (AbstractPlugin) pluginClass.newInstance();

        setPluginProps(pluginDir, plugin, props);
        registerJSONRpcServices(props, classLoader, plugin);
        registerEventListeners(props, classLoader, plugin);

        register(plugin);
    }

    /**
     * Registers the specified plugin.
     * 
     * @param plugin the specified plugin
     */
    private static void register(final AbstractPlugin plugin) {
        final String viewName = plugin.getViewName();

        final Map<String, List<AbstractPlugin>> pluginMap = getPluginsMap();

        List<AbstractPlugin> list = pluginMap.get(viewName);
        if (null == list) {
            list = new ArrayList<AbstractPlugin>();
            pluginMap.put(viewName, list);
        }

        list.add(plugin);
        PLUGINS.put(Plugin.PLUGINS, pluginMap);

        LOGGER.log(Level.FINER,
                   "Registered plugin[name={0}, version={1}] for view[name={2}], "
                   + "[{3}] plugins totally", new Object[]{
                    plugin.getName(), plugin.getVersion(), viewName,
                    pluginMap.size()});
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
        final String jsonRpcClasses = props.getProperty(
                Plugin.PLUGIN_JSON_RPC_CLASSES);
        LOGGER.log(Level.FINEST,
                   "Plugin[name={0}, author={1}, version={2}, types={3}, "
                   + "jsonRpcClasses={4}]", new Object[]{name, author,
                                                         version, types,
                                                         jsonRpcClasses});
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
     * Registers json rpc services with the specified plugin properties, class 
     * loader and plugin.
     * 
     * <p>
     *   <b>Note</b>: If the specified plugin has some json rpc services, each
     *   of these service MUST implement a static method named 
     *   {@code getInstance} to obtain an instance of this service. See 
     *   <a href="http://en.wikipedia.org/wiki/Singleton_pattern">
     *   Singleton Pattern</a> for more details.
     * </p>
     * 
     * @param props the specified plugin properties
     * @param classLoader the specified class loader
     * @param plugin the specified plugin
     * @throws Exception exception
     */
    private static void registerJSONRpcServices(final Properties props,
                                                final URLClassLoader classLoader,
                                                final AbstractPlugin plugin)
            throws Exception {
        final String jsonRpcClasses =
                props.getProperty(Plugin.PLUGIN_JSON_RPC_CLASSES);
        final String[] jsonRpcClassArray = jsonRpcClasses.split(",");
        for (int i = 0; i < jsonRpcClassArray.length; i++) {
            final String jsonRpcClassName = jsonRpcClassArray[i];
            if (Strings.isEmptyOrNull(jsonRpcClassName)) {
                LOGGER.log(Level.INFO,
                           "No json rpc service to load for plugin[name={0}]",
                           plugin.getName());
                return;
            }

            final Class<?> jsonRpcClass =
                    classLoader.loadClass(jsonRpcClassName);
            final Method getInstance = jsonRpcClass.getMethod("getInstance");
            final AbstractJSONRpcService jsonRpcService =
                    (AbstractJSONRpcService) getInstance.invoke(jsonRpcClass);

            JSONRPCBridge.getGlobalBridge().registerObject(
                    jsonRpcService.getServiceObjectName(), jsonRpcService);
            LOGGER.log(Level.FINER,
                       "Registered json rpc service[{0}] for plugin[name={1}]",
                       new Object[]{jsonRpcService.getServiceObjectName(),
                                    plugin.getName()});
        }
    }

    /**
     * Registers event listeners with the specified plugin properties, class 
     * loader and plugin.
     *
     * <p>
     *   <b>Note</b>: If the specified plugin has some event listeners, each
     *   of these listener MUST implement a static method named 
     *   {@code getInstance} to obtain an instance of this listener. See 
     *   <a href="http://en.wikipedia.org/wiki/Singleton_pattern">
     *   Singleton Pattern</a> for more details.
     * </p>
     * 
     * @param props the specified plugin properties
     * @param classLoader the specified class loader
     * @param plugin the specified plugin
     * @throws Exception exception
     */
    private static void registerEventListeners(final Properties props,
                                               final URLClassLoader classLoader,
                                               final AbstractPlugin plugin)
            throws Exception {
        final String eventListenerClasses =
                props.getProperty(Plugin.PLUGIN_EVENT_LISTENER_CLASSES);
        final String[] eventListenerClassArray = eventListenerClasses.split(",");
        for (int i = 0; i < eventListenerClassArray.length; i++) {
            final String eventListenerClassName = eventListenerClassArray[i];
            if (Strings.isEmptyOrNull(eventListenerClassName)) {
                LOGGER.log(Level.INFO,
                           "No event listener to load for plugin[name={0}]",
                           plugin.getName());
                return;
            }

            LOGGER.log(Level.FINEST, "Loading event listener[class={0}]",
                       eventListenerClassName);

            final Class<?> eventListenerClass =
                    classLoader.loadClass(eventListenerClassName);
            final Method getInstance =
                    eventListenerClass.getMethod("getInstance");
            final AbstractEventListener<?> eventListener =
                    (AbstractEventListener) getInstance.invoke(
                    eventListenerClass);

            final EventManager eventManager = EventManager.getInstance();
            eventManager.registerListener(eventListener);
            LOGGER.log(Level.FINER,
                       "Registered event listener[class={0}, eventType={1}] for plugin[name={2}]",
                       new Object[]{eventListener.getClass(),
                                    eventListener.getEventType(),
                                    plugin.getName()});
        }
    }

    /**
     * Gets plugins holder map.
     * 
     * <p>
     * If not found the plugin holder map in {@linkplain #PLUGINS cache}, 
     * creates an empty map then put it into the cache and return.
     * </p>
     * 
     * @return plugins holder map
     */
    private static Map<String, List<AbstractPlugin>> getPluginsMap() {
        Map<String, List<AbstractPlugin>> ret = PLUGINS.get(Plugin.PLUGINS);
        if (null == ret) {
            ret = new HashMap<String, List<AbstractPlugin>>();

            PLUGINS.put(Plugin.PLUGINS, ret);

            LOGGER.log(Level.INFO, "Created an empty plugins holder map");
        }

        return ret;
    }

    /**
     * Private default constructor.
     */
    private PluginLoader() {
    }
}
