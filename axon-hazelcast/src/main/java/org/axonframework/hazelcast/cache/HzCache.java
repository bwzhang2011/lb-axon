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
package org.axonframework.hazelcast.cache;

import com.google.common.collect.Sets;
import com.hazelcast.core.IMap;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheEntry;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheListener;
import net.sf.jsr107cache.CacheStatistics;
import org.axonframework.domain.AggregateRoot;
import org.axonframework.hazelcast.IHzInstanceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class HzCache implements Cache {
    private final static Logger LOGGER = LoggerFactory.getLogger(HzCache.class);

    private final IMap<Object,Object> m_cache;
    private final ClassLoader m_classLoader;


    /**
     * c-tor
     *
     * @param cacheManager
     * @param cacheName
     */
    public HzCache(IHzInstanceProxy cacheManager, String cacheName) {
        m_classLoader = cacheManager.getClassloader();
        m_cache = cacheManager.getMap(cacheName);

        LOGGER.debug("{} - ClassLoader {}",m_cache.getName(),m_classLoader);
        LOGGER.debug("{} - Cache       {}",m_cache.getName(),m_cache);
    }

    @Override
    public boolean containsKey(Object key) {
        return m_cache.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return m_cache.containsValue(value);
    }

    @Override
    public Set keySet() {
        return m_cache.keySet();
    }

    @Override
    public Set entrySet() {
        return m_cache.entrySet();
    }

    @Override
    public boolean isEmpty() {
        return m_cache.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void putAll(Map t) {
        m_cache.putAll(t);
    }

    @Override
    public int size() {
        return m_cache.size();
    }

    @Override
    public Collection values() {
        return m_cache.values();
    }

    @Override
    public Object get(Object key) {
        ClassLoader cl  = Thread.currentThread().getContextClassLoader();
        Object      obj = null;

        try {
            Thread.currentThread().setContextClassLoader(m_classLoader);
            obj = m_cache.get(key);

            if(obj instanceof AggregateRoot) {
                AggregateRoot ar = (AggregateRoot)obj;
                LOGGER.debug("Cache.get : id={}, version={}",
                    ar.getIdentifier(),ar.getVersion());
            }

        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        return obj;
    }

    @Override
    public Object peek(Object key) {
        return get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        ClassLoader cl  = Thread.currentThread().getContextClassLoader();
        Object      obj = null;

        try {
            Thread.currentThread().setContextClassLoader(m_classLoader);
            if(value instanceof AggregateRoot) {
                AggregateRoot ar = (AggregateRoot)value;
                LOGGER.debug("Cache.put : id={}, version={}",
                    ar.getIdentifier(),ar.getVersion());
            }

            obj = m_cache.put(key,value);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        return obj;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map getAll(Collection keys) throws CacheException {
        return m_cache.getAll(Sets.newHashSet(keys));
    }

    @Override
    public void load(Object key) throws CacheException {
        throw new RuntimeException("load - NotImplemented");
    }

    @Override
    public void loadAll(Collection keys) throws CacheException {
        throw new RuntimeException("loadAll - NotImplemented");
    }

    @Override
    public CacheEntry getCacheEntry(Object key) {
        return null;
    }

    @Override
    public CacheStatistics getCacheStatistics() {
        return null;
    }

    @Override
    public Object remove(Object key) {
        ClassLoader cl  = Thread.currentThread().getContextClassLoader();
        Object      obj = null;

        try {
            Thread.currentThread().setContextClassLoader(m_classLoader);
            obj = m_cache.remove(key);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        return obj;
    }

    @Override
    public void clear() {
        ClassLoader cl  = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(m_classLoader);
            m_cache.clear();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @Override
    public void evict() {
        throw new RuntimeException("evict - NotImplemented");
    }

    @Override
    public void addListener(CacheListener listener) {
        throw new RuntimeException("addListener - NotImplemented");
    }

    @Override
    public void removeListener(CacheListener listener) {
        throw new RuntimeException("removeListener - NotImplemented");
    }
}
