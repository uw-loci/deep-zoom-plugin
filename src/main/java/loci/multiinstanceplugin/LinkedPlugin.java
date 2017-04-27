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

import java.util.Set;

import loci.deepzoom.plugin.ImageWrapper;
import loci.deepzoom.plugin.annotations.Input;
import loci.deepzoom.plugin.annotations.Output;

/**
 * The LinkedPlugin represents a given plugin class in a given stage of a
 * pipeline. It listens for the set of input images that is required and
 * launches instances of the plugin once that set is acquired.
 *
 * @author Aivar Grislis
 */
public class LinkedPlugin implements ILinkedPlugin {

	PluginAnnotations m_annotations;
	IPluginLauncher m_launcher;

	/**
	 * Create an instance for a given class name.
	 */
	LinkedPlugin(final String className) throws PluginClassException {

		// get associated class
		Class<?> pluginClass = null;
		try {
			pluginClass = Class.forName(className);
		}
		catch (final ClassNotFoundException e) {
			// class cannot be located
			System.out.println("Can't find " + className);
		}
		catch (final ExceptionInInitializerError e) {
			// initialization provoked by this method fails
			System.out.println("Error initializing " + className + " " +
				e.getStackTrace());
		}
		catch (final LinkageError e) {
			// linkage fails
			System.out
				.println("Linkage error " + className + " " + e.getStackTrace());
		}

		// validate class
		boolean success = false;
		if (null != pluginClass) {
			success = true;

			if (!pluginClass.isAssignableFrom(AbstractPlugin.class)) {
				success = false;
				System.out.println("Plugin should extend AbstractPlugin");
			}

			if (!pluginClass.isAssignableFrom(IPlugin.class)) {
				success = false;
				System.out.println("Plugin should implement IPlugin");
			}
		}

		if (success) {
			init(pluginClass);
		}
		else {
			throw new PluginClassException("Plugin class is invalid " + className);
		}
	}

	/**
	 * Create an instance for a given class.
	 */
	public LinkedPlugin(final Class<?> pluginClass) {
		init(pluginClass);
	}

	/**
	 * Helper function for constructors.
	 */
	private void init(final Class<?> pluginClass) {
		// examine annotations
		m_annotations = new PluginAnnotations(pluginClass);

		// create launcher
		m_launcher = new PluginLauncher(pluginClass, m_annotations);
	}

	/**
	 * Gets the set of annotated input names.
	 *
	 * @return set of names
	 */
	@Override
	public Set<String> getInputNames() {
		return m_annotations.getInputNames();
	}

	/**
	 * Gets the set of annotated output names.
	 *
	 * @return set of names
	 */
	@Override
	public Set<String> getOutputNames() {
		return m_annotations.getOutputNames();
	}

	/**
	 * Chains default output of this node to default input of next.
	 */
	@Override
	public void chainNext(final ILinkedPlugin next) {
		chainNext(Output.DEFAULT, next, Input.DEFAULT);
	}

	/**
	 * Chains named output of this node to default input of next.
	 */
	@Override
	public void chainNext(final String outName, final ILinkedPlugin next) {
		chainNext(outName, next, Input.DEFAULT);
	}

	/**
	 * Chains default output of this node to named input of next.
	 */
	@Override
	public void chainNext(final ILinkedPlugin next, final String inName) {
		chainNext(Output.DEFAULT, next, inName);
	}

	/**
	 * Chains named output of this node to named output of next.
	 */
	@Override
	public void chainNext(final String outName, final ILinkedPlugin next,
		final String inName)
	{
		m_launcher.chainNext(outName, next.getLauncher(), inName);
	}

	/**
	 * Chains default input of this node to default output of previous.
	 */
	@Override
	public void chainPrevious(final ILinkedPlugin previous) {
		chainPrevious(Input.DEFAULT, previous, Output.DEFAULT);
	}

	/**
	 * Chains named input of this node to default output of previous.
	 */
	@Override
	public void chainPrevious(final String inName, final ILinkedPlugin previous) {
		chainPrevious(inName, previous, Output.DEFAULT);
	}

	/**
	 * Chains default input of this node to named output of previous.
	 */
	@Override
	public void chainPrevious(final ILinkedPlugin previous, final String outName)
	{
		chainPrevious(Input.DEFAULT, previous, outName);
	}

	/**
	 * Chains named input of this node to named output of previous.
	 */
	@Override
	public void chainPrevious(final String inName, final ILinkedPlugin previous,
		final String outName)
	{
		m_launcher.chainPrevious(inName, previous.getLauncher(), outName);
	}

	/**
	 * Used to put default image from outside the plugin. An external put provides
	 * image for an internal get from within this plugin.
	 */
	@Override
	public void externalPut(final ImageWrapper image) {
		externalPut(Input.DEFAULT, image);
	}

	/**
	 * Used to put named image from outside the plugin. Am external put provides
	 * image for an internal get from within this plugin.
	 */
	@Override
	public void externalPut(final String inName, final ImageWrapper image) {
		m_launcher.externalPut(inName, image);
	}

	/**
	 * Gets the plugin launcher for this linked plugin.
	 *
	 * @return launcher
	 */
	@Override
	public IPluginLauncher getLauncher() {
		return m_launcher;
	}

	/**
	 * Quits processing images.
	 */
	@Override
	public void quit() {
		m_launcher.quit();
	}
}
