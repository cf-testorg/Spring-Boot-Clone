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

package org.springframework.cli.command;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.springframework.cli.Command;
import org.springframework.cli.runner.SpringApplicationRunner;
import org.springframework.cli.runner.SpringApplicationRunnerConfiguration;

import static java.util.Arrays.asList;

/**
 * {@link Command} to 'run' a groovy script or scripts.
 * 
 * @author Phillip Webb
 * @author Dave Syer
 * @see SpringApplicationRunner
 */
public class RunCommand extends OptionParsingCommand {

	public RunCommand() {
		super("run", "Run a spring groovy script", new RunOptionHandler());
	}

	@Override
	public String getUsageHelp() {
		return "[options] <files> [--] [args]";
	}

	public void stop() {
		if (this.getHandler() != null) {
			((RunOptionHandler) this.getHandler()).runner.stop();
		}
	}

	private static class RunOptionHandler extends OptionHandler {

		private OptionSpec<Void> watchOption;

		private OptionSpec<Void> editOption;

		private OptionSpec<Void> noGuessImportsOption;

		private OptionSpec<Void> noGuessDependenciesOption;

		private OptionSpec<Void> verboseOption;

		private OptionSpec<Void> quietOption;

		private OptionSpec<Void> localOption;

		private OptionSpec<String> classpathOption;

		private SpringApplicationRunner runner;

		@Override
		protected void options() {
			this.watchOption = option("watch", "Watch the specified file for changes");
			this.localOption = option("local",
					"Accumulate the dependencies in a local folder (./grapes)");
			this.editOption = option(asList("edit", "e"),
					"Open the file with the default system editor");
			this.noGuessImportsOption = option("no-guess-imports",
					"Do not attempt to guess imports");
			this.noGuessDependenciesOption = option("no-guess-dependencies",
					"Do not attempt to guess dependencies");
			this.verboseOption = option(asList("verbose", "v"), "Verbose logging");
			this.quietOption = option(asList("quiet", "q"), "Quiet logging");
			this.classpathOption = option(asList("classpath", "cp"),
					"Additional classpath entries").withRequiredArg();
		}

		@Override
		protected void run(OptionSet options) throws Exception {
			List<?> nonOptionArguments = options.nonOptionArguments();
			File[] files = getFileArguments(nonOptionArguments);
			List<?> args = nonOptionArguments.subList(files.length,
					nonOptionArguments.size());

			if (options.has(this.editOption)) {
				Desktop.getDesktop().edit(files[0]);
			}

			SpringApplicationRunnerConfiguration configuration = new SpringApplicationRunnerConfigurationAdapter(
					options);
			if (configuration.isLocal() && System.getProperty("grape.root") == null) {
				System.setProperty("grape.root", ".");
			}
			this.runner = new SpringApplicationRunner(configuration, files,
					args.toArray(new String[args.size()]));
			this.runner.compileAndRun();
		}

		private File[] getFileArguments(List<?> nonOptionArguments) {
			List<File> files = new ArrayList<File>();
			for (Object option : nonOptionArguments) {
				if (option instanceof String) {
					String filename = (String) option;
					if ("--".equals(filename)) {
						break;
					}
					// TODO: add support for strict Java compilation
					// TODO: add support for recursive search in directory
					if (filename.endsWith(".groovy") || filename.endsWith(".java")) {
						File file = new File(filename);
						if (file.isFile() && file.canRead()) {
							files.add(file);
						}
					}
				}
			}
			if (files.size() == 0) {
				throw new RuntimeException("Please specify a file to run");
			}
			return files.toArray(new File[files.size()]);
		}

		/**
		 * Simple adapter class to present the {@link OptionSet} as a
		 * {@link SpringApplicationRunnerConfiguration}.
		 */
		private class SpringApplicationRunnerConfigurationAdapter implements
				SpringApplicationRunnerConfiguration {

			private OptionSet options;

			public SpringApplicationRunnerConfigurationAdapter(OptionSet options) {
				this.options = options;
			}

			@Override
			public boolean isWatchForFileChanges() {
				return this.options.has(RunOptionHandler.this.watchOption);
			}

			@Override
			public boolean isGuessImports() {
				return !this.options.has(RunOptionHandler.this.noGuessImportsOption);
			}

			@Override
			public boolean isGuessDependencies() {
				return !this.options.has(RunOptionHandler.this.noGuessDependenciesOption);
			}

			@Override
			public boolean isLocal() {
				return this.options.has(RunOptionHandler.this.localOption);
			}

			@Override
			public Level getLogLevel() {
				if (this.options.has(RunOptionHandler.this.verboseOption)) {
					return Level.FINEST;
				}
				if (this.options.has(RunOptionHandler.this.quietOption)) {
					return Level.OFF;
				}
				return Level.INFO;
			}

			@Override
			public String getClasspath() {
				if (this.options.has(RunOptionHandler.this.classpathOption)) {
					return this.options.valueOf(RunOptionHandler.this.classpathOption);
				}
				return "";
			}

		}
	}

}
