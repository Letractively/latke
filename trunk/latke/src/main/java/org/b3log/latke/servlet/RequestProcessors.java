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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.annotation.RequestProcessing;
import org.b3log.latke.annotation.RequestProcessor;
import org.b3log.latke.util.AntPathMatcher;

/**
 * Request processor utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.4, Sep 26, 2011
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
     * Processors.
     */
    private static Map<Method, Object> processors =
            new HashMap<Method, Object>();

    /**
     * Invokes a processor method with the specified request URI, method and 
     * context.
     * 
     * @param requestURI the specified request URI
     * @param method the specified method
     * @param context the specified context
     * @return invoke result, returns {@code null} if invoke failed
     */
    public static Object invoke(final String requestURI, final String method,
                                final HTTPRequestContext context) {
        final ProcessorMethod processMethod = getProcessorMethod(requestURI,
                                                                 method);

        if (null == processMethod) {
            LOGGER.log(Level.WARNING,
                       "Can not find process method for request[requestURI={0}, method={1}]",
                       new Object[]{requestURI, method});
            return null;
        }

        final Method processorMethod = processMethod.getProcessorMethod();
        Object processorObject = processors.get(processorMethod);

        try {
            if (null == processorObject) {
                final Class<?> processorClass =
                        processMethod.getProcessorClass();
                final Object instance = processorClass.newInstance();

                processors.put(processorMethod, instance);

                processorObject = instance;
            }

            return processorMethod.invoke(processorObject, context);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Invokes processor method failed", e);

            return null;
        }
    }

    /**
     * Scans classpath to discover request processor classes via annotation
     * {@linkplain org.b3log.latke.annotation.RequestProcessor}.
     */
    // XXX: only WEB-INF/classes at present, to consider WEB/INF/lib?
    public static void discover() {
        discoverFromClassesDir();
        discoverFromLibDir();
    }

    /**
     * Scans classpath (classes directory) to discover request processor classes.
     */
    private static void discoverFromClassesDir() {
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
            LOGGER.log(Level.SEVERE,
                       "Scans classpath (classes directory) failed", e);
        }
    }

    /**
     * Scans classpath (lib directory) to discover request processor classes.
     */
    private static void discoverFromLibDir() {
        final String webRoot = AbstractServletListener.getWebRoot();
        final File libDir = new File(webRoot + File.separator + "WEB-INF"
                                     + File.separator + "lib" + File.separator);
        @SuppressWarnings("unchecked")
        final Collection<File> files =
                FileUtils.listFiles(libDir, new String[]{"jar"}, true);

        final ClassLoader classLoader = RequestProcessors.class.getClassLoader();

        try {
            for (final File file : files) {
                final String path = file.getPath();
                if (path.contains("appengine")
                    || path.contains("freemarker")
                    || path.contains("commons")
                    || path.contains("jabsorb")
                    || path.contains("jsoup")
                    || path.contains("xml")) { // XXX: urgly....
                    continue; // Skips GAE
                }

                final JarFile jarFile = new JarFile(path);

                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry jarEntry = entries.nextElement();
                    String className = jarEntry.getName();

                    LOGGER.info(":class: " + className);

                    if (className.contains("$")
                        || !className.endsWith(".class")
                        || className.contains("META-INF")) {
                        continue; // Skips inner class
                    }

                    className = StringUtils.substringBefore(className, ".");
                    className = className.replaceAll("/", ".");

                    Class<?> clz = null;
                    try {
                        clz = classLoader.loadClass(className);
                    } catch (final ClassNotFoundException e) {
                        continue; // Ignores....
                    }

                    if (clz.isAnnotationPresent(RequestProcessor.class)) {
                        LOGGER.log(Level.FINER,
                                   "Found a request processor[className={0}]",
                                   className);
                        final Method[] declaredMethods =
                                clz.getDeclaredMethods();
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
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE,
                       "Scans classpath (classes directory) failed", e);

        }
    }

    /**
     * Gets process method for the specified request URI and method.
     * 
     * @param requestURI the specified request URI
     * @param method the specified method
     * @return process method, returns {@code null} if not found
     */
    private static ProcessorMethod getProcessorMethod(final String requestURI,
                                                      final String method) {
        final List<ProcessorMethod> matches = new ArrayList<ProcessorMethod>();
        int i = 0;
        for (final ProcessorMethod processorMethod : processorMethods) {
            if (method.equals(processorMethod.getMethod())) {
                if (requestURI.equals(processorMethod.getURIPattern())) {
                    return processorMethod;
                }

                if (AntPathMatcher.match(processorMethod.getURIPattern(),
                                         requestURI)) {
                    i++;
                    matches.add(processorMethod);
                }
            }
        }

        if (i > 1) {
            final StringBuilder stringBuilder = new StringBuilder(
                    "Can not determine request method for configured methods[");
            final Iterator<ProcessorMethod> iterator = matches.iterator();
            while (iterator.hasNext()) {
                final ProcessorMethod processMethod = iterator.next();

                stringBuilder.append("[className=");
                stringBuilder.append(processMethod.getProcessorMethod().
                        getDeclaringClass().getSimpleName());
                stringBuilder.append(", methodName=");
                stringBuilder.append(
                        processMethod.getProcessorMethod().getName());
                stringBuilder.append(", patterns=");
                stringBuilder.append(processMethod.getURIPattern());
                stringBuilder.append("]");

                if (iterator.hasNext()) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append("]");

            LOGGER.warning(stringBuilder.toString());
        }

        return matches.get(0);
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
            final HTTPRequestMethod[] requestMethods =
                    requestProcessing.method();

            for (int j = 0; j < requestMethods.length; j++) {
                final HTTPRequestMethod requestMethod = requestMethods[j];

                final ProcessorMethod processorMethod =
                        new ProcessorMethod();
                processorMethods.add(processorMethod);

                processorMethod.setMethod(requestMethod.name());
                processorMethod.setURIPattern(uriPattern);
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
    public String getURIPattern() {
        return uriPattern;
    }

    /**
     * Sets the URI pattern with the specified URI pattern.
     * 
     * @param uriPattern the specified URI pattern
     */
    public void setURIPattern(final String uriPattern) {
        this.uriPattern = uriPattern;
    }
}