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
package org.b3log.latke.urlfetch.local;

import org.b3log.latke.servlet.HTTPRequestMethod;

/**
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 0.0.0.1, Aug 10, 2011
 * 
 */
public final class UrlFetchHandlerFactory {

    /**
     * 
     * @param requestMethod XXX
     * @return {@link UrlFetchCommonHandler}
     */
    public static UrlFetchCommonHandler getFetchHandler(final HTTPRequestMethod requestMethod) {

        UrlFetchCommonHandler fetchHandler = null;

        switch (requestMethod) {
        case POST:
            fetchHandler = new UrlFetchPostHandler();
            break;
        default:
            fetchHandler = new UrlFetchCommonHandler();
            break;

        }
        return fetchHandler;
    }

    /**
     * 
     */
    private UrlFetchHandlerFactory() {

    }

}
