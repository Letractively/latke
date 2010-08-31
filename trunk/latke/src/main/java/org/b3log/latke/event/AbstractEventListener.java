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

import org.apache.log4j.Logger;

/**
 * Abstract event listener(Observer).
 *
 * @param <T> the type of event data
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Aug 27, 2010
 */
public abstract class AbstractEventListener<T> {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractEventListener.class);
    /**
     * Event manager.
     */
    private EventManager eventManager;
    /**
     * Event type.
     */
    private String eventType;

    /**
     * Gets the event type of this listener could handle.
     *
     * @return event type
     */
    public abstract String getEventType();

    /**
     * Constructs an {@link AbstractEventListener} object and register it with
     * the specified event manager and type of event.
     *
     * @param eventType the type of event this listener could handle
     * @param eventManager the specified event manager
     */
    public AbstractEventListener(final String eventType,
                                 final EventManager eventManager) {
        this.eventType = eventType;
        this.eventManager = eventManager;

        register();
    }

    /**
     * Performs the listener {@code action} method with the specified event
     * queue and event.
     *
     * @param eventQueue the specified event
     * @param event the specified event
     * @throws EventException event exception
     * @see #action(org.b3log.latke.event.Event) 
     */
    final void performAction(final AbstractEventQueue eventQueue,
                             final Event event) throws EventException {
        @SuppressWarnings("unchecked")
        final Event<T> eventObject = (Event<T>) event;
        try {
            action(eventObject);
        } finally { // remove event from event queue
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
     * @throws EventException event exception
     */
    public abstract void action(final Event<T> event) throws EventException;

    /**
     * Registers this listener to event manager.
     */
    private void register() {
        eventManager.registerListener(this);
        LOGGER.debug("Registered an event listener[classSimpleName="
                     + getClass().getSimpleName() + ", eventType=" + eventType
                     + "]");
    }
}