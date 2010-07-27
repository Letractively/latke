/*
 * Copyright (C) 2009, 2010, B3log Team
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

package org.b3log.latke.client;

import org.b3log.latke.model.User;
import org.b3log.latke.util.Strings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.b3log.latke.Keys;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Session utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.5, Jun 23, 2010
 */
public final class Sessions {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Sessions.class);

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
        String userId = null;

        final HttpSession session = request.getSession(false);

        try {
            if (null != session) {
                final JSONObject user = (JSONObject) session.getAttribute(
                        User.USER);
                userId = user.optString(Keys.OBJECT_ID);
                userId = Strings.isEmptyOrNull(userId)
                        ? user.getString(User.USER_NAME) : userId;
            }
        } catch (final JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }

        boolean ret = false;
        if (null != userId) {
            session.invalidate();
            ret = true;
        }

        return ret;
    }

    /**
     * Gets the current logged in user with the specified request.
     *
     * @param request the specified request
     * @return the current user or {@code null}
     */
    public static JSONObject currentUser(final HttpServletRequest request) {
        JSONObject ret = null;
        final HttpSession session = request.getSession(false);

        if (null != session) {
            ret = (JSONObject) session.getAttribute(User.USER);
        }

        return ret;
    }
}
