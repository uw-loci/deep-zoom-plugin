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

package loci.deepzoom.plugin;

import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import loci.chainableplugin.IPropertyCollection;
import loci.chainableplugin.PropertyCollection;

/**
 * Wrapper for an image.
 * <p>
 * This associates a set of String/Object property pairs with the image. Also
 * keeps track of width and height.
 * </p>
 * <p>
 * Note that ImagePlus keeps a set Java Properties and lots of ImageJ image
 * classes can give you width and height. I exposed these things in this
 * interface as they are the most important things about images for this demo
 * implementation.
 * </p>
 *
 * @author Aivar Grislis
 */
public class ImageWrapper {

	private final PropertyCollection m_properties = new PropertyCollection();
	private final ImageProcessor m_imageProcessor;
	private final String m_name;
	private final int m_width;
	private final int m_height;

	/**
	 * Creates an ImageWrapper based on an ImageJ ImageProcessor.
	 *
	 * @param imageProcessor
	 */
	public ImageWrapper(final ImageProcessor imageProcessor) {
		m_imageProcessor = imageProcessor;
		m_name = "input";
		m_width = imageProcessor.getWidth();
		m_height = imageProcessor.getHeight();
	}

	/**
	 * Creates an ImageWrapper based on another ImageWrapper.
	 *
	 * @param other
	 */
	public ImageWrapper(final ImageWrapper other) {
		m_imageProcessor = other.getImageProcessor();
		m_name = other.getName();
		m_width = other.getWidth();
		m_height = other.getHeight();
		m_properties.setAll(other.getProperties().getAll());
	}

	/**
	 * Creates an ImageWrapper for a new image based on another ImageWrapper. This
	 * implementation is very specific to the Deep Zoom plugin and just uses a
	 * ColorProcessor.
	 *
	 * @param other
	 * @param name new name
	 * @param width new width
	 * @param height new height
	 */
	public ImageWrapper(final ImageWrapper other, final String name,
		final int width, final int height)
	{
		m_imageProcessor = new ColorProcessor(width, height);
		m_name = name;
		m_width = width;
		m_height = height;
		Iterator iterator;
		if (null != other) {
			m_properties.setAll(other.getProperties().getAll());
			final Map map = other.getProperties().getAll();
			final Set keySet = map.keySet();
			iterator = keySet.iterator();
		}
	}

	/**
	 * Gets the underlying ImageJ ImageProcessor.
	 *
	 * @return ImageProcessor
	 */
	public ImageProcessor getImageProcessor() {
		return m_imageProcessor;
	}

	/**
	 * Gets the properties associated with this image.
	 *
	 * @return properties
	 */
	public IPropertyCollection getProperties() {
		return m_properties;
	}

	/**
	 * Gets the name of the image.
	 *
	 * @return name
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Gets the width of the image.
	 *
	 * @return width
	 */
	public int getWidth() {
		return m_width;
	}

	/**
	 * Gets the height of the image.
	 *
	 * @return height
	 */
	public int getHeight() {
		return m_height;
	}

	/**
	 * Gets the pixel array. This implementation is very specific to the Deep Zoom
	 * plugin and just uses an integer array.
	 *
	 * @return pixel array
	 */
	public int[] getPixels() {
		return (int[]) m_imageProcessor.getPixels();
	}

	/**
	 * Displayable info for the image.
	 *
	 * @return
	 */
	@Override
	public String toString() {
		return m_name + " " + m_width + " " + m_height;
	}
}
