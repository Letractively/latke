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
package org.b3log.latke.user;

/**
 * User service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 8, 2011
 */
public interface UserService {

    /**
     * Gets the current logged in user.
     * 
     * @return user if the user is logged in, return {@code null} otherwise
     */
    GeneralUser getCurrentUser();

    /**
     * Determines whether the user logged in.
     * 
     * @return {@code true} if the user logged in, returns {@code false} 
     * otherwise
     */
    boolean isUserLoggedIn();

    /**
     * Determines whether the user is administrator.
     * 
     * @return {@code true} if the user is administrator, returns 
     * {@code false} otherwise
     */
    boolean isUserAdmin();

    /**
     * Creates login URL with the specified destination URL (redirect to the URL
     * if login successfully).
     * 
     * @param destinationURL the specified destination URL
     * @return login URL
     */
    String createLoginURL(final String destinationURL);

    /**
     * Creates logout URL with the specified destination URL (redirect to the URL
     * if logout successfully).
     * 
     * @param destinationURL the specified destination URL
     * @return login URL
     */
    String createLogoutURL(final String destinationURL);
}
