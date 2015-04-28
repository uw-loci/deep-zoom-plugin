/*
 * #%L
 * Deep Zoom plugin for ImageJ.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.multiinstanceplugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import loci.deepzoom.plugin.ImageWrapper;

/**
 * The PluginLauncher talks to the PluginScheduler and launches new instances of
 * the plugin as needed.
 *
 * @author Aivar Grislis
 */
public class PluginLauncher implements IPluginLauncher {

	public static boolean s_singleInstance = false;
	UUID m_id = UUID.randomUUID();
	Class<?> m_pluginClass;
	PluginAnnotations m_annotations;
	Thread m_thread;
	volatile boolean m_quit = false;
	Map<String, String> m_outputNames = new HashMap<String, String>();

	/**
	 * Creates a launcher for a given class, that has the given input and output
	 * image name annotations.
	 *
	 * @param pluginClass
	 * @param annotations
	 */
	public PluginLauncher(final Class<?> pluginClass,
		final PluginAnnotations annotations)
	{
		m_pluginClass = pluginClass;
		m_annotations = annotations;
		m_thread = new LauncherThread();
		m_thread.setDaemon(true);
		m_thread.start();
	}

	/**
	 * Chains this launcher to next one.
	 *
	 * @param outName
	 * @param next
	 * @param inName
	 */
	@Override
	public void chainNext(final String outName, final IPluginLauncher next,
		final String inName)
	{
		PluginScheduler.getInstance().chain(this, outName, next, inName);
	}

	/**
	 * Chains this launcher to previous one.
	 *
	 * @param inName
	 * @param previous
	 * @param outName
	 */
	@Override
	public void chainPrevious(final String inName,
		final IPluginLauncher previous, final String outName)
	{
		PluginScheduler.getInstance().chain(previous, outName, this, inName);
	}

	/**
	 * Initiates a plugin chain by feeding a named image to this launcher's
	 * plugin.
	 *
	 * @param name
	 * @param image
	 */
	@Override
	public void externalPut(final String name, final ImageWrapper image) {
		final String fullInName = uniqueName(name);
		PluginScheduler.getInstance().put(fullInName, image);
	}

	/**
	 * Generates a unique input image name for this launcher.
	 *
	 * @param name
	 * @return
	 */
	@Override
	public String uniqueName(final String name) {
		return m_id.toString() + '-' + name;
	}

	/**
	 * Associates a unique input image name for some other launcher to our output
	 * image name.
	 *
	 * @param outName
	 * @param fullInName
	 */
	@Override
	public void associate(final String outName, final String fullInName) {
		m_outputNames.put(outName, fullInName);
	}

	/**
	 * Quits processing the chain.
	 */
	@Override
	public void quit() {
		m_quit = true;
	}

	/**
	 * Processing thread for launcher. Waits for a complete set of input images,
	 * then spawns a thread with a new instance of the plugin to process them.
	 */
	private class LauncherThread extends Thread {

		@Override
		public void run() {
			final Set<String> inputNames = m_annotations.getInputNames();
			while (!m_quit) {
				// assemble a set of input images
				final Map<String, ImageWrapper> inputImages =
					new HashMap<String, ImageWrapper>();
				for (final String inputName : inputNames) {
					final String fullInName = uniqueName(inputName);
					final ImageWrapper image =
						PluginScheduler.getInstance().get(fullInName);
					inputImages.put(inputName, image);
				}

				// TODO Good place to throttle thread creation here
				PluginScheduler.getInstance().reportNewPlugin(
					m_pluginClass.getSimpleName());

				// launch the plugin for this set of images
				final Thread pluginThread = new PluginThread(inputImages);
				pluginThread.start();

				// Only run one plugin instance at a time?
				if (s_singleInstance) { // TODO implemented in a quick & dirty way
					// wait for plugin to finish
					// (Note: this is all a kludge for now: you might as well just run the
					// plugin
					// on this launcher thread.)
					try {
						pluginThread.join();
					}
					catch (final InterruptedException e) {
						System.out.println("LauncherThread.run() insterrupted on join");
					}
				}
			}
		}
	}

	/**
	 * Processing thread for a plugin instance. Instantiates and runs the plugin.
	 */
	private class PluginThread extends Thread {

		Map<String, ImageWrapper> m_inputImages;
		IPluginInternal m_pluginInstance;

		PluginThread(final Map<String, ImageWrapper> inputImages) {
			m_inputImages = inputImages;
			m_pluginInstance = null;
			try {
				m_pluginInstance = (IPluginInternal) m_pluginClass.newInstance();
			}
			catch (final InstantiationException e) {
				System.out.println("Problem instantiating plugin " +
					m_pluginClass.getSimpleName() + ' ' + e.getMessage());
			}
			catch (final IllegalAccessException e) {
				System.out.println("Illegal access instantiating plugin " +
					m_pluginClass.getSimpleName() + ' ' + e.getMessage());
			}
		}

		@Override
		public void run() {
			if (null != m_pluginInstance) {
				m_pluginInstance.start(m_inputImages, m_outputNames);
			}
		}
	}
}
