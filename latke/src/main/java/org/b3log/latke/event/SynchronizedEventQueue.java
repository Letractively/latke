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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Synchronized event queue.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 12, 2010
 */
final class SynchronizedEventQueue extends Observable {

    /**
     * Synchronized event queue.
     */
    private Map<String, List<Event<?>>> synchronizedEvents =
            new HashMap<String, List<Event<?>>>();
    /**
     * Event manager.
     */
    private EventManager eventManager;

    /**
     * Constructs a {@link SynchronizedEventQueue} object with the specified
     * event manager.
     *
     * @param eventManager the specified event manager
     */
    SynchronizedEventQueue(final EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Fires the specified event.
     *
     * @param event the specified event
     */
    synchronized void fireEvent(final Event<?> event) {
        final String eventType = event.getType();
        List<Event<?>> events = synchronizedEvents.get(eventType);
        if (null == events) {
            events = new ArrayList<Event<?>>();
            synchronizedEvents.put(eventType, events);
        }

        events.add(event);
        setChanged();
        notifyObservers(event);
    }

    /**
     * Removes the specified event from this event queue.
     *
     * @param event the specified event
     */
    void removeEvent(final Event<?> event) {
        synchronizedEvents.get(event.getType()).remove(event);
    }
}
