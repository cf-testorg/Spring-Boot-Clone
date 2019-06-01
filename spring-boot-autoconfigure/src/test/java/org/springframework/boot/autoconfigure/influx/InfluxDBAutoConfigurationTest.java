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

package org.springframework.boot.autoconfigure.influx;

import org.assertj.core.api.Java6Assertions;
import org.influxdb.InfluxDB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Tests for {@link InfluxDBAutoConfiguration}.
 *
 * @author Sergey Kuptsov
 */
public class InfluxDBAutoConfigurationTest {

	private AnnotationConfigApplicationContext context;

	@Before
	public void setUp() {
		this.context = new AnnotationConfigApplicationContext();
	}

	@After
	public void tearDown() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void canEnableConfiguration() {
		this.context.register(InfluxDBAutoConfiguration.class);
		EnvironmentTestUtils.addEnvironment(this.context, "spring.data.influx.url=http://localhost");
		EnvironmentTestUtils.addEnvironment(this.context, "spring.data.influx.password:password");
		EnvironmentTestUtils.addEnvironment(this.context, "spring.data.influx.user:user");
		this.context.refresh();
		Java6Assertions.assertThat(this.context.getBeansOfType(InfluxDB.class)).isNotEmpty();
	}

	@Test
	public void canEnableWithEmptyUserConfiguration() {
		this.context.register(InfluxDBAutoConfiguration.class);
		EnvironmentTestUtils.addEnvironment(this.context, "spring.data.influx.url=http://localhost");
		this.context.refresh();
		Java6Assertions.assertThat(this.context.getBeansOfType(InfluxDB.class)).isNotEmpty();
	}
}
