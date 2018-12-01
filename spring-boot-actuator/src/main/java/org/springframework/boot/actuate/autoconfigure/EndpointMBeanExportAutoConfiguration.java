/*
 * Copyright 2013 the original author or authors.
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

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.jmx.EndpointMBeanExporter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.MBeanExporter;

/**
 * {@link EnableAutoConfiguration Auto-configuration} to enable JMX export for
 * {@link Endpoint}s.
 * 
 * @author Christian Dupuis
 */
@Configuration
@ConditionalOnBean({ MBeanExporter.class })
@AutoConfigureAfter({ EndpointAutoConfiguration.class })
@ConditionalOnExpression("${endpoints.jmx.enabled:true}")
class EndpointMBeanExportAutoConfiguration {

	@Bean
	public EndpointMBeanExporter endpointMBeanExporter() {
		return new EndpointMBeanExporter();
	}
}