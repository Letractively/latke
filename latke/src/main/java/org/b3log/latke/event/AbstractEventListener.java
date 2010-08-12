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

import java.util.Observable;
import java.util.Observer;

/**
 * Abstract event listener.
 *
 * @param <T> the type of event data
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 12, 2010
 */
public abstract class AbstractEventListener<T> implements Observer {

    /**
     * Event manager.
     */
    private EventManager eventManager;

    /**
     * Constructs an {@link AbstractEventListener} object and register it with
     * the specified event manager.
     *
     * @param eventManager the specified event manager
     */
    public AbstractEventListener(final EventManager eventManager) {
        this.eventManager = eventManager;

        register();
    }

    @Override
    public void update(final Observable eventQueue,
                       final Object event) {
        @SuppressWarnings("unchecked")
        final Event<T> eventObject = (Event<T>) event;
        try {
            process(eventObject);
        } finally {
            if (eventQueue instanceof SynchronizedEventQueue) {
                final SynchronizedEventQueue synchronizedEventQueue =
                        (SynchronizedEventQueue) eventQueue;
                synchronizedEventQueue.removeEvent(eventObject);
            }
        }
    }

    /**
     * Processes the specified event.
     *
     * @param event the specified event
     */
    public abstract void process(final Event<T> event);

    /**
     * Registers this listener to event manager.
     */
    private void register() {
        eventManager.registerListener(this);
    }
}
