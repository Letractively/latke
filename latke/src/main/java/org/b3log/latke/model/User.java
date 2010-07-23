/*
 * Copyright 2009, 2010, B3log Team
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
package org.b3log.latke.model;

/**
 * This class defines all user model relevant keys.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.6, Jul 23, 2010
 */
public final class User {

    /**
     * User.
     */
    public static final String USER = "user";
    /**
     * Users.
     */
    public static final String USERS = "users";
    /**
     * User name.
     */
    public static final String USER_NAME = "userName";
    /**
     * User password.
     */
    public static final String USER_PASSWORD = "userPassword";
    /**
     * User new password.
     */
    public static final String USER_NEW_PASSWORD = "userNewPassword";
    /**
     * Update time of this user.
     */
    public static final String USER_UPDATE_TIME = "userUpdateTime";
    /**
     * User role.
     */
    public static final String USER_ROLE = "userRole";
    // Relations ///////////////////////////////////////////////////////////////
    /**
     * {@linkplain Role#ROLE_ID Role id} of this user.
     */
    public static final String USER_ROLE_ID = "userRoleId";
    // End of Relations ////////////////////////////////////////////////////////

    /**
     * Private default constructor.
     */
    private User() {
    }
}
