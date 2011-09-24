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

import org.b3log.latke.user.GeneralUser;
import org.b3log.latke.user.UserService;

/**
 * Local user service.
 *
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Sep 24, 2011
 */
public final class LocalUserService implements UserService {

    @Override
    public GeneralUser getCurrentUser() {
        final GeneralUser ret = new GeneralUser();

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isUserLoggedIn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isUserAdmin() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String createLoginURL(final String destinationURL) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String createLogoutURL(final String destinationURL) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
