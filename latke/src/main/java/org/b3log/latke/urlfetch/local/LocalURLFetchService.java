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
package org.b3log.latke.urlfetch.local;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.HTTPResponse;
import org.b3log.latke.urlfetch.URLFetchService;

/**
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 0.0.0.2, Aug 15, 2011
 * 
 */
public class LocalURLFetchService implements URLFetchService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LocalURLFetchService.class.getName());

    @Override
    public HTTPResponse fetch(final HTTPRequest request) throws IOException {

        try {
            final HTTPRequestMethod requestMethod = request.getRequestMethod();
            if (requestMethod == null) {
                throw new ServiceException(" requestMethod  for URLFetch should not be null");
            }

            return UrlFetchHandlerFactory.getFetchHandler(requestMethod).doFetch(request);

        } catch (final ServiceException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return null;
        }

    }

    @Override
    public Future<?> fetchAsync(final HTTPRequest request) {

        final FutureTask<HTTPResponse> futureTask = new FutureTask<HTTPResponse>(
                new Callable<HTTPResponse>() {

                    @Override
                    public HTTPResponse call() throws Exception {
                        return fetch(request);
                    }
                });

        return futureTask;

    }

}
