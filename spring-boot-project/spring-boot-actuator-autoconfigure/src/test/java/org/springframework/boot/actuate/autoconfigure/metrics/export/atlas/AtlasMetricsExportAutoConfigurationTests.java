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

package org.springframework.boot.actuate.autoconfigure.metrics.export.atlas;

import com.netflix.spectator.atlas.AtlasConfig;
import io.micrometer.atlas.AtlasMeterRegistry;
import io.micrometer.core.instrument.Clock;
import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AtlasMetricsExportAutoConfiguration}.
 *
 * @author Andy Wilkinson
 */
public class AtlasMetricsExportAutoConfigurationTests {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withConfiguration(
					AutoConfigurations.of(AtlasMetricsExportAutoConfiguration.class));

	@Test
	public void backsOffWithoutAClock() {
		this.runner.run((context) -> assertThat(context)
				.doesNotHaveBean(AtlasMeterRegistry.class));
	}

	@Test
	public void autoConfiguresItsConfigAndMeterRegistry() {
		this.runner.withUserConfiguration(BaseConfiguration.class)
				.run((context) -> assertThat(context)
						.hasSingleBean(AtlasMeterRegistry.class)
						.hasSingleBean(AtlasConfig.class));
	}

	@Test
	public void allowsCustomConfigToBeUsed() {
		this.runner.withUserConfiguration(CustomConfigConfiguration.class)
				.run((context) -> assertThat(context)
						.hasSingleBean(AtlasMeterRegistry.class)
						.hasSingleBean(AtlasConfig.class).hasBean("customConfig"));
	}

	@Test
	public void allowsCustomRegistryToBeUsed() {
		this.runner.withUserConfiguration(CustomRegistryConfiguration.class)
				.run((context) -> assertThat(context)
						.hasSingleBean(AtlasMeterRegistry.class).hasBean("customRegistry")
						.hasSingleBean(AtlasConfig.class));
	}

	@Configuration
	static class BaseConfiguration {

		@Bean
		public Clock clock() {
			return Clock.SYSTEM;
		}

	}

	@Configuration
	@Import(BaseConfiguration.class)
	static class CustomConfigConfiguration {

		@Bean
		public AtlasConfig customConfig() {
			return new AtlasConfig() {

				@Override
				public String get(String k) {
					return null;
				}

			};
		}

	}

	@Configuration
	@Import(BaseConfiguration.class)
	static class CustomRegistryConfiguration {

		@Bean
		public AtlasMeterRegistry customRegistry(AtlasConfig config, Clock clock) {
			return new AtlasMeterRegistry(config, clock);
		}

	}

}
