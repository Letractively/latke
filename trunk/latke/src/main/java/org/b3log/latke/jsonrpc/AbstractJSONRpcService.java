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

package org.b3log.latke.jsonrpc;

/**
 * Abstract json RPC service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.1.0, Dec 7, 2010
 */
public abstract class AbstractJSONRpcService {

    /**
     * JavaScirpt client service object name.
     */
    private String serviceObjectName = genServiceObjectName();

    /**
     * Gets the JavaScirpt client service object name.
     *
     * @return service object name
     */
    public final String getServiceObjectName() {
        return serviceObjectName;
    }

    /**
     * Generates service object name for this object.
     *
     * @return the generated service object name
     */
    private String genServiceObjectName() {
        final String simpleName = getClass().getSimpleName();
        final char firstChar = simpleName.charAt(0);

        return Character.toLowerCase(firstChar) + simpleName.substring(1);
    }
}
