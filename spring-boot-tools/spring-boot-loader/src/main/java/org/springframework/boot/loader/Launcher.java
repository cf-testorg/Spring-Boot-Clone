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

package org.springframework.boot.loader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.logging.Logger;

/**
 * Base class for launchers that can start an application with a fully configured
 * classpath.
 * 
 * @author Phillip Webb
 */
public abstract class Launcher {

	private Logger logger = Logger.getLogger(Launcher.class.getName());

	/**
	 * The main runner class. This must be loaded by the created ClassLoader so cannot be
	 * directly referenced.
	 */
	private static final String RUNNER_CLASS = Launcher.class.getPackage().getName()
			+ ".MainMethodRunner";

	/**
	 * Launch the application. This method is the initial entry point that should be
	 * called by a subclass {@code public static void main(String[] args)} method.
	 * @param args the incoming arguments
	 */
	public void launch(String[] args) {
		try {
			launch(args, getClass().getProtectionDomain());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Launch the application given the protection domain.
	 * @param args the incoming arguments
	 * @param protectionDomain the protection domain
	 * @throws Exception
	 */
	protected void launch(String[] args, ProtectionDomain protectionDomain)
			throws Exception {
		CodeSource codeSource = protectionDomain.getCodeSource();
		URI location = (codeSource == null ? null : codeSource.getLocation().toURI());
		String path = (location == null ? null : location.getPath());
		if (path == null) {
			throw new IllegalStateException("Unable to determine code source archive");
		}
		File root = new File(path);
		if (!root.exists()) {
			throw new IllegalStateException(
					"Unable to determine code source archive from " + root);
		}
		Archive archive = (root.isDirectory() ? new ExplodedArchive(root)
				: new JarFileArchive(root));
		launch(args, archive);
	}

	/**
	 * Launch the application given the archive file
	 * @param args the incoming arguments
	 * @param archive the underlying (zip/war/jar) archive
	 * @throws Exception
	 */
	protected void launch(String[] args, Archive archive) throws Exception {
		List<Archive> lib = new ArrayList<Archive>();
		for (Archive.Entry entry : archive.getEntries()) {
			if (isNestedArchive(entry)) {
				this.logger.fine("Adding: " + entry.getName());
				lib.add(archive.getNestedArchive(entry));
			}
		}

		this.logger.fine("Added " + lib.size() + " entries");
		postProcessLib(archive, lib);
		ClassLoader classLoader = createClassLoader(lib);
		launch(args, archive, classLoader);
	}

	/**
	 * Determine if the specified {@link JarEntry} is a nested item that should be added
	 * to the classpath. The method is called once for each entry.
	 * @param jarEntry the jar entry
	 * @return {@code true} if the entry is a nested item (jar or folder)
	 */
	protected abstract boolean isNestedArchive(Archive.Entry jarEntry);

	/**
	 * Called to post-process lib entries before they are used. Implementations can add
	 * and remove entries.
	 * @param archive the archive
	 * @param lib the existing lib
	 * @throws Exception
	 */
	protected void postProcessLib(Archive archive, List<Archive> lib) throws Exception {
	}

	/**
	 * Create a classloader for the specified lib.
	 * @param lib the lib
	 * @return the classloader
	 * @throws Exception
	 */
	protected ClassLoader createClassLoader(List<Archive> lib) throws Exception {
		URL[] urls = new URL[lib.size()];
		for (int i = 0; i < urls.length; i++) {
			urls[i] = lib.get(i).getUrl();
		}
		return createClassLoader(urls);
	}

	/**
	 * Create a classloader for the specified URLs
	 * @param urls the URLs
	 * @return the classloader
	 * @throws Exception
	 */
	protected ClassLoader createClassLoader(URL[] urls) throws Exception {
		return new LaunchedURLClassLoader(urls, getClass().getClassLoader());
	}

	/**
	 * Launch the application given the archive file and a fully configured classloader.
	 * @param args the incoming arguments
	 * @param archive the archive
	 * @param classLoader the classloader
	 * @throws Exception
	 */
	protected void launch(String[] args, Archive archive, ClassLoader classLoader)
			throws Exception {
		String mainClass = getMainClass(archive);
		Runnable runner = createMainMethodRunner(mainClass, args, classLoader);
		Thread runnerThread = new Thread(runner);
		runnerThread.setContextClassLoader(classLoader);
		runnerThread.setName(Thread.currentThread().getName());
		runnerThread.start();
	}

	/**
	 * Obtain the main class that should be used to launch the application. By default
	 * this method uses a {@code Start-Class} manifest entry.
	 * @param archive the archive
	 * @return the main class
	 * @throws Exception
	 */
	protected String getMainClass(Archive archive) throws Exception {
		String mainClass = archive.getManifest().getMainAttributes()
				.getValue("Start-Class");
		if (mainClass == null) {
			throw new IllegalStateException("No 'Start-Class' manifest entry specified");
		}
		return mainClass;
	}

	/**
	 * Create the {@code MainMethodRunner} used to launch the application.
	 * @param mainClass the main class
	 * @param args the incoming arguments
	 * @param classLoader the classloader
	 * @return a runnable used to start the application
	 * @throws Exception
	 */
	protected Runnable createMainMethodRunner(String mainClass, String[] args,
			ClassLoader classLoader) throws Exception {
		Class<?> runnerClass = classLoader.loadClass(RUNNER_CLASS);
		Constructor<?> constructor = runnerClass.getConstructor(String.class,
				String[].class);
		return (Runnable) constructor.newInstance(mainClass, args);
	}

	protected boolean isArchive(String name) {
		return name.endsWith(".jar") || name.endsWith(".zip");
	}

}
