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
package org.b3log.latke.repository.sleepycat;

import org.b3log.latke.action.util.PageCaches;
import org.b3log.latke.repository.Transaction;

/**
 * Sleepycat  transaction. Just wraps {@link com.sleepycat.je.Transaction} 
 * simply.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Dec 3, 2011
 */
public class SleepycatTransaction implements Transaction {

    /**
     * Sleepycat transaction.
     */
    private com.sleepycat.je.Transaction sleepycatTransaction;
    /**
     * Flag of clear query cache.
     */
    private boolean clearQueryCache = true;

    /**
     * Constructs a {@link SleepycatTransaction} object with the specified Sleepycat
     * database transaction {@link com.sleepycat.je.Transaction transaction}.
     *
     * @param sleepycatTransaction the specified Sleepycat database transaction 
     */
    public SleepycatTransaction(
            final com.sleepycat.je.Transaction sleepycatTransaction) {
        this.sleepycatTransaction = sleepycatTransaction;
    }

    /**
     * Gets the underlying Sleepycat transaction.
     * 
     * @return Sleepycat transaction
     */
    public com.sleepycat.je.Transaction getSleepycatTransaction() {
        return sleepycatTransaction;
    }

    @Override
    public String getId() {
        return String.valueOf(sleepycatTransaction.getId());
    }

    @Override
    public void commit() {
        sleepycatTransaction.commit();
        
        SleepycatRepository.TX.set(null);

        if (clearQueryCache) {
            SleepycatRepository.CACHE.removeAll();
            PageCaches.removeAll();
        }
    }

    @Override
    public void rollback() {
        sleepycatTransaction.abort();
        SleepycatRepository.TX.set(null);
    }

    @Override
    public boolean isActive() {
        return sleepycatTransaction.isValid();
    }

    @Override
    public void clearQueryCache(final boolean flag) {
        this.clearQueryCache = flag;
    }
}
