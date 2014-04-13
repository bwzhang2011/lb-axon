/*
 * Copyright (c) 2010-2014. Axon Framework
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
package org.axonframework.ext.repository;

import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.repository.Repository;

/**
 *
 */
public interface IRepositoryFactory {
    /**
     * Factory method
     *
     * @param type the aggregate class
     * @param <T>  the aggregate type
     * @param <I>  the type of the identifier of the aggregate
     * @return     a Repository instance
     */
    public <I, T extends EventSourcedAggregateRoot<I>> Repository<T> createRepository(
        Class<T> type);
}
