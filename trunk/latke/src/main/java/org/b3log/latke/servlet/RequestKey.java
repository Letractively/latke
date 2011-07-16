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

/**
 * HTTP request key.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jul 16, 2011
 */
public final class RequestKey {

    /**
     * Request URI.
     */
    private String requestURI;
    /**
     * Request method.
     */
    private String method;

    /**
     * Gets the method.
     * 
     * @return method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the method with the specified method.
     * 
     * @param method the specified method
     */
    public void setMethod(final String method) {
        this.method = method;
    }

    /**
     * Gets the request URI.
     * 
     * @return request URI
     */
    public String getRequestURI() {
        return requestURI;
    }

    /**
     * Sets the request URI with the specified request URI.
     * 
     * @param requestURI the specified request URI
     */
    public void setRequestURI(final String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RequestKey other = (RequestKey) obj;
        if ((this.requestURI == null) ? (other.requestURI != null)
            : !this.requestURI.equals(other.requestURI)) {
            return false;
        }
        if ((this.method == null) ? (other.method != null)
            : !this.method.equals(other.method)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 2;
        hash = 2 * hash + (this.requestURI != null
                           ? this.requestURI.hashCode() : 0);
        hash = 2 * hash + (this.method != null ? this.method.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "requestURI=" + requestURI + ", method=" + method;
    }
}
