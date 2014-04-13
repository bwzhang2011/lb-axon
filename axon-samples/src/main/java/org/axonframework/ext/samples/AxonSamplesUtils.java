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
package org.axonframework.ext.samples;

import com.hazelcast.config.Config;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.axonframework.ext.eventstore.chronicle.ChronicleEventStore;

public class AxonSamplesUtils {

    public static Config newHazelcastLocalConfig() {
        Config config = new Config();
        config.setProperty("hazelcast.logging.type", "slf4j");
        config.getNetworkConfig().setPortAutoIncrement(false);
        config.getNetworkConfig().getInterfaces().setEnabled(false);
        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);

        return config;
    }

    public static ChronicleEventStore defaultChronicleEventStore() {
        String basePath = FilenameUtils.concat(
            FileUtils.getTempDirectoryPath(),
            "chronicle/axon-evt-store");

        return new ChronicleEventStore(basePath);
    }
}