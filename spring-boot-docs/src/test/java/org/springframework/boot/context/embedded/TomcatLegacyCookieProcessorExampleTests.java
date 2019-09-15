/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.context.embedded;

import org.apache.catalina.Context;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.TomcatLegacyCookieProcessorExample.LegacyCookieProcessorConfiguration;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TomcatLegacyCookieProcessorExample}.
 *
 * @author Andy Wilkinson
 */
public class TomcatLegacyCookieProcessorExampleTests {

	@Test
	public void cookieProcessorIsCustomized() {
		EmbeddedWebApplicationContext applicationContext = (EmbeddedWebApplicationContext) new SpringApplication(
				TestConfiguration.class, LegacyCookieProcessorConfiguration.class).run();
		Context context = (Context) ((TomcatEmbeddedServletContainer) applicationContext.getEmbeddedServletContainer())
				.getTomcat().getHost().findChildren()[0];
		assertThat(context.getCookieProcessor()).isInstanceOf(LegacyCookieProcessor.class);
	}

	@Configuration
	static class TestConfiguration {

		@Bean
		public TomcatEmbeddedServletContainerFactory tomcatFactory() {
			return new TomcatEmbeddedServletContainerFactory(0);
		}

		@Bean
		public EmbeddedServletContainerCustomizerBeanPostProcessor postProcessor() {
			return new EmbeddedServletContainerCustomizerBeanPostProcessor();
		}

	}

}
