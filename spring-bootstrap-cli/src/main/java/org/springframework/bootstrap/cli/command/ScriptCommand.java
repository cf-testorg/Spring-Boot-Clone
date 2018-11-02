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
package org.springframework.bootstrap.cli.command;

import groovy.lang.Script;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.ivy.util.FileUtil;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.bootstrap.cli.Command;
import org.springframework.bootstrap.cli.compiler.GroovyCompiler;
import org.springframework.bootstrap.cli.compiler.GroovyCompilerConfiguration;

/**
 * {@link Command} to run a Groovy script.
 * 
 * @author Dave Syer
 * 
 */
public class ScriptCommand implements Command {

	private static String[] DEFAULT_PATHS = new String[] { "${SPRING_HOME}/ext",
			"${SPRING_HOME}/bin" };

	private String[] paths = DEFAULT_PATHS;

	private Class<?> mainClass;

	private Object main;

	private String name;

	public ScriptCommand(String script) {
		this.name = script;
	}

	@Override
	public String getName() {
		if (getMain() instanceof Command) {
			return ((Command) getMain()).getName();
		}
		return this.name;
	}

	@Override
	public String getDescription() {
		if (getMain() instanceof Command) {
			return ((Command) getMain()).getDescription();
		}
		return this.name;
	}

	@Override
	public String getHelp() {
		if (getMain() instanceof OptionHandler) {
			return ((OptionHandler) getMain()).getHelp();
		}
		if (getMain() instanceof Command) {
			return ((Command) getMain()).getHelp();
		}
		return null;
	}

	@Override
	public void run(String... args) throws Exception {
		if (getMain() instanceof Command) {
			((Command) getMain()).run(args);
		} else if (getMain() instanceof OptionHandler) {
			((OptionHandler) getMain()).run(args);
		} else if (this.main instanceof Runnable) {
			((Runnable) this.main).run();
		} else if (this.main instanceof Script) {
			Script script = (Script) this.main;
			script.setProperty("args", args);
			script.run();
		}
	}

	/**
	 * Paths to search for script files.
	 * 
	 * @param paths the paths to set
	 */
	public void setPaths(String[] paths) {
		this.paths = paths;
	}

	@Override
	public String getUsageHelp() {
		if (getMain() instanceof Command) {
			return ((Command) getMain()).getDescription();
		}
		return "[options] <args>";
	}

	protected Object getMain() {
		if (this.main == null) {
			try {
				this.main = getMainClass().newInstance();
			} catch (Exception e) {
				throw new IllegalStateException("Cannot create main class: " + this.name,
						e);
			}
		}
		return this.main;
	}

	private void compile() {
		GroovyCompiler compiler = new GroovyCompiler(new ScriptConfiguration());
		compiler.addCompilationCustomizers(new ScriptCompilationCustomizer());
		File source = locateSource(this.name);
		Class<?>[] classes;
		try {
			classes = compiler.compile(source);
		} catch (CompilationFailedException e) {
			throw new IllegalStateException("Could not compile script", e);
		} catch (IOException e) {
			throw new IllegalStateException("Could not compile script", e);
		}
		this.mainClass = classes[0];
	}

	private Class<?> getMainClass() {
		if (this.mainClass == null) {
			compile();
		}
		return this.mainClass;
	}

	private File locateSource(String name) {
		String resource = "commands/" + name + ".groovy";
		URL url = getClass().getClassLoader().getResource(resource);
		File file = null;
		if (url != null) {
			try {
				file = File.createTempFile(name, ".groovy");
				FileUtil.copy(url, file, null);
			} catch (IOException e) {
				throw new IllegalStateException("Could not create temp file for source: "
						+ name);
			}
		} else {
			String home = System.getProperty("SPRING_HOME", System.getenv("SPRING_HOME"));
			if (home == null) {
				home = ".";
			}
			for (String path : this.paths) {
				String subbed = path.replace("${SPRING_HOME}", home);
				File test = new File(subbed, resource);
				if (test.exists()) {
					file = test;
					break;
				}
			}
		}
		if (file == null) {
			throw new IllegalStateException("No script found for : " + name);
		}
		return file;
	}

	private static class ScriptConfiguration implements GroovyCompilerConfiguration {

		@Override
		public boolean isGuessImports() {
			return true;
		}

		@Override
		public boolean isGuessDependencies() {
			return true;
		}

	}

}
