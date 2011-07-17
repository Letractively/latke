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

package org.b3log.latke.servlet;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.annotation.RequestProcessing;
import org.b3log.latke.annotation.RequestProcessor;

/**
 * Request processor utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jul 17, 2011
 */
public final class RequestProcessors {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(RequestProcessors.class.getName());
    /**
     * Processor methods.
     */
    private static Set<ProcessorMethod> processorMethods =
            new HashSet<ProcessorMethod>();

    /**
     * Scans classpath to discover request processor classes via annotation
     * {@linkplain org.b3log.latke.annotation.RequestProcessor}.
     */
    // TODO: only WEB-INF/classes at present
    public static void discover() {
        final String webRoot = AbstractServletListener.getWebRoot();
        final File classesDir = new File(webRoot + File.separator + "WEB-INF"
                                         + File.separator
                                         + "classes" + File.separator);
        @SuppressWarnings("unchecked")
        final Collection<File> classes =
                FileUtils.listFiles(classesDir, new String[]{"class"}, true);
        final ClassLoader classLoader = RequestProcessors.class.getClassLoader();

        try {
            for (final File classFile : classes) {
                final String path = classFile.getPath();
                final String className =
                        StringUtils.substringBetween(path, "WEB-INF"
                                                           + File.separator
                                                           + "classes"
                                                           + File.separator,
                                                     ".class").
                        replaceAll("\\/", ".").replaceAll("\\\\", ".");
                final Class<?> clz = classLoader.loadClass(className);

                if (clz.isAnnotationPresent(RequestProcessor.class)) {
                    LOGGER.log(Level.FINER,
                               "Found a request processor[className={0}]",
                               className);
                    final Method[] declaredMethods = clz.getDeclaredMethods();
                    for (int i = 0; i < declaredMethods.length; i++) {
                        final Method mthd = declaredMethods[i];
                        final RequestProcessing annotation =
                                mthd.getAnnotation(RequestProcessing.class);

                        if (null == annotation) {
                            continue;
                        }

                        addProcessorMethod(annotation, clz, mthd);
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Scans classpath failed", e);
        }
    }

    /**
     * Gets process method for the specified request URI and method.
     * 
     * @param requestURI the specified request URI
     * @param method the specified method
     * @return process method, returns {@code null} if not found
     */
    public static Method getProcessMethod(final String requestURI,
                                          final String method) {
        // TODO: Ant-style path pattern matching, caching
        for (final ProcessorMethod processorMethod : processorMethods) {
            if (method.equals(processorMethod.getMethod())
                && requestURI.equals(processorMethod.getUriPattern())) {
                return processorMethod.getProcessorMethod();
            }
        }

        return null;
    }

    /**
     * Adds processor method by the specified annotation, class and method.
     * 
     * @param requestProcessing the specified annotation
     * @param clz the specified class
     * @param method the specified method 
     */
    private static void addProcessorMethod(
            final RequestProcessing requestProcessing, final Class<?> clz,
            final Method method) {
        final String[] uriPatterns = requestProcessing.value();

        for (int i = 0; i < uriPatterns.length; i++) {
            final String uriPattern = uriPatterns[i];
            final RequestMethod[] requestMethods =
                    requestProcessing.method();

            for (int j = 0; j < requestMethods.length; j++) {
                final RequestMethod requestMethod = requestMethods[j];

                final ProcessorMethod processorMethod =
                        new ProcessorMethod();
                processorMethods.add(processorMethod);

                processorMethod.setMethod(requestMethod.name());
                processorMethod.setUriPattern(uriPattern);
                processorMethod.setProcessorClass(clz);
                processorMethod.setProcessorMethod(method);
            }
        }
    }

    /**
     * Default private constructor.
     */
    private RequestProcessors() {
    }
}

/**
 * Request processor method.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jul 17, 2011
 */
final class ProcessorMethod {

    /**
     * URI path pattern.
     */
    private String uriPattern;
    /**
     * Request method.
     */
    private String method;
    /**
     * Class.
     */
    private Class<?> processorClass;
    /**
     * Method.
     */
    private Method processorMethod;

    /**
     * Gets method.
     * 
     * @return method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the method with the specified method.
     * 
     * @param method the specified method
     */
    public void setMethod(final String method) {
        this.method = method;
    }

    /**
     * Gets the processor class.
     * 
     * @return processor class
     */
    public Class<?> getProcessorClass() {
        return processorClass;
    }

    /**
     * Sets the processor class with the specified processor class.
     * 
     * @param processorClass the specified processor class
     */
    public void setProcessorClass(final Class<?> processorClass) {
        this.processorClass = processorClass;
    }

    /**
     * Gets the processor method.
     * 
     * @return processor method
     */
    public Method getProcessorMethod() {
        return processorMethod;
    }

    /**
     * Sets the processor method with the specified processor method.
     * 
     * @param processorMethod the specified processor method
     */
    public void setProcessorMethod(final Method processorMethod) {
        this.processorMethod = processorMethod;
    }

    /**
     * Gets the URI pattern.
     * 
     * @return URI pattern
     */
    public String getUriPattern() {
        return uriPattern;
    }

    /**
     * Sets the URI pattern with the specified URI pattern.
     * 
     * @param uriPattern the specified URI pattern
     */
    public void setUriPattern(final String uriPattern) {
        this.uriPattern = uriPattern;
    }
}