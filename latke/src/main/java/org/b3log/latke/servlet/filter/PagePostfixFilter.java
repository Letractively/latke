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

package org.b3log.latke.servlet.filter;

import org.b3log.latke.servlet.AbstractServletListener;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * *.do!
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.6, Jun 22, 2010
 */
public final class PagePostfixFilter implements Filter {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(
            PagePostfixFilter.class);
    /**
     * Filter configuration.
     */
    private FilterConfig filterConfig;

    /**
     * Filters the all non .do, .css, .jpg, .gif, .png, .js from the specified
     * request, excepts the
     * {@linkplain AbstractServletListener#getPostfixExceptionPaths()
     * specified paths}.
     *
     * @param request the specified request
     * @param response the specified response
     * @param chain filter chain
     * @throws IOException io exception
     * @throws ServletException servlet exception
     * @see AbstractServletListener#getPostfixExceptionPaths() 
     */
    @Override
    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpServletRequest =
                (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse =
                (HttpServletResponse) response;

        final String requestURI = httpServletRequest.getRequestURI();
        final String requestLocalName =
                httpServletRequest.getLocalName();
        LOGGER.trace("Request[URI=" + requestURI + ", localName="
                + requestLocalName + "]");

        final Set<String> postfixExceptionPaths =
                AbstractServletListener.getPostfixExceptionPaths();
        if (postfixExceptionPaths.contains(requestURI)) {
            LOGGER.trace("Excepts request[URI=" + requestURI + "] from "
                    + getClass().getSimpleName());
            chain.doFilter(request, response);

            return;
        }

        final String welcomePage = AbstractServletListener.getWelcomePage();
        Exception problem = null;

        final String uri = requestURI.toLowerCase();
        final boolean isDo = uri.endsWith(".do");
        final boolean isCSS = uri.endsWith(".css");
        final boolean isJPG = uri.endsWith(".jpg");
        final boolean isGIF = uri.endsWith(".gif");
        final boolean isPNG = uri.endsWith(".png");
        final boolean isJS = uri.endsWith(".js");
        final int idx = uri.lastIndexOf(".");
        String postfix = null;
        if (idx != -1) {
            postfix = requestURI.substring(idx, requestURI.length());
        } else {

            httpServletResponse.sendRedirect(welcomePage);

            return;
        }

        if (!isDo && !isCSS && !isJPG && !isGIF && !isPNG && !isJS) {
            problem = new Exception("The resource[postfix="
                    + postfix + "] you requested is "
                    + "illegal.");
            //sendProcessingError(problem, response);
            LOGGER.trace(problem.getMessage());
            httpServletResponse.sendRedirect(welcomePage);

            return;
        }

        chain.doFilter(request, response); // TODO: error-page in web.xml [404] has no effect
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }

    /**
     * {@inheritDoc}
     *
     * @param filterConfig filter config
     */
    @Override
    public void init(final FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            LOGGER.trace("Initializing filter " + toString());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @return filter config
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return getClass().getSimpleName();
        }

        return filterConfig.toString();
    }

    /**
     * Sends processing error.
     *
     * @param throwable a throwable object
     * @param response servlet response 
     */
    private void sendProcessingError(final Throwable throwable,
                                     final ServletResponse response) {
        final String stackTrace = getStackTrace(throwable);

        if (stackTrace != null && !stackTrace.equals("")) {
            try {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                final PrintStream ps =
                        new PrintStream(response.getOutputStream());
                final PrintWriter pw = new PrintWriter(ps);
                pw.print(
                        "<html><head><title>Error</title></head><body>");

                // TODO: Localize this for next official release
                pw.print(
                        "<h1>The resource did not process correctly</h1><pre>");
                pw.print(stackTrace);
                pw.print("</pre></body></html>");
                pw.close();
                ps.close();
                response.getOutputStream().close();
            } catch (final IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            try {
                final PrintStream ps = new PrintStream(
                        response.getOutputStream());
                throwable.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
            } catch (final IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Gets stack trace.
     *
     * @param throwable a throwable object
     * @return stack trace string
     */
    public String getStackTrace(final Throwable throwable) {
        String stackTrace = null;
        try {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return stackTrace;
    }
}
