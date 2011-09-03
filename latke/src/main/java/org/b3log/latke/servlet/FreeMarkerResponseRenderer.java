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

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;

/**
 * <a href="http://freemarker.org">FreeMarker</a> HTTP response renderer.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jul 16, 2011
 */
// TODO: i18n handling
public final class FreeMarkerResponseRenderer extends AbstractHTTPResponseRenderer {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(FreeMarkerResponseRenderer.class.getName());
    /**
     * Template name.
     */
    private String templateName;
    /**
     * Data model.
     */
    private Map<String, Object> dataModel = new HashMap<String, Object>();
    /**
     * FreeMarker configuration.
     */
    private static final Configuration TEMPLATE_CFG = new Configuration();

    static {
        try {
            TEMPLATE_CFG.setDirectoryForTemplateLoading(new File(
                    AbstractServletListener.getWebRoot()));
        } catch (final IOException e) {
            throw new RuntimeException("Can not find the template directory!", e);
        }
    }

    @Override
    public void render(final HTTPRequestContext context) {
        final HttpServletResponse response = context.getResponse();
        try {
            final PrintWriter writer = response.getWriter();
            // TODO: caching
            final Template template = TEMPLATE_CFG.getTemplate(templateName);

            template.process(dataModel, writer);
            writer.close();
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "FreeMarker renders error", e);

            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, "Can not sned error 500!", ex);
            }
        }
    }

    /**
     * Gets the data model.
     * 
     * @return data model
     */
    public Map<String, Object> getDataModel() {
        return dataModel;
    }

    /**
     * Gets the template name.
     * 
     * @return template name
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Sets the template name with the specified template name.
     * 
     * @param templateName the specified template name
     */
    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }
}
