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
package org.b3log.latke.image;

/**
 * A {@code Composite} represents a composition of an image onto a canvas. 
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 8, 2011
 */
public final class Composite {

    /**
     * Valid anchoring positions for a compositing operation. 
     * The anchor position of the image is aligned with the anchor position of 
     * the canvas. 
     * 
     * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
     * @version 1.0.0.0, Aug 8, 2011
     */
    public static enum Anchor {

        /**
         * BOTTOM_CENTER.
         */
        BOTTOM_CENTER,
        /**
         * BOTTOM_LEFT.
         */
        BOTTOM_LEFT,
        /**
         * BOTTOM_RIGHT.
         */
        BOTTOM_RIGHT,
        /**
         * CENTER_CENTER.
         */
        CENTER_CENTER,
        /**
         * CENTER_LEFT.
         */
        CENTER_LEFT,
        /**
         * CENTER_RIGHT.
         */
        CENTER_RIGHT,
        /**
         * TOP_CENTER.
         */
        TOP_CENTER,
        /**
         * TOP_LEFT.
         */
        TOP_LEFT,
        /**
         * TOP_RIGHT.
         */
        TOP_RIGHT
    }
}
