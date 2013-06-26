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
package org.axonframework.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Instance;
import com.hazelcast.core.MultiMap;

import java.util.Collection;

/**
 *
 */
public interface IHzInstanceProxy {

    /**
     *
     * @return
     */
    public HazelcastInstance getInstance();

    /**
     *
     * @return
     */
    public ClassLoader getClassloader();

    /**
     *
     * @param mapName
     * @param <K>
     * @param <V>
     * @return
     */
    public <K,V> IMap<K,V> getMap(String mapName);

    /**
     *
     * @param mapName
     * @param <K>
     * @param <V>
     * @return
     */
    public <K,V> MultiMap<K,V> getMultiMap(String mapName);

    /**
     *
     * @param listName
     * @param <T>
     * @return
     */
    public <T> IList<T> getList(String listName);

    /**
     *
     * @param queueName
     * @param <T>
     * @return
     */
    public <T> IQueue<T> getQueue(String queueName);

    /**
     *
     * @param lockName
     * @return
     */
    public ILock getLock(String lockName);

    /**
     *
     * @param topicName
     * @param <E>
     * @return
     */
    public <E> ITopic<E> getTopic(String topicName);

    /**
     *
     * @return
     */
    public Collection<Instance> getDistributedObjects();

    /**
     *
     * @return
     */
    public Collection<Instance> getDistributedObjects(Instance.InstanceType type);
}
