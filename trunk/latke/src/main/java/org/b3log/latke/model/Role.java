/*
 * Copyright 2009, 2010, B3log
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b3log.latke.model;

/**
 * This class defines all role/group model relevant keys.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.4, May 27, 2010
 */
public final class Role {

    /**
     * Role.
     */
    public static final String ROLE = "role";
    /**
     * Default role.
     */
    public static final String DEFAULT_ROLE = "defaultRole";
    /**
     * Administrator role.
     */
    public static final String ADMIN_ROLE = "adminRole";
    /**
     * Roles.
     */
    public static final String ROLES = "roles";
    /**
     * Role id.
     */
    public static final String ROLE_ID = "roleId";
    /**
     * Role id SQL type.
     */
    public static final String ROLE_ID_SQL_TYPE = "CHAR";
    /**
     * Role id SQL type length.
     */
    public static final String ROLE_ID_SQL_TYPE_LENGTH = "20";
    /**
     * Role name.
     */
    public static final String ROLE_NAME = "roleName";
    /**
     * Role name SQL type.
     */
    public static final String ROLE_NAME_SQL_TYPE = "VARCHAR";
    /**
     * Role name SQL type length.
     */
    public static final String ROLE_NAME_SQL_TYPE_LENGTH = "10";
    /**
     * Update time of this role.
     */
    public static final String ROLE_UPDATE_TIME = "roleUpdateTime";
    /**
     * Role update time SQL type.
     */
    public static final String ROLE_UPDATE_TIME_SQL_TYPE = "DATETIME";
    /**
     * Role permission set.
     */
    public static final String ROLE_PERMISSION_SET = "rolePermissionSet";
    /**
     * Role permission set relation role id.
     */
    public static final String ROLE_PERMISSION_SET_RELATION_ROLE_ID =
            "rolePermissionSetRelationRoleId";
    /**
     * Role permission set relation role id SQL type.
     */
    public static final String ROLE_PERMISSION_SET_RELATION_ROLE_ID_SQL_TYPE = "CHAR";
    /**
     * Role permission set relation role id SQL type length.
     */
    public static final String ROLE_PERMISSION_SET_RELATION_ROLE_ID_SQL_TYPE_LENGTH = "20";
    /**
     * Role user id.
     */
    public static final String ROLE_USER_ID = "roleUserId";
    /**
     * Role user id SQL type.
     */
    public static final String ROLE_USER_ID_SQL_TYPE =
            User.USER_ID_SQL_TYPE;
    /**
     * Role user id SQL type length.
     */
    public static final String ROLE_USER_ID_SQL_TYPE_LEGNTH =
            User.USER_ID_SQL_TYPE_LENGTH;

    /**
     * Private default constructor.
     */
    private Role() {
    }
}
