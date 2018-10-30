/*
 * Copyright 2012-2013 the original author or authors.
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

package org.springframework.bootstrap.actuate.autoconfigure;

import javax.servlet.Servlet;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.bootstrap.actuate.security.SecurityFilterPostProcessor;
import org.springframework.bootstrap.actuate.trace.InMemoryTraceRepository;
import org.springframework.bootstrap.actuate.trace.TraceEndpoint;
import org.springframework.bootstrap.actuate.trace.TraceRepository;
import org.springframework.bootstrap.context.annotation.ConditionalOnClass;
import org.springframework.bootstrap.context.annotation.ConditionalOnMissingBean;
import org.springframework.bootstrap.context.annotation.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for /trace endpoint.
 * 
 * @author Dave Syer
 */
@Configuration
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class })
@ConditionalOnMissingBean({ TraceEndpoint.class })
public class TraceConfiguration {

	@Autowired
	private TraceRepository traceRepository;

	@Configuration
	public static class SecurityFilterPostProcessorConfiguration {

		@Autowired(required = false)
		private TraceRepository traceRepository = new InMemoryTraceRepository();

		@Bean
		@ConditionalOnMissingBean(TraceRepository.class)
		protected TraceRepository traceRepository() {
			return this.traceRepository;
		}

		@Value("${management.dump_requests:false}")
		private boolean dumpRequests;

		@Bean
		@ConditionalOnClass(name = "org.springframework.security.web.SecurityFilterChain")
		public SecurityFilterPostProcessor securityFilterPostProcessor(
				BeanFactory beanFactory) {
			SecurityFilterPostProcessor processor = new SecurityFilterPostProcessor(
					this.traceRepository);
			processor.setDumpRequests(this.dumpRequests);
			return processor;
		}

	}

	@Bean
	public TraceEndpoint traceEndpoint() {
		return new TraceEndpoint(this.traceRepository);
	}

}
