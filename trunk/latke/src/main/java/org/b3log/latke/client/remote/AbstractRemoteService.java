/*
 * Copyright 2009, 2010, B3log
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b3log.latke.client.remote;

import java.io.Serializable;
import org.apache.log4j.Logger;
import org.jabsorb.JSONRPCBridge;

/**
 * Abstract remote service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.7, Jun 16, 2010
 */
public abstract class AbstractRemoteService implements Serializable {

    /**
     * Default serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractRemoteService.class);
    /**
     * JSON rpc bridge.
     */
    private JSONRPCBridge jsonRpcBridge;
    /**
     * JavaScirpt client service object name.
     */
    private String serviceObjectName;
    /**
     * Registered with json rpc bridge?
     */
    private boolean registered;

    /**
     * Public constructor with parameter. 
     *
     * <p>
     * Registers this constructing object as a remote JavaScirpt service object
     * with the specified json rpc bridge.
     * </p>
     *
     * @param jsonRpcBridge the specified json rpc bridge. 
     */
    public AbstractRemoteService(final JSONRPCBridge jsonRpcBridge) {
        this.jsonRpcBridge = jsonRpcBridge;
        serviceObjectName = genServiceObjectName();

        register();
    }

    /**
     * Registers this object as a remote JavaScript service object with json
     * rpc bridge.
     */
    public final void register() {
        synchronized (this) {
            if (!registered) {
                jsonRpcBridge.registerObject(serviceObjectName, this);
                registered = true;

                LOGGER.info("Remote JavaScirpt service[serviceObjectName="
                        + serviceObjectName + "] register successfully");
            }
        }

        LOGGER.info("Remote JavaScirpt service[serviceObjectName="
                + serviceObjectName + "] has been registered with json rpc bridge "
                + "[" + jsonRpcBridge + "] successfully");
    }

    /**
     * Unregisters this object as a remote JavaScript service object with json
     * rpc bridge.
     */
    public final void unregister() {
        synchronized (this) {
            if (registered) {
                jsonRpcBridge.unregisterObject(this);
                registered = false;

                LOGGER.info("Remote JavaScirpt service[serviceObjectName="
                        + serviceObjectName + "] unregister successfully");
            }
        }

        LOGGER.info("Remote JavaScirpt service[serviceObjectName="
                + serviceObjectName + "] has been unregistered with json rpc "
                + "bridge [" + jsonRpcBridge + "] successfully");
    }

    /**
     * Gets the JavaScirpt client service object name.
     *
     * @return service object name
     */
    public final String getServiceObjectName() {
        return serviceObjectName;
    }

    /**
     * Determines whether this object is registered with json rpc bridge.
     *
     * @return {@code true} as registered, {@code false} otherwise
     */
    public final boolean isRegistered() {
        return registered;
    }

    /**
     * Generates service object name for this object.
     *
     * @return the generated service object name
     */
    private String genServiceObjectName() {
        final String simpleName = getClass().getSimpleName();
        final char firstChar = simpleName.charAt(0);

        return Character.toLowerCase(firstChar) + simpleName.substring(1);
    }
}
