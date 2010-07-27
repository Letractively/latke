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

package org.b3log.latke.servlet.filter;

import org.b3log.latke.client.Sessions;
import org.b3log.latke.servlet.AbstractServletListener;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Authentication filter.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Jun 23, 2010
 */
public final class AuthenticationFilter implements Filter {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AuthenticationFilter.class);

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Filters the has not logged in user for the specified request. Redirects
     * it to welcome page.
     *
     * @param request the specified request
     * @param response the specified response
     * @param chain filter chain
     * @throws IOException io exception
     * @throws ServletException servlet exception
     * @see AbstractServletListener#getWelcomePage() 
     */
    @Override
    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain) throws IOException,
                                                         ServletException {
        final HttpServletRequest httpServletRequest =
                (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse =
                (HttpServletResponse) response;

        if (!hasLoggedIn(httpServletRequest)) {
            LOGGER.info("Authenticate fail, redirect to welcome page");
            sendRedirectToWelcomePage(httpServletResponse);

            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Determines whether the user for the specified request has logged in.
     *
     * @param request the specified request
     * @return {@code true} if logged in, {@code false} otherwise
     */
    private boolean hasLoggedIn(final HttpServletRequest request) {
        final String requestURI = request.getRequestURI();
        final String requestURL = request.getRequestURL().toString();
        LOGGER.trace("Request[URI=" + requestURI + ", URL=" + requestURL + "]");

        JSONObject user = null;
        final String welcomePage = AbstractServletListener.getWelcomePage();

        user = Sessions.currentUser(request);
        LOGGER.debug("Session[user=" + user + "]");

        if (null == user && !requestURI.endsWith(welcomePage)) {
            LOGGER.debug("Redirect to welcome page");
            return false;
        }

        return true;
    }

    /**
     * Sends redirect to welcome page for the specified response.
     *
     * @param response the specified response
     * @throws IOException io exception
     */
    private void sendRedirectToWelcomePage(final HttpServletResponse response)
            throws IOException {
        final String welcomePage = AbstractServletListener.getWelcomePage();

        response.sendRedirect(welcomePage);
    }
}
