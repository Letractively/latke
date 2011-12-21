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
package org.b3log.latke.repository.jdbc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.b3log.latke.repository.Repositories;
import org.b3log.latke.repository.RepositoryException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JdbcRepositories utilities.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Dec 20, 2011
 */
public final class JdbcRepositories {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(JdbcRepositories.class.getName());

    /**
     * /** to json "repositories".
     */
    private static final String REPOSITORIES = "repositories";

    /**
     * /** to json "name".
     */
    private static final String NAME = "name";

    /**
     * /** to json "keys".
     */
    private static final String KEYS = "keys";

    /**
     * /** to json "type".
     */
    private static final String TYPE = "type";

    /**
     * /** to json "nullable".
     */
    private static final String NULLABLE = "nullable";

    /**
     * /** to json "length".
     */
    private static final String LENGTH = "length";

    /**
     * ** to json "iskey".
     */
    private static final String ISKEY = "iskey";

    /**
     * the default keyname.
     */
    private static final String OID = "oId";

    /**
     * store all repository filed definition in a Map.
     * <p>
     * key: the name of the repository value: list of all the FieldDefinition
     * </p>
     */
    private static Map<String, List<FieldDefinition>> repositoriesMap = null;

    /**
     * get the RepositoriesMap ,lazy load.
     * 
     * @return Map<String, List<FieldDefinition>>
     */
    public static Map<String, List<FieldDefinition>> getRepositoriesMap() {

        if (repositoriesMap == null) {

            try {
                initRepositoriesMap();
            } catch (final Exception e) {

                LOGGER.log(Level.SEVERE,
                        "initRepositoriesMap mistake " + e.getMessage(), e);
            }
        }

        return repositoriesMap;

    }

    /**
     * init the repositoriesMap.
     * 
     * @throws JSONException JSONException
     * @throws RepositoryException RepositoryException
     */
    private static void initRepositoriesMap() throws JSONException,
            RepositoryException {

        final JSONObject jsonObject = Repositories.getRepositoriesDescription();

        if (jsonObject == null) {

            LOGGER.warning("the repository description[repository.json]");
            return;
        }

        jsonToRepositoriesMap(jsonObject);

    }

    /**
     * analysis json data structure to java Map structure.
     * 
     * @param jsonObject json Model
     * @throws JSONException JSONException
     */
    public static void jsonToRepositoriesMap(final JSONObject jsonObject)
            throws JSONException {

        repositoriesMap = new HashMap<String, List<FieldDefinition>>();

        final JSONArray repositoritArray = jsonObject
                .getJSONArray(REPOSITORIES);

        JSONObject repositoritObject = null;
        JSONObject fieldDefinitionObject = null;

        for (int i = 0; i < repositoritArray.length(); i++) {

            repositoritObject = repositoritArray.getJSONObject(i);
            final String repositoryName = repositoritObject.getString(NAME);

            final List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();
            repositoriesMap.put(repositoryName, fieldDefinitions);

            final JSONArray keysJsonArray = repositoritObject
                    .getJSONArray(KEYS);

            FieldDefinition definition = null;
            for (int j = 0; j < keysJsonArray.length(); j++) {
                fieldDefinitionObject = keysJsonArray.getJSONObject(i);
                definition = fillFieldDefinitionData(fieldDefinitionObject);
                fieldDefinitions.add(definition);
            }

        }

    }

    /**
     * fillFieldDefinitionData.
     * 
     * @param fieldDefinitionObject josn model
     * @return {@link FieldDefinition}
     * @throws JSONException JSONException
     */
    private static FieldDefinition fillFieldDefinitionData(
            final JSONObject fieldDefinitionObject) throws JSONException {

        final FieldDefinition fieldDefinition = new FieldDefinition();
        fieldDefinition.setName(fieldDefinitionObject.getString(NAME));
        fieldDefinition.setType(fieldDefinitionObject.getString(TYPE));
        fieldDefinition.setNullable(fieldDefinitionObject.optBoolean(NULLABLE,
                true));
        fieldDefinition.setLength(fieldDefinitionObject.optInt(LENGTH));
        fieldDefinition.setIsKey(fieldDefinitionObject.optBoolean(ISKEY));

        /**
         * the default key name is 'old'.
         */
        if (OID.equals(fieldDefinition.getName())) {
            fieldDefinition.setIsKey(true);
        }

        return fieldDefinition;

    }

    /**
     * Private constructor.
     */
    private JdbcRepositories() {

    }

}
