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

package loci.deepzoom.workflow.plugin;

import java.util.Map;

import loci.deepzoom.plugin.annotations.Input;
import loci.deepzoom.plugin.annotations.Output;

/**
 * Abstract base class for plugin. Starts up plugin processing, gets and puts
 * images for the plugin.
 *
 * @author Aivar Grislis
 */
public abstract class AbstractPlugin implements IPluginInternal, IPlugin {

	Map<String, ItemWrapper> m_inputImages;
	Map<String, String> m_outputNames;

	/**
	 * Starts up processing. Called from plugin launcher.
	 *
	 * @param inputImages maps each input name to an image
	 * @param outputNames maps each output name to a unique input name for the
	 *          next chained plugin.
	 */
	@Override
	public void start(final Map<String, ItemWrapper> inputImages,
		final Map<String, String> outputNames)
	{
		m_inputImages = inputImages;
		m_outputNames = outputNames;

		try {
			// do the actual work of the plugin
			process();
		}
		catch (final Exception e) {
			System.out.println("Plugin exception " + e.getMessage());
		}

		m_inputImages = null;
	}

	/**
	 * Gets the default input image from previous in chain. Called from subclass.
	 *
	 * @return image
	 */
	@Override
	public ItemWrapper get() {
		return get(Input.DEFAULT);
	}

	/**
	 * Gets a named input image from previous in chain. Called from subclass.
	 *
	 * @param inName
	 * @return image
	 */
	@Override
	public ItemWrapper get(final String inName) {
		final ItemWrapper input = m_inputImages.get(inName);
		if (null == input) {
			// run-time request disagrees with annotation
			PluginAnnotations.nameNotAnnotated(PluginAnnotations.InputOutput.INPUT,
				inName);
		}
		return input;
	}

	/**
	 * Puts the default output image to next in chain (if any). Called from
	 * subclass.
	 *
	 * @param image
	 */
	@Override
	public void put(final ItemWrapper image) {
		put(Output.DEFAULT, image);
	}

	/**
	 * Puts named output image to next in chain (if any). Called from subclass.
	 *
	 * @param outName
	 * @param image
	 */
	@Override
	public void put(final String outName, final ItemWrapper image) {
		// TODO how to check annotation? No longer visible from here.
		/*
		if (isAnnotatedName(InputOutput.OUTPUT, outName)) {
		    System.out.println("was annotated");
		    // anyone interested in this output data?
		    String fullName = m_map.get(outName);
		    System.out.println("full name is " + fullName);
		    if (null != fullName) {
		        // yes, pass it on
		        NodeScheduler.getInstance().put(fullName, data);
		    }
		}
		*/
		final String fullInName = m_outputNames.get(outName);
		PluginScheduler.getInstance().put(fullInName, image);
	}
}
