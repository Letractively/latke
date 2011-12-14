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
package org.b3log.latke.servlet.renderer.freemarker;

import java.util.logging.Logger;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.action.util.PageCaches;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.util.Strings;
import org.json.JSONObject;
import static org.b3log.latke.action.AbstractCacheablePageAction.*;

/**
 * <a href="http://freemarker.org">FreeMarker</a> HTTP response 
 * renderer.
 * 
 * <p>
 * This renderer will put page content into cache.
 * <p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.3, Dec 14, 2011
 */
public class CacheFreeMarkerRenderer extends AbstractFreeMarkerRenderer {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(CacheFreeMarkerRenderer.class.getName());

    @Override
    protected void beforeRender(final HTTPRequestContext context)
            throws Exception {
    }

    @Override
    protected void afterRender(final HTTPRequestContext context)
            throws Exception {
        final HttpServletRequest request = context.getRequest();
        final String pageContent = (String) request.getAttribute(CACHED_CONTENT);

        if (null == pageContent) {
            return;
        }

        if (Latkes.isPageCacheEnabled()) {
            final String cachedPageKey =
                    (String) request.getAttribute(Keys.PAGE_CACHE_KEY);
            if (Strings.isEmptyOrNull(cachedPageKey)) {
                return;
            }

            LOGGER.log(Level.FINEST, "Caching page[cachedPageKey={0}]",
                       cachedPageKey);

            check(request, pageContent);

            final JSONObject cachedValue = new JSONObject();
            cachedValue.put(CACHED_CONTENT, pageContent);
            cachedValue.put(CACHED_TYPE, request.getAttribute(CACHED_TYPE));
            cachedValue.put(CACHED_OID, request.getAttribute(CACHED_OID));
            cachedValue.put(CACHED_TITLE, request.getAttribute(CACHED_TITLE));
            cachedValue.put(CACHED_LINK, request.getAttribute(CACHED_LINK));

            PageCaches.put(cachedPageKey, cachedValue);
            LOGGER.log(Level.FINEST, "Cached page[cachedPageKey={0}]",
                       cachedPageKey);
        }
    }
}
