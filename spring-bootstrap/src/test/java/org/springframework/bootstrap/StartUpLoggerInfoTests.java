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

package org.springframework.bootstrap;

import org.apache.commons.logging.impl.SimpleLog;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Dave Syer
 */
public class StartUpLoggerInfoTests {

	private StringBuffer output = new StringBuffer();

	private SimpleLog log = new SimpleLog("test") {
		@Override
		protected void write(StringBuffer buffer) {
			StartUpLoggerInfoTests.this.output.append(buffer).append("\n");
		};
	};

	@Test
	public void sourceClassIncluded() {
		new StartupInfoLogger(getClass()).log(this.log);
		assertTrue("Wrong output: " + this.output,
				this.output.toString().contains("Starting " + getClass().getSimpleName()));
		// System.err.println(this.output);
	}

	@Test
	public void bootstrapVersionIncluded() {
		new StartupInfoLogger(getClass()).log(this.log);
		assertTrue("Wrong output: " + this.output,
				this.output.toString().contains("Spring Bootstrap v"));
	}

}
