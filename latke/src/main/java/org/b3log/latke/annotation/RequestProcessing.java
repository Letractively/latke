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
package org.b3log.latke.annotation;

import org.b3log.latke.servlet.RequestMethod;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated method for HTTP servlet request processing.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Jul 16, 2011
 * @see RequestProcessor
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestProcessing {

    /**
     * The dispatching URI path patterns of a request.
     * 
     * <p>
     * Semantics of these values adapting to the URL patterns 
     * (&lt;url-pattern/&gt;) configures in 
     * web application descriptor (web.xml) of a servlet. Ant-style path 
     * patterns are also supported.
     * </p>
     */
    String[] value() default {};

    /**
     * The HTTP request methods the annotated method should process.
     */
    RequestMethod[] method() default {RequestMethod.GET};
}
