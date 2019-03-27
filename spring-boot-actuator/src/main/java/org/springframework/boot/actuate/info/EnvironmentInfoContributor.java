/*
 * Copyright 2012-2015 the original author or authors.
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

package org.springframework.boot.actuate.info;

import java.util.Map;

import org.springframework.core.env.ConfigurableEnvironment;

/**
 * A {@link InfoContributor} that provides all environment entries prefixed with
 * info.
 *
 * @author Meang Akira Tanaka
 * @author Stephane Nicoll
 * @since 1.4.0
 */
public class EnvironmentInfoContributor extends AbstractEnvironmentInfoContributor {

	private final Map<String, Object> info;

	public EnvironmentInfoContributor(ConfigurableEnvironment environment) {
		super(environment);
		this.info = extract("info");
	}

	@Override
	public void contribute(Info.Builder builder) {
		builder.withDetails(this.info);
	}

}
