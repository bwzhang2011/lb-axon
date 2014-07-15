/*
 * Copyright (c) 2010-2013. Axon Framework
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
package org.axonframework.ext.hazelcast.test;

import com.google.common.collect.Lists;
import com.hazelcast.core.HazelcastInstance;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.domain.SimpleDomainEventStream;
import org.axonframework.eventstore.EventStore;
import org.axonframework.ext.hazelcast.store.HzEventStore;
import org.axonframework.ext.hazelcast.test.model.HzAxonEventMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HzEventStoreTest extends HzTestBase {
    private HazelcastInstance m_instance = null;

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() throws Exception {
        m_instance = createInstance();
    }

    @After
    public void tearDown() throws Exception {
        if(m_instance != null)  {
            m_instance.shutdown();
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testSaveStreamAndReadBack() {
        assertNotNull("HazelcastInstance is null",m_instance);

        String     type  = "org.axonframework.ext.store.chronicle.test";
        String     aid   = UUID.randomUUID().toString();
        EventStore store = new HzEventStore(m_instance);
        int        evts  = 10;

        List<DomainEventMessage<?>> demWrite = Lists.newArrayListWithCapacity(evts);
        for(int i=0;i<evts;i++) {
            demWrite.add(new HzAxonEventMessage(aid,i,"evt-" + i));
        }

        store.appendEvents(type,new SimpleDomainEventStream(demWrite));

        List<DomainEventMessage<?>> demRead = Lists.newArrayListWithCapacity(evts);
        DomainEventStream des = store.readEvents(type,aid);
        while (des.hasNext()) {
            demRead.add(des.next());
        }

        assertEquals(demWrite.size(),demRead.size());

        for(int i=0;i<evts;i++) {
            assertEquals(
                demWrite.get(i).getIdentifier(),
                demRead.get(i).getIdentifier());
        }
    }
}
