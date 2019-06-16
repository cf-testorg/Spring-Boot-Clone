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

package org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus;

import io.micrometer.prometheus.PrometheusConfig;

import org.springframework.boot.actuate.autoconfigure.metrics.export.RegistryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@link ConfigurationProperties} for configuring metrics export to Prometheus.
 *
 * @author Jon Schneider
 * @since 2.0.0
 */
@ConfigurationProperties(prefix = "spring.metrics.prometheus")
public class PrometheusProperties extends RegistryProperties implements PrometheusConfig {

	private boolean enabled = true;

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setDescriptions(Boolean descriptions) {
		set("descriptions", descriptions);
	}

	@Override
	public String prefix() {
		return "spring.metrics.prometheus";
	}

}
