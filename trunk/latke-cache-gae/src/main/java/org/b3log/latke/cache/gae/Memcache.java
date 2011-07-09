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
package org.b3log.latke.cache.gae;

import com.google.appengine.api.memcache.InvalidValueException;
import com.google.appengine.api.memcache.MemcacheSerialization;
import com.google.appengine.api.memcache.MemcacheSerialization.Flag;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.MemcacheServicePb;
import com.google.appengine.api.memcache.Stats;
import com.google.appengine.repackaged.com.google.protobuf.ByteString;
import com.google.appengine.repackaged.com.google.protobuf.InvalidProtocolBufferException;
import com.google.appengine.repackaged.com.google.protobuf.Message;
import com.google.apphosting.api.ApiProxy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Collection;
import java.util.logging.Logger;
import org.b3log.latke.cache.Cache;

/**
 * Simple warper of <a href="http://code.google.com/appengine/docs/java/memcache/">
 * Google App Engine memcache service</a>.
 * 
 * <p>
 *   <b>Note</b>:
 *   <ul>
 *     <li>Invoking {@link #removeAll()} will clear all caches.</li>
 *     <li>Statistics does not respect caches, this will return statistic states 
 *         sum for all caches.</li>
 *   </ul>
 * </p>
 *
 * @param <K> the key of an object
 * @param <V> the type of objects
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.1.0, Jul 9, 2011
 */
public final class Memcache<K, V> implements Cache<K, V> {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(Memcache.class.getName());
    /**
     * Memcache service.
     */
    private MemcacheService memcacheService;
    /**
     * Name of this cache.
     */
    private String name;
    /**
     * Integer value for true flag.
     */
    private static final int TRUE_INT = 49;
    /**
     * Integer value for false flag.
     */
    private static final int FALSE_INT = 48;

    /**
     * Constructs a memcache with the specified name.
     *
     * @param name the specified name
     */
    public Memcache(final String name) {
        this.name = name;

        memcacheService = MemcacheServiceFactory.getMemcacheService(name);
    }

    /**
     * Gets the name of this cache.
     *
     * @return name of this cache
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final K key) {
        return memcacheService.contains(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final K key, final V value) {
        if (null == key) {
            throw new IllegalArgumentException(
                    "The specified key can not be null!");
        }

        if (null == value) {
            throw new IllegalArgumentException(
                    "The specified value can not be null!");
        }

        memcacheService.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final K key) {
        if (null == key) {
            return null;
        }

        final MemcacheServicePb.MemcacheGetResponse.Builder response =
                MemcacheServicePb.MemcacheGetResponse.newBuilder();
        MemcacheServicePb.MemcacheGetRequest request;
        try {
            request = MemcacheServicePb.MemcacheGetRequest.newBuilder().
                    setNameSpace(getName()).addKey(
                    ByteString.copyFrom(MemcacheSerialization.makePbKey(key))).
                    build();
        } catch (final IOException ex) {
            throw new IllegalArgumentException(
                    (new StringBuilder()).append("Cannot use as a key[").
                    append(key).append("]").toString(), ex);
        }

        if (!makeSyncCall("Get", request, response,
                          (new StringBuilder()).append(
                "Memcache get: exception getting 1 key[").append(key).append(
                "]").toString())) {
            return null;
        }

        if (0 == response.getItemCount()) {
            return null;
        }

        final MemcacheServicePb.MemcacheGetResponse.Item item =
                response.getItem(0);
        try {
            return (V) deserialize(item.getValue().toByteArray(),
                                   item.getFlags());
        } catch (final ClassNotFoundException ex) {
            memcacheService.getErrorHandler().handleDeserializationError(
                    new InvalidValueException((new StringBuilder()).append(
                    "Can't find class for value of key[").append(key).append(
                    "]").toString(), ex));
        } catch (final IOException ex) {
            throw new InvalidValueException((new StringBuilder()).append(
                    "IO exception parsing value of [").append(key).append("]").
                    toString(), ex);
        }

        return null;
    }

    @Override
    public long inc(final K key, final long delta) {
        if (null == key) {
            throw new IllegalArgumentException(
                    "The specified key can not be null!");
        }

        if (!memcacheService.contains(key)) {
            memcacheService.put(key, 1L);
        }

        return memcacheService.increment(key, delta);
    }

    @Override
    public void remove(final K key) {
        memcacheService.delete(key);
    }

    @Override
    public void remove(final Collection<K> keys) {
        memcacheService.deleteAll(keys);
    }

    @Override
    public void removeAll() {
        memcacheService.clearAll(); // Will clear in all namespaces
        LOGGER.finest("Clear all caches");
    }

    @Override
    public void setMaxCount(final long maxCount) {
    }

    @Override
    public long getMaxCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getHitCount() {
        final Stats statistics = memcacheService.getStatistics();
        if (null
            != statistics) {
            return statistics.getHitCount();
        }

        return -1;
    }

    @Override
    public long getMissCount() {
        final Stats statistics = memcacheService.getStatistics();
        if (null != statistics) {
            return statistics.getMissCount();
        }

        return -1;
    }

    @Override
    public long getPutCount() {
        return getCachedCount();
    }

    @Override
    public long getCachedBytes() {
        final Stats statistics = memcacheService.getStatistics();
        if (null != statistics) {
            return statistics.getTotalItemBytes();
        }

        return -1;
    }

    @Override
    public long getHitBytes() {
        final Stats statistics = memcacheService.getStatistics();
        if (null != statistics) {
            return statistics.getBytesReturnedForHits();
        }

        return -1;
    }

    @Override
    public long getCachedCount() {
        final Stats statistics = memcacheService.getStatistics();
        if (null != statistics) {
            return statistics.getItemCount();
        }

        return -1;
    }

    @Override
    public void collect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Makes a sync call with the specified method name, request, response and
     * error text.
     *
     * @param methodName the specified method name
     * @param request the specified request
     * @param response the specified response
     * @param errorText the specified error text
     * @return {@code true} for succeeded, returns {@code false} otherwise
     */
    private boolean makeSyncCall(final String methodName,
                                 final Message request,
                                 final Message.Builder response,
                                 final String errorText) {
        try {
            final byte[] responseBytes =
                    ApiProxy.makeSyncCall("memcache", methodName,
                                          request.toByteArray());
            response.mergeFrom(responseBytes);

            return true;
        } catch (final InvalidProtocolBufferException ex) {
            memcacheService.getErrorHandler().handleServiceError(
                    new MemcacheServiceException("Could not decode response:",
                                                 ex));
        } catch (final com.google.apphosting.api.ApiProxy.ApplicationException ae) {
            LOGGER.info((new StringBuilder()).append(errorText).append(": ").
                    append(ae.getErrorDetail()).toString());
            memcacheService.getErrorHandler().handleServiceError(
                    new MemcacheServiceException(errorText));
        } catch (final com.google.apphosting.api.ApiProxy.ApiProxyException ex) {
            memcacheService.getErrorHandler().handleServiceError(
                    new MemcacheServiceException(errorText, ex));
        }

        return false;
    }

    /**
     * Deserializes the specified array of bytes with the specified flag.
     *
     * @param value the specified array of bytes
     * @param flag the specified flag
     * @return an object or {@code null}
     * @throws ClassNotFoundException class not found exception
     * @throws IOException io exception
     */
    public static Object deserialize(final byte[] value, final int flag)
            throws ClassNotFoundException, IOException {
        final Flag flagVal = Flag.fromInt(flag);

        switch (flagVal) {
            case BYTES:
                return value;
            case BOOLEAN:
                if (value.length != 1) {
                    throw new InvalidValueException(
                            "Cannot deserialize Boolean: bad length");
                }

                switch (value[0]) {
                    case TRUE_INT:
                        return Boolean.TRUE;
                    case FALSE_INT:
                        return Boolean.FALSE;
                    default:
                        throw new InvalidValueException(
                                "Cannot deserialize Boolean[value="
                                + value[0] + "]");
                }
            case BYTE:
                return Byte.valueOf(new String(value, "US-ASCII"));
            case SHORT:
                return Short.valueOf(new String(value, "US-ASCII"));
            case INTEGER:
                return Integer.valueOf(new String(value, "US-ASCII"));
            case LONG:
                return Long.valueOf(new String(value, "US-ASCII"));
            case UTF8:
                return new String(value, "UTF-8");
            case OBJECT:
                if (value.length == 0) {
                    return null;
                }

                final ByteArrayInputStream bais =
                        new ByteArrayInputStream(value);
                final ObjectInputStream ois = new ObjectInputStream(bais) {

                    @Override
                    protected Class<?> resolveClass(final ObjectStreamClass desc)
                            throws IOException, ClassNotFoundException {
                        final String className = desc.getName();
                        try {
                            return Class.forName(className, false, Thread.
                                    currentThread().getContextClassLoader());
                        } catch (final ClassNotFoundException ex) {
                            return super.resolveClass(desc);
                        }
                    }
                };

                Object ret = null;
                try {
                    ret = ois.readObject();
                } finally {
                    ois.close();
                }

                return ret;

            default:
                return null;
        }
    }
}
