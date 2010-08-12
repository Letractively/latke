/*
 * Copyright (C) 2009, 2010, B3log Team
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
package org.b3log.latke.event;

import org.json.JSONObject;
import org.testng.annotations.Test;

/**
 * {@link EventManager} test case.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 12, 2010
 */
public final class EventManagerTestCase {

    /**
     *
     * @throws Exception exception
     */
    @Test
    public void test() throws Exception {
        final EventManager eventManager = EventManager.getInstance();
        eventManager.registerListener(new TestEventListener1());
        eventManager.registerListener(new TestEventListener2());

        final JSONObject eventData = new JSONObject();
        eventData.put("prop1", 1);

        eventManager.fireEventSynchronously(
                new Event<JSONObject>("Test", eventData));
    }

    /**
     * Test event listener 1.
     *
     * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
     * @version 1.0.0.0, Aug 12, 2010
     */
    private final class TestEventListener1
            extends AbstractEventListener<JSONObject> {

        @Override
        public void process(final Event<JSONObject> event) {
            System.out.println("Listener1 is processing a event[type="
                               + event.getType() + ", data=" + event.getData()
                               + "]");
        }
    }

    /**
     * Test event listener 2.
     *
     * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
     * @version 1.0.0.0, Aug 12, 2010
     */
    private final class TestEventListener2
            extends AbstractEventListener<JSONObject> {

        @Override
        public void process(final Event<JSONObject> event) {
            System.out.println("Listener2 is processing a event[type="
                               + event.getType() + ", data=" + event.getData()
                               + "]");
        }
    }
}
