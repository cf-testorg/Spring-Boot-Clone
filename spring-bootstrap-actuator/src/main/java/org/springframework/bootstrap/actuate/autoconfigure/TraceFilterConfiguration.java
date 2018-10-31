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
import org.springframework.bootstrap.actuate.endpoint.trace.WebRequestLoggingFilter;
import org.springframework.bootstrap.actuate.trace.InMemoryTraceRepository;
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
public class TraceFilterConfiguration {

	@Autowired(required = false)
	private TraceRepository traceRepository = new InMemoryTraceRepository();

	@ConditionalOnMissingBean(TraceRepository.class)
	@Configuration
	protected static class TraceRepositoryConfiguration {
		@Bean
		public TraceRepository traceRepository() {
			return new InMemoryTraceRepository();
		}
	}

	@ConditionalOnClass({ Servlet.class, DispatcherServlet.class })
	protected static class WebRequestLoggingFilterConfiguration {

		@Autowired
		private TraceRepository traceRepository;

		@Value("${management.dump_requests:false}")
		private boolean dumpRequests;

		@Bean
		public WebRequestLoggingFilter webRequestLoggingFilter(BeanFactory beanFactory) {

			WebRequestLoggingFilter filter = new WebRequestLoggingFilter(
					this.traceRepository);
			filter.setDumpRequests(this.dumpRequests);
			return filter;
		}

	}

}
