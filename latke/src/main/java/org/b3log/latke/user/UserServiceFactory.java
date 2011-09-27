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

import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeEnv;

/**
 * User service factory.
 * 
 * <p>Always prepare {@link org.b3log.latke.user.local.LocalUserService local version} 
 * of user service,regardless of {@link RuntimeEnv runtime environment}.</p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Sep 27, 2011
 */
public final class UserServiceFactory {

    /**
     * User service.
     */
    private static final UserService USER_SERVICE;

    static {
        final RuntimeEnv runtimeEnv = Latkes.getRuntimeEnv();

        try {
            Class<UserService> serviceClass = null;

            switch (runtimeEnv) {
                case GAE: // GAE & Local use the same implementation
                case LOCAL:
                    serviceClass =
                            (Class<UserService>) Class.forName(
                            "org.b3log.latke.user.local.LocalUserService");
                    USER_SERVICE = serviceClass.newInstance();
                    break;
                /*
                serviceClass =
                (Class<UserService>) Class.forName(
                "org.b3log.latke.user.gae.GAEUserService");
                USER_SERVICE = serviceClass.newInstance();
                break;
                 */
                default:
                    throw new RuntimeException(
                            "Latke runs in the hell.... "
                            + "Please set the enviornment correctly");
            }
        } catch (final Exception e) {
            throw new RuntimeException("Can not initialize User Service!", e);
        }
    }

    /**
     * Gets user service (always be an instance of 
     * {@link org.b3log.latke.user.local.LocalUserService}).
     * 
     * @return user service
     */
    public static UserService getUserService() {
        return USER_SERVICE;
    }

    /**
     * Private default constructor.
     */
    private UserServiceFactory() {
    }
}
