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
package org.b3log.latke.user.local;

import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.Keys;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.user.GeneralUser;
import org.b3log.latke.user.UserService;
import org.b3log.latke.util.Sessions;
import org.json.JSONObject;

/**
 * Local user service.
 *
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Sep 24, 2011
 */
public final class LocalUserService implements UserService {

    @Override
    public GeneralUser getCurrentUser(final HttpServletRequest request) {
        final JSONObject currentUser = Sessions.currentUser(request);
        if (null == currentUser) {
            return null;
        }

        final GeneralUser ret = new GeneralUser();
        ret.setEmail(currentUser.optString(User.USER_EMAIL));
        ret.setId(currentUser.optString(Keys.OBJECT_ID));
        ret.setNickname(currentUser.optString(User.USER_NAME));

        return ret;
    }

    @Override
    public boolean isUserLoggedIn(final HttpServletRequest request) {
        return null != Sessions.currentUser(request);
    }

    @Override
    public boolean isUserAdmin(final HttpServletRequest request) {
        final JSONObject currentUser = Sessions.currentUser(request);

        if (null == currentUser) {
            return false;
        }

        return Role.ADMIN_ROLE.equals(currentUser.optString(User.USER_ROLE));
    }

    @Override
    public String createLoginURL(final String destinationURL) {
        return "/login?goto=" + destinationURL;
    }

    @Override
    public String createLogoutURL(final String destinationURL) {
        return "/logout?goto=" + destinationURL;
    }
}