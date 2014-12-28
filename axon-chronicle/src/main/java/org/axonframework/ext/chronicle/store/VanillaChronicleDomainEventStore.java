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
package org.axonframework.ext.chronicle.store;


import net.openhft.chronicle.ChronicleQueueBuilder;
import org.apache.commons.io.FilenameUtils;
import org.axonframework.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class VanillaChronicleDomainEventStore<T> extends ChronicleDomainEventStore<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaChronicleDomainEventStore.class);

    /**
     * c-tor
     *
     * @param serializer        the DomainEventStream serializer
     * @param basePath          the Chronicle data path
     * @param storageId         the Chronicle data name
     * @param aggregateType     the AggregateType
     * @param aggregateId       the AggregateId
     */
    public VanillaChronicleDomainEventStore(Serializer serializer, String basePath, String storageId, String aggregateType, String aggregateId) {
        super(serializer, basePath, storageId, aggregateType, aggregateId);
    }

    @Override
    public void init() {
        String dataPath = FilenameUtils.concat(getBasePath(), getStorageId());
        LOGGER.debug("VanillaChronicle => BasePath: {}, DataPath: {}", getBasePath(), dataPath);

        try {
            init(ChronicleQueueBuilder.vanilla(dataPath).build());
        } catch(Exception e) {
            LOGGER.warn("Exception",e);
        }
    }
}
