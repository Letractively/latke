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
package org.b3log.latke.repository.gae;

import org.b3log.latke.repository.Repository;
import org.b3log.latke.repository.RepositoryException;
import org.json.JSONObject;

/**
 * Google App Engine datastore.
 *
 * <p>
 * See <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/package-summary.html">
 * The Datastore Java API(Low-level API)</a> for more details.
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Sep 7, 2011
 */
public interface GAERepository extends Repository {

    /**
     * The asynchronous version of interface {@linkplain #add(org.json.JSONObject)}.
     *
     * @param jsonObject the specified json object
     * @return the generated object id
     * @throws RepositoryException repository exception
     */
    String addAsync(final JSONObject jsonObject)
            throws RepositoryException;

    /**
     * The asynchronous version of interface 
     * {@linkplain #update(java.lang.String, org.json.JSONObject)}. 
     *
     * @param id the specified id
     * @param jsonObject the specified new json object
     * @throws RepositoryException repository exception
     */
    void updateAsync(final String id, final JSONObject jsonObject)
            throws RepositoryException;
}
