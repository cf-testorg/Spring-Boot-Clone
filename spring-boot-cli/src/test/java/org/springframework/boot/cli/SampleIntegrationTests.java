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

package org.springframework.boot.cli;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.ivy.util.FileUtil;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.OutputCapture;
import org.springframework.boot.cli.command.RunCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests to exercise the samples.
 * 
 * @author Dave Syer
 */
public class SampleIntegrationTests {

	@BeforeClass
	public static void cleanGrapes() throws Exception {
		// GrapesCleaner.cleanIfNecessary();
		// System.setProperty("ivy.message.logger.level", "3");
	}

	@Rule
	public OutputCapture outputCapture = new OutputCapture();

	private RunCommand command;

	private void start(final String... sample) throws Exception {
		Future<RunCommand> future = Executors.newSingleThreadExecutor().submit(
				new Callable<RunCommand>() {
					@Override
					public RunCommand call() throws Exception {
						RunCommand command = new RunCommand();
						command.run(sample);
						return command;
					}
				});
		this.command = future.get(4, TimeUnit.MINUTES);
	}

	@After
	public void stop() {
		if (this.command != null) {
			this.command.stop();
		}
	}

	@Test
	public void appSample() throws Exception {
		start("samples/app.groovy");
		String output = this.outputCapture.getOutputAndRelease();
		assertTrue("Wrong output: " + output, output.contains("Hello World"));
	}

	@Test
	public void templateSample() throws Exception {
		start("samples/template.groovy");
		String output = this.outputCapture.getOutputAndRelease();
		assertTrue("Wrong output: " + output, output.contains("Hello World!"));
	}

	@Test
	public void jobSample() throws Exception {
		start("samples/job.groovy", "foo=bar");
		String output = this.outputCapture.getOutputAndRelease();
		System.out.println(output);
		assertTrue("Wrong output: " + output,
				output.contains("completed with the following parameters"));
	}

	@Test
	public void reactorSample() throws Exception {
		start("samples/reactor.groovy", "Phil");
		String output = this.outputCapture.getOutputAndRelease();
		int count = 0;
		while (!output.contains("Hello Phil") && count++ < 5) {
			Thread.sleep(200);
			output = this.outputCapture.getOutputAndRelease();
		}
		assertTrue("Wrong output: " + output, output.contains("Hello Phil"));
	}

	@Test
	public void jobWebSample() throws Exception {
		start("samples/job.groovy", "samples/web.groovy", "foo=bar");
		String output = this.outputCapture.getOutputAndRelease();
		assertTrue("Wrong output: " + output,
				output.contains("completed with the following parameters"));
		String result = FileUtil.readEntirely(new URL("http://localhost:8080")
				.openStream());
		assertEquals("World!", result);
	}

	@Test
	public void webSample() throws Exception {
		start("samples/web.groovy");
		String result = FileUtil.readEntirely(new URL("http://localhost:8080")
				.openStream());
		assertEquals("World!", result);
	}

	@Test
	public void uiSample() throws Exception {
		start("samples/ui.groovy", "--classpath=.:src/test/resources");
		String result = FileUtil.readEntirely(new URL("http://localhost:8080")
				.openStream());
		assertTrue("Wrong output: " + result, result.contains("Hello World"));
		result = FileUtil.readEntirely(new URL(
				"http://localhost:8080/css/bootstrap.min.css").openStream());
		assertTrue("Wrong output: " + result, result.contains("container"));
	}

	@Test
	public void actuatorSample() throws Exception {
		start("samples/actuator.groovy");
		String result = FileUtil.readEntirely(new URL("http://localhost:8080")
				.openStream());
		assertEquals("{\"message\":\"Hello World!\"}", result);
	}

	@Test
	public void integrationSample() throws Exception {
		start("samples/integration.groovy");
		String output = this.outputCapture.getOutputAndRelease();
		assertTrue("Wrong output: " + output, output.contains("Hello, World"));
	}

	@Test
	public void xmlSample() throws Exception {
		start("samples/app.xml", "samples/runner.groovy");
		String output = this.outputCapture.getOutputAndRelease();
		assertTrue("Wrong output: " + output, output.contains("Hello World"));
	}

}
