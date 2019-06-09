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

package org.springframework.boot.actuate.autoconfigure;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.boot.actuate.endpoint.mvc.HalJsonMvcEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.ManagementServletContext;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoints;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EndpointWebMvcHypermediaManagementContextConfiguration}.
 *
 * @author Andy Wilkinson
 */
public class EndpointWebMvcHypermediaManagementContextConfigurationTests {

	private AnnotationConfigWebApplicationContext context;

	@Before
	public void setRequestAttributes() {
		RequestContextHolder.setRequestAttributes(
				new ServletRequestAttributes(new MockHttpServletRequest()));
	}

	@After
	public void resetRequestAttributes() {
		RequestContextHolder.resetRequestAttributes();
	}

	@After
	public void closeContext() {
		this.context.close();
	}

	@Test
	public void basicConfiguration() {
		load();
		assertThat(this.context.getBeansOfType(ManagementServletContext.class))
				.hasSize(1);
		assertThat(this.context.getBeansOfType(HalJsonMvcEndpoint.class)).hasSize(1);
		assertThat(this.context.getBeansOfType(DefaultCurieProvider.class)).isEmpty();
	}

	private void load(String... properties) {
		this.context = new AnnotationConfigWebApplicationContext();
		TestPropertyValues.of(properties).applyTo(this.context);
		this.context.register(TestConfiguration.class,
				HttpMessageConvertersAutoConfiguration.class,
				EndpointWebMvcHypermediaManagementContextConfiguration.class);
		this.context.refresh();
	}

	@Configuration
	@EnableConfigurationProperties({ ManagementServerProperties.class,
			ServerProperties.class })
	static class TestConfiguration {

		@Bean
		public MvcEndpoints mvcEndpoints() {
			return new MvcEndpoints();
		}

	}

}
