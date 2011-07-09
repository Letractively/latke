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

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.model.Plugin;
import org.b3log.latke.util.Strings;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Abstract plugin.
 * 
 * <p>
 *   <b>Note</b>: The subclass implementation of this abstract class MUST 
 *   has a static method named {@code getInstance} to obtain an instance of 
 *   this plugin. See 
 *   <a href="http://en.wikipedia.org/wiki/Singleton_pattern">
 *   Singleton Pattern</a> for more implementation details.
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.5, Jun 26, 2011
 */
public abstract class AbstractPlugin {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractPlugin.class.getName());
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
     * Directory of this plugin.
     */
    private String dir;
    /**
     * Status of this plugin.
     */
    private PluginStatus status = PluginStatus.ENABLED;
    /**
     * Types of this plugin.
     */
    private Set<PluginType> types = new HashSet<PluginType>();
    /**
     * Languages.
     */
    private Map<String, Properties> langs = new HashMap<String, Properties>();
    /**
     * FreeMarker configuration.
     */
    private Configuration configuration;

    /**
     * Gets an existing view name.
     * 
     * @return view name, the plugin to plug
     */
    public abstract String getViewName();

    /**
     * Unplugs.
     */
    public abstract void unplug();

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
     * Initializes template engine.
     * 
     * @param dir the specified directory
     */
    public void setDir(final String dir) {
        this.dir = dir;

        configuration = new Configuration();
        configuration.setDefaultEncoding("UTF-8");
        try {
            configuration.setDirectoryForTemplateLoading(new File(dir));
        } catch (final IOException e) {
            Logger.getLogger(getClass().getName()).
                    log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Reads lang_xx.properties into field {@link #langs langs}.
     */
    public void readLangs() {
        final File root = new File(dir);
        final File[] langFiles = root.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                if (name.startsWith(Keys.LANGUAGE)
                    && name.endsWith(".properties")) {
                    return true;
                }

                return false;
            }
        });

        for (int i = 0; i < langFiles.length; i++) {
            final File lang = langFiles[i];
            final String langFileName = lang.getName();
            final String key =
                    langFileName.substring(Keys.LANGUAGE.length() + 1,
                                           langFileName.lastIndexOf("."));
            final Properties props = new Properties();
            try {
                props.load(new FileInputStream(lang));
                langs.put(key, props);
            } catch (final Exception e) {
                Logger.getLogger(getClass().getName()).
                        log(Level.SEVERE,
                            "Get plugin[name="
                            + name + "]'s language configuration failed", e);
            }
        }
    }

    /**
     * Gets language label with the specified locale and key.
     * 
     * @param locale the specified locale
     * @param key the specified key
     * @return language label
     */
    public String getLang(final Locale locale, final String key) {
        return langs.get(locale.toString()).getProperty(key);
    }

    /**
     * Plugs with the specified data model.
     * 
     * @param dataModel the specified data model
     */
    public void plug(final Map<String, Object> dataModel) {
        String content = (String) dataModel.get(Plugin.PLUGINS);
        if (null == content) {
            dataModel.put(Plugin.PLUGINS, "");
        }
        handleLangs(dataModel);

        content = (String) dataModel.get(Plugin.PLUGINS);
        final StringBuilder contentBuilder = new StringBuilder(content);

        contentBuilder.append(getViewContent(dataModel));

        final String pluginsContent = contentBuilder.toString();
        dataModel.put(Plugin.PLUGINS, pluginsContent);

        LOGGER.log(Level.FINER, "Plugin[name={0}] has been plugged", getName());
    }

    /**
     * Processes languages. Retrieves language labels with default locale, then 
     * sets them into the specified data model.
     * 
     * @param dataModel the specified data model
     */
    private void handleLangs(final Map<String, Object> dataModel) {
        final Locale locale = Latkes.getDefaultLocale();
        final String language = locale.getLanguage();
        final String country = locale.getCountry();
        final String variant = locale.getVariant();

        final StringBuilder keyBuilder = new StringBuilder(language);
        if (!Strings.isEmptyOrNull(country)) {
            keyBuilder.append("_").append(country);
        }
        if (!Strings.isEmptyOrNull(variant)) {
            keyBuilder.append("_").append(variant);
        }

        final String localKey = keyBuilder.toString();
        final Properties props = langs.get(localKey);
        final Set<Object> keySet = props.keySet();
        for (final Object key : keySet) {
            dataModel.put((String) key, props.getProperty((String) key));
        }
    }

    /**
     * Gets view content of a plugin. The content is processed with the 
     * specified data model by template engine.
     * 
     * @param dataModel the specified data model
     * @return plugin view content
     */
    private String getViewContent(final Map<String, Object> dataModel) {
        try {
            final Template template =
                    configuration.getTemplate(Plugin.PLUGIN + ".ftl");
            final StringWriter sw = new StringWriter();
            template.process(dataModel, sw);

            return sw.toString();
        } catch (final Exception e) {
            Logger.getLogger(getClass().getName()).
                    log(Level.SEVERE,
                        "Get plugin[name=" + name
                        + "]'s view failed, will return warning", e);
            return "<div style='color: red;'>Plugin[name="
                   + name + "] runs failed</div>";
        }
    }

    /**
     * Converts this plugin to a json object.
     * 
     * @return a json object, for example, 
     * <pre>
     * {
     *     "name": "",
     *     "version": "",
     *     "author": "",
     *     "status": {@link PluginStatus}
     * }
     * </pre>
     * @throws JSONException if can not convert 
     */
    public JSONObject toJSONObject() throws JSONException {
        final JSONObject ret = new JSONObject();
        ret.put(Plugin.PLUGIN_NAME, getName());
        ret.put(Plugin.PLUGIN_VERSION, getVersion());
        ret.put(Plugin.PLUGIN_AUTHOR, getAuthor());
        ret.put(Plugin.PLUGIN_STATUS, getStatus());

        return ret;
    }

    /**
     * Sets the status with the specified status.
     * 
     * @param status the specified status
     */
    public void setStatus(final PluginStatus status) {
        this.status = status;
    }

    /**
     * Gets the status of this plugin.
     * 
     * @return status
     */
    public PluginStatus getStatus() {
        return status;
    }

    /**
     * Gets the author of this plugin.
     * 
     * @return author
     */
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

    /**
     * Gets the name of this plugin.
     * 
     * @return name
     */
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

    /**
     * Gets the version of this plugin.
     * 
     * @return version
     */
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

    /**
     * Gets the types of this plugin.
     * 
     * @return types
     */
    public Set<PluginType> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    /**
     * Adds the specified type.
     * 
     * @param type the specified type
     */
    public void addType(final PluginType type) {
        types.add(type);
    }
}