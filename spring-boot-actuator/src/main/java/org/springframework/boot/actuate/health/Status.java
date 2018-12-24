/*
 * Copyright 2012-2014 the original author or authors.
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

package org.springframework.boot.actuate.health;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Value object to express state of a component or subsystem.
 *
 * <p>
 * Status provides convenient constants for commonly used states like {@link #UP},
 * {@link #DOWN} or {@link #OUT_OF_SERVICE}.
 *
 * <p>
 * Custom states can also be created and used throughout the Spring Boot Health subsystem.
 *
 * @author Christian Dupuis
 * @since 1.1.0
 */
@JsonInclude(Include.NON_EMPTY)
public class Status {

	/**
	 * Convenient constant value representing unknown state
	 */
	public static final Status UNKOWN = new Status("UNKOWN");

	/**
	 * Convenient constant value representing up state
	 */
	public static final Status UP = new Status("UP");

	/**
	 * Convenient constant value representing down state
	 */
	public static final Status DOWN = new Status("DOWN");

	/**
	 * Convenient constant value representing out-of-service state
	 */
	public static final Status OUT_OF_SERVICE = new Status("OUT_OF_SERVICE");

	private final String code;

	private final String description;

	public Status(String code) {
		this(code, "");
	}

	public Status(String code, String description) {
		Assert.notNull(code, "Code must not be null");
		Assert.notNull(description, "Description must not be null");
		this.code = code;
		this.description = description;
	}

	@JsonProperty("status")
	public String getCode() {
		return this.code;
	}

	@JsonInclude(Include.NON_EMPTY)
	public String getDescription() {
		return this.description;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj != null && obj instanceof Status) {
			return ObjectUtils.nullSafeEquals(this.code, ((Status) obj).code);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.code.hashCode();
	}
}