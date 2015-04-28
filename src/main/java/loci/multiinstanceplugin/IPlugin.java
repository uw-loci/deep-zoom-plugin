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

import loci.deepzoom.plugin.ImageWrapper;

/**
 * Interface for a Plugin. Defines the main processing method that a plugin
 * implements, as well as methods that are available to the plugin internally to
 * get and put named images.
 *
 * @author Aivar Grislis
 */
public interface IPlugin {

	/**
	 * Gets the default input image from previous in chain. Called from within
	 * implementation.
	 *
	 * @return image
	 */
	ImageWrapper get();

	/**
	 * Gets a named input image from previous in chain. Called from within
	 * implemenation.
	 *
	 * @param inName
	 * @return image
	 */
	ImageWrapper get(String inName);

	/**
	 * This is the body of the plugin, defined in implemenation.
	 */
	void process();

	/**
	 * Puts the default output image to next in chain (if any). Called from within
	 * implemenation.
	 *
	 * @param image
	 */
	void put(ImageWrapper image);

	/**
	 * Puts named output image to next in chain (if any). Called from within
	 * implementation.
	 *
	 * @param outName
	 * @param image
	 */
	void put(String outName, ImageWrapper image);
}
