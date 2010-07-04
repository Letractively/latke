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

package org.b3log.latke.client.util;

import org.b3log.latke.client.remote.AbstractRemoteService;
import org.b3log.latke.servlet.AbstractServletListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Loads the services for JavaScript client.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.4, May 4, 2010
 */
public final class RemoteJSServiceClassLoader {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(RemoteJSServiceClassLoader.class);

    /**
     * Private default constructor.
     */
    private RemoteJSServiceClassLoader() {
    }

    /**
     * Loads remote JavaScript service classes with the specified class
     * directory path.
     *
     * @param classesDirPath the specified class directory path.
     * @return a list of classes
     */
    public static List<Class<?>> loadServiceClasses(
            final String classesDirPath) {
        final File remoteJSServiceClassesDir = new File(classesDirPath);

        final List<File> classFiles = getClassFiles(remoteJSServiceClassesDir);
        final List<Class<?>> ret = loadClasses(classFiles);

        return ret;
    }

    /**
     * Gets class files with the specified class directory.
     *
     * @param classesDir the specified class directory
     * @return a list of class files
     */
    private static List<File> getClassFiles(final File classesDir) {
        final List<File> ret = new ArrayList<File>();
        final File[] files = classesDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File file) {
//                if (file.isDirectory() || file.getName().endsWith(".class")) {
                return true;
//                } else {
//                    return false;
//                }
            }
        });

        if (null != files) {
            for (int i = 0; i < files.length; i++) {
                final File file = files[i];
                if (file.isFile()) {
                    ret.add(file);
                    LOGGER.trace("Got class file[" + file.getPath() + "]");
                } else if (file.isDirectory()) {
                    ret.addAll(getClassFiles(file));
                }
            }
        }

        return ret;
    }

    /**
     * Loads classes with the specified class files.
     *
     * @param classFiles the specified class files
     * @return a list of classes
     */
    private static List<Class<?>> loadClasses(final List<File> classFiles) {
        final List<Class<?>> ret = new ArrayList<Class<?>>();
        final ClassLoader classLoader = AbstractRemoteService.class.getClassLoader();

        try {
            for (final File classFile : classFiles) {
                final String fullPath = classFile.getPath();
                final String packageAndClassPath =
                        AbstractServletListener.getClientRemoteServicePackage()
                        + fullPath.substring(
                        fullPath.lastIndexOf(File.separator));
                String className = packageAndClassPath.replaceAll(
                        File.separator.equals("\\") ? "\\\\" : // for windows
                        File.separator, // others OS
                        ".");
                // removes the .class extension of filename
                className = className.substring(0, className.lastIndexOf("."));

                LOGGER.trace("Loading class[name=" + className + "]");

                final Class<?> clazz = classLoader.loadClass(className);

                ret.add(clazz);
            }
        } catch (final ClassNotFoundException e) {
            LOGGER.fatal(e.getMessage(), e);
        }

        return ret;
    }
}
