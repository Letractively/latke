/*
 * Copyright (c) 2009, 2010, 2011, 2012, B3log Team
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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link StaticResources} test case.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Feb 27, 2012
 */
public class StaticResourcesTestCase {

    /**
     * Tests method {@link StaticResources#isStatic(java.lang.String)}.
     */
    @Test
    public void isStatic() {
        Assert.assertTrue(StaticResources.isStatic("/css/test.css"));
        Assert.assertTrue(StaticResources.isStatic("/images/test.jpg"));
        Assert.assertFalse(StaticResources.isStatic("/test.notExist"));
        Assert.assertFalse(StaticResources.isStatic("/images/test"));
    }
}