/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.actuate.autoconfigure.health;

import java.util.Map;

import reactor.core.publisher.Flux;

import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.RedisReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;

/**
 * Configuration for available {@link ReactiveHealthIndicator reactive health indicators}.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
@Configuration
@ConditionalOnClass(Flux.class)
public class ReactiveHealthIndicatorsConfiguration {

	@Configuration
	@ConditionalOnBean(ReactiveRedisConnectionFactory.class)
	@ConditionalOnEnabledHealthIndicator("redis")
	static class RedisHealthIndicatorConfiguration extends
			CompositeReactiveHealthIndicatorConfiguration<RedisReactiveHealthIndicator, ReactiveRedisConnectionFactory> {

		private final Map<String, ReactiveRedisConnectionFactory> redisConnectionFactories;

		RedisHealthIndicatorConfiguration(
				Map<String, ReactiveRedisConnectionFactory> redisConnectionFactories) {
			this.redisConnectionFactories = redisConnectionFactories;
		}

		@Bean
		@ConditionalOnMissingBean(name = "redisHealthIndicator")
		public ReactiveHealthIndicator redisHealthIndicator() {
			return createHealthIndicator(this.redisConnectionFactories);
		}

	}

}
