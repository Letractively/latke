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

package org.b3log.latke.repository.gae;

import org.b3log.latke.repository.Transaction;

/**
 * Google App Engine datastore transaction. Just wraps
 * {@link com.google.appengine.api.datastore.Transaction} simply.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Dec 8, 2010
 */
public final class GAETransaction implements Transaction {

    /**
     * Underlying Google App Engine transaction.
     */
    private com.google.appengine.api.datastore.Transaction appEngineDatastoreTx;

    /**
     * Constructs a {@link GAETransaction} object with the specified Google App
     * Engine datastore {@link com.google.appengine.api.datastore.Transaction
     * transaction}.
     *
     * @param appEngineDatastoreTx the specified Google App Engine datastore
     * transaction
     */
    public GAETransaction(
            final com.google.appengine.api.datastore.Transaction appEngineDatastoreTx) {
        this.appEngineDatastoreTx = appEngineDatastoreTx;
    }

    @Override
    public void commit() {
        appEngineDatastoreTx.commit();
    }

    @Override
    public void rollback() {
        appEngineDatastoreTx.rollback();
    }

    @Override
    public boolean isActive() {
        return appEngineDatastoreTx.isActive();
    }
}
