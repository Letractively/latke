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
package org.b3log.latke.service;

import java.util.Map;
import org.json.JSONObject;

/**
 * Abstract service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.4, Jun 15, 2010
 */
public abstract class AbstractService {

    /**
     * Creates data in database with the specified input.
     *
     * @param input the specified input
     * @return creation result
     * @throws ServiceException service exception
     */
    public JSONObject create(final JSONObject input) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Counts with the specified input.
     *
     * @param input the specified input
     * @return the size of the specified entities. For example: <pre>{
     *     "logsSize": 12
     * }</pre>
     * @throws ServiceException service exception
     */
    public JSONObject count(final JSONObject input) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Retrieves data with the specified input.
     *
     * @param input the specified input
     * @return retrieved data, if not found the data, returns a new(empty)
     * result set
     * @throws ServiceException service exception
     */
    public JSONObject retrieve(final JSONObject input) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Updates data with the specified input.
     *
     * @param input the specified input
     * @return updating result
     * @throws ServiceException service exception
     */
    public JSONObject update(final JSONObject input) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Deletes data with the specified input.
     *
     * @param input the specified input
     * @return deleting result
     * @throws ServiceException service exception
     */
    public JSONObject delete(final JSONObject input) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Determins the specified input exists in database or not.
     *
     * @param input the specified input
     * @return {@code true} if exists, otherwise, returns {@code false}
     * @throws ServiceException service exception
     */
    public boolean exist(final JSONObject input) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Fills <a href="http://www.freemarker.org">FreeMarker</a> template by the
     * specified data model.
     *
     * @param dataModel the specified data model
     * @throws ServiceException service exception
     */
    public void fill(final Map<String, Object> dataModel)
            throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
