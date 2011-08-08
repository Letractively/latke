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

import java.util.Collection;

/**
 * Image service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 8, 2011
 */
public interface ImageService {

    /**
     * Makes an image with the specified data.
     * 
     * @param data the specified data
     * @return image
     */
    Image makeImage(final byte[] data);

    /**
     * Creates an image composition with the specified image, x offset, y offset,
     * opacity and anchor.
     * 
     * @param image the specified image
     * @param xOffset the specified x offset
     * @param yOffset the specified y offset
     * @param opacity the specified opacity
     * @param anchor the specified anchor
     * @return image composition
     */
    Composite makeComposite(final Image image,
                            final int xOffset, final int yOffset,
                            final float opacity, final Composite.Anchor anchor);

    /**
     * Applies the specified composites using a canvas with 
     * dimensions determined by the specified width, height and background color. 
     * Uses PNG as its output encoding. 
     * 
     * @param composites the specified composites
     * @param width the specified width
     * @param height the specified height
     * @param color the specified background color
     * @return image
     */
    Image composite(final Collection<Composite> composites,
                    final int width, final int height, final long color);
}
