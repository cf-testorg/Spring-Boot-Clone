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

package org.springframework.boot.autoconfigure.web;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Dave Syer
 */
public class MessageConvertersTests {

	@Test
	public void defaultsCreated() {
		MessageConverters messageConverters = new MessageConverters();
		assertFalse(messageConverters.getDefaultMessageConverters().isEmpty());
	}

	@Test
	public void overrideExistingConverter() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		MessageConverters messageConverters = new MessageConverters(
				Arrays.<HttpMessageConverter<?>> asList(converter));
		assertTrue(messageConverters.getMessageConverters().contains(converter));
	}

	@Test
	public void addNewOne() {
		HttpMessageConverter<?> converter = Mockito.mock(HttpMessageConverter.class);
		MessageConverters messageConverters = new MessageConverters(
				Arrays.<HttpMessageConverter<?>> asList(converter));
		assertTrue(messageConverters.getMessageConverters().contains(converter));
		assertEquals(converter, messageConverters.getMessageConverters().get(0));
	}

}
