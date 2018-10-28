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

package org.springframework.bootstrap.cli.compiler;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.bootstrap.cli.compiler.autoconfigure.SpringBootstrapCompilerAutoConfiguration;
import org.springframework.bootstrap.cli.compiler.autoconfigure.SpringMvcCompilerAutoConfiguration;

/**
 * Compiler for Groovy source files. Primarily a simple Facade for
 * {@link GroovyClassLoader#parseClass(File)} with the following additional features:
 * <ul>
 * <li>{@link CompilerAutoConfiguration} strategies will de applied during compilation</li>
 * 
 * <li>Multiple classes can be returned if the Groovy source defines more than one Class</li>
 * 
 * <li>Generated class files can also be loaded using
 * {@link ClassLoader#getResource(String)}</li>
 * <ul>
 * 
 * @author Phillip Webb
 */
public class GroovyCompiler {

	// FIXME could be a strategy
	private static final CompilerAutoConfiguration[] COMPILER_AUTO_CONFIGURATIONS = {
			new SpringBootstrapCompilerAutoConfiguration(),
			new SpringMvcCompilerAutoConfiguration(),
			new SpringBootstrapCompilerAutoConfiguration() };

	private GroovyCompilerConfiguration configuration;

	private ExtendedGroovyClassLoader loader;

	/**
	 * Create a new {@link GroovyCompiler} instance.
	 * @param configuration the compiler configuration
	 */
	public GroovyCompiler(final GroovyCompilerConfiguration configuration) {
		this.configuration = configuration;
		CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
		this.loader = new ExtendedGroovyClassLoader(getClass().getClassLoader(),
				compilerConfiguration);
		compilerConfiguration
				.addCompilationCustomizers(new CompilerAutoConfigureCustomizer());
	}

	/**
	 * Compile the specified Groovy source files, applying any
	 * {@link CompilerAutoConfiguration}s. All classes defined in the file will be
	 * returned from this method with the first item being the primary class (defined at
	 * the top of the file).
	 * @param file the file to compile
	 * @return compiled classes
	 * @throws CompilationFailedException
	 * @throws IOException
	 */
	public Class<?>[] compile(File file) throws CompilationFailedException, IOException {
		this.loader.clearCache();
		List<Class<?>> classes = new ArrayList<Class<?>>();
		Class<?> mainClass = this.loader.parseClass(file);
		for (Class<?> loadedClass : this.loader.getLoadedClasses()) {
			classes.add(loadedClass);
		}
		classes.remove(mainClass);
		classes.add(0, mainClass);
		return classes.toArray(new Class<?>[classes.size()]);
	}

	/**
	 * {@link CompilationCustomizer} to call {@link CompilerAutoConfiguration}s.
	 */
	private class CompilerAutoConfigureCustomizer extends CompilationCustomizer {

		public CompilerAutoConfigureCustomizer() {
			super(CompilePhase.CONVERSION);
		}

		@Override
		public void call(SourceUnit source, GeneratorContext context, ClassNode classNode)
				throws CompilationFailedException {
			ImportCustomizer importCustomizer = new ImportCustomizer();

			// Early sweep to get dependencies
			DependencyCustomizer dependencyCustomizer = new DependencyCustomizer(
					GroovyCompiler.this.loader);
			for (CompilerAutoConfiguration autoConfiguration : COMPILER_AUTO_CONFIGURATIONS) {
				if (autoConfiguration.matches(classNode)) {
					if (GroovyCompiler.this.configuration.isGuessDependencies()) {
						autoConfiguration.applyDependencies(dependencyCustomizer);
					}
				}
			}
			dependencyCustomizer.call();

			// Additional auto configuration
			for (CompilerAutoConfiguration autoConfiguration : COMPILER_AUTO_CONFIGURATIONS) {
				if (autoConfiguration.matches(classNode)) {
					if (GroovyCompiler.this.configuration.isGuessImports()) {
						autoConfiguration.applyImports(importCustomizer);
						importCustomizer.call(source, context, classNode);
					}
					if (source.getAST().getClasses().size() > 0
							&& classNode.equals(source.getAST().getClasses().get(0))) {
						autoConfiguration.applyToMainClass(GroovyCompiler.this.loader,
								GroovyCompiler.this.configuration, context, source,
								classNode);
					}
					autoConfiguration
							.apply(GroovyCompiler.this.loader,
									GroovyCompiler.this.configuration, context, source,
									classNode);
				}
			}
			importCustomizer.call(source, context, classNode);
		}

	}

}
