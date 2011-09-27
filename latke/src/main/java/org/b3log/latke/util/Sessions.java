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
package org.b3log.latke.util;

import java.util.logging.Logger;
import org.b3log.latke.model.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

/**
 * Session utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.6, Aug 15, 2010
 */
public final class Sessions {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(Sessions.class.getName());

    /**
     * Private default constructor.
     */
    private Sessions() {
    }

    /**
     * Logins the specified user from the specified request.
     *
     * @param request the specified request
     * @param user the specified user
     */
    public static void login(final HttpServletRequest request,
                             final JSONObject user) {
        final HttpSession session = request.getSession();

        session.setAttribute(User.USER, user);
    }

    /**
     * Logouts a user with the specified request.
     *
     * @param request the specified request
     * @return {@code true} if succeed, otherwise returns {@code false}
     */
    public static boolean logout(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (null != session) {
            session.invalidate();

            return true;
        }

        return false;
    }

    /**
     * Gets the current user with the specified request.
     * 
     * @param request the specified request
     * @return the current user, returns {@code null} if not logged in 
     */
    public static JSONObject currentUser(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (null != session) {
            return (JSONObject) session.getAttribute(User.USER);
        }

        return null;
    }

    /**
     * Gets the current logged in user password with the specified request.
     *
     * @param request the specified request
     * @return the current user password or {@code null}
     */
    public static String currentUserPwd(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (null != session) {
            final JSONObject user = (JSONObject) session.getAttribute(User.USER);

            return user.optString(User.USER_PASSWORD);
        }

        return null;
    }

    /**
     * Gets the current logged in user name with the specified request.
     *
     * @param request the specified request
     * @return the current user name or {@code null}
     */
    public static String currentUserName(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (null != session) {
            final JSONObject user = (JSONObject) session.getAttribute(User.USER);

            return user.optString(User.USER_NAME);
        }

        return null;
    }

    /**
     * Gets the current logged in user email with the specified request.
     *
     * @param request the specified request
     * @return the current user name or {@code null}
     */
    public static String currentUserEmail(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (null != session) {
            final JSONObject user = (JSONObject) session.getAttribute(User.USER);

            return user.optString(User.USER_EMAIL);
        }

        return null;
    }
}
