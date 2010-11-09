/*
 * Copyright (c) 2009, 2010, B3log Team
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

package org.b3log.latke.jsonrpc.util;

import java.util.logging.Level;
import org.b3log.latke.jsonrpc.AbstractJSONRpcService;
import org.b3log.latke.servlet.AbstractServletListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Loads the services for JavaScript client.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.1.0, Aug 3, 2010
 */
public final class JSONRPCServiceClassLoader {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(JSONRPCServiceClassLoader.class.getName());

    /**
     * Private default constructor.
     */
    private JSONRPCServiceClassLoader() {
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
                    LOGGER.log(Level.FINEST, "Got class file[{0}]",
                               file.getPath());
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
        final ClassLoader classLoader = AbstractJSONRpcService.class.
                getClassLoader();

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
                className = className.replace("/", ".");
                // removes the .class extension of filename
                className = className.substring(0, className.lastIndexOf("."));

                LOGGER.log(Level.FINEST, "Loading class[name={0}]", className);

                final Class<?> clazz = classLoader.loadClass(className);

                ret.add(clazz);
            }
        } catch (final ClassNotFoundException e) {
            LOGGER.severe(e.getMessage());
        }

        return ret;
    }
}
