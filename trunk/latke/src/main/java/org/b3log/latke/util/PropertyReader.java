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
package org.b3log.latke.util;


import java.io.IOException;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Read  properties from the directory WEB-INF/classes/* .
 * @author <a href="mailto:jiangzezhou1989@gmail.com">zezhou jiang</a>
 * @version 0.0.0.1, Aug 8, 2011
 */
public final class PropertyReader {

    /**
     * Retrieve the properties specified by the fileName.
     * The property file should be in the specified WEB-INF/classes/* 
     * @param fileName relative path to a properties file in the  WEB-INF/classes/* 
     * @return a <code>Properties</code> object based on the input file  
     */
    public static Properties getProperties(final String fileName) {
      
        Properties prop = null;
        final InputStream in = PropertyReader.class
                .getClassLoader()
                .getResourceAsStream(fileName);
       
        try {
            prop = new Properties();
            prop.load(in);
        } catch (final IOException ex) {
            Logger.getLogger(PropertyReader.class.getName(), ex.getMessage());
        }
        return prop;
    }
    
 
    
    /**
     * Private default constructor.
     */
    private PropertyReader() {
        
    }
}
