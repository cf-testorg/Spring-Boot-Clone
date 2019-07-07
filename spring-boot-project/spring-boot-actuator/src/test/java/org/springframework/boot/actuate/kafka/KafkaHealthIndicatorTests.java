/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.kafka;

import java.util.Collections;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Test;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.test.rule.KafkaEmbedded;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link KafkaHealthIndicator}
 *
 * @author Juan Rada
 */
public class KafkaHealthIndicatorTests {

	private static final Long RESPONSE_TIME = 1000L;

	private KafkaEmbedded kafkaEmbedded;
	private KafkaAdmin kafkaAdmin;

	private void startKafka(int replicationFactor) throws Exception {
		this.kafkaEmbedded = new KafkaEmbedded(1, true);
		this.kafkaEmbedded.brokerProperties(Collections.singletonMap(
				KafkaHealthIndicator.REPLICATION_PROPERTY,
				String.valueOf(replicationFactor)));
		this.kafkaEmbedded.before();
		this.kafkaAdmin = new KafkaAdmin(Collections.singletonMap(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				this.kafkaEmbedded.getBrokersAsString()));
	}

	private void shutdownKafka() throws Exception {
		this.kafkaEmbedded.destroy();
	}

	@Test
	public void kafkaIsUp() throws Exception {
		startKafka(1);
		KafkaHealthIndicator healthIndicator =
				new KafkaHealthIndicator(this.kafkaAdmin, RESPONSE_TIME);
		Health health = healthIndicator.health();
		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertDetails(health.getDetails());
		shutdownKafka();
	}

	private void assertDetails(Map<String, Object> details) {
		assertThat(details).containsEntry("brokerId", "0");
		assertThat(details).containsKey("clusterId");
		assertThat(details).containsEntry("nodes", 1);
	}

	@Test
	public void notEnoughNodesForReplicationFactor() throws Exception {
		startKafka(2);
		KafkaHealthIndicator healthIndicator =
				new KafkaHealthIndicator(this.kafkaAdmin, RESPONSE_TIME);
		Health health = healthIndicator.health();
		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertDetails(health.getDetails());
		shutdownKafka();
	}

	@Test
	public void kafkaIsDown() throws Exception {
		this.kafkaAdmin = new KafkaAdmin(Collections.singletonMap(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:34987"));
		KafkaHealthIndicator healthIndicator =
				new KafkaHealthIndicator(this.kafkaAdmin, RESPONSE_TIME);
		Health health = healthIndicator.health();
		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat((String) health.getDetails().get("error")).isNotEmpty();
	}
}
