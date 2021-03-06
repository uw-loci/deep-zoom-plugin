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

package loci.chainableplugin;

import loci.deepzoom.plugin.ImageWrapper;

/**
 * This is the abstract base class for a chainable processor.
 *
 * @author Aivar Grislis
 */
public abstract class AbstractPipelineProcessor implements IPipelineProcessor {

	IProcessor m_next;

	/**
	 * Chains this processor to another. Called externally from the processor
	 * before processing starts.
	 *
	 * @param next
	 */
	@Override
	public void chain(final IProcessor next) {
		this.m_next = next;
	}

	/**
	 * This is the abstract method that does the work, to be implemented in the
	 * concrete class processor.
	 *
	 * @param imageWrapper
	 * @return results code from processing
	 */
	@Override
	public abstract int process(ImageWrapper imageWrapper);

	/**
	 * This method passes on the image to the next processor.
	 *
	 * @param image
	 * @return results code from next processor
	 */
	public int nextInChainProcess(final ImageWrapper image) {
		return this.m_next.process(image);
	}
}
