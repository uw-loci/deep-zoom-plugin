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

package loci.chainableplugin.deepzoom;

import loci.chainableplugin.AbstractPipelineProcessor;
import loci.deepzoom.plugin.ImageWrapper;

/**
 * A processor that takes an image and scales it in half. Implemented for 24-bit
 * RGBA images using integers for pixels.
 *
 * @author Aivar Grislis
 */
public class ScaleInHalfProcessor extends AbstractPipelineProcessor {

	int[] m_bucket = new int[4];

	@Override
	public int process(final ImageWrapper image) {
		final int srcWidth = image.getWidth();
		final int srcHeight = image.getHeight();
		final int dstWidth = (srcWidth + 1) / 2;
		final int dstHeight = (srcHeight + 1) / 2;
		final int[] srcPixels = image.getPixels();

		final ImageWrapper halvedImage =
			new ImageWrapper(image, image.getName() + "_halved", dstWidth, dstHeight);

		final int[] dstPixels = halvedImage.getPixels();

		for (int y = 0; y < dstHeight; y++) {
			for (int x = 0; x < dstWidth; x++) {
				zeroBuckets();

				int srcPixel = srcPixels[(2 * y * srcWidth + 2 * x)];
				addToBuckets(srcPixel);
				if (2 * x + 1 < srcWidth) {
					srcPixel = srcPixels[(2 * y * srcWidth + 2 * x + 1)];
					addToBuckets(srcPixel);
					if (2 * y + 1 < srcHeight) {
						srcPixel = srcPixels[((2 * y + 1) * srcWidth + 2 * x + 1)];
						addToBuckets(srcPixel);
					}
				}
				if (2 * y + 1 < srcHeight) {
					srcPixel = srcPixels[((2 * y + 1) * srcWidth + 2 * x)];
					addToBuckets(srcPixel);
				}
				dstPixels[(y * dstWidth + x)] = averageBuckets();
			}
		}
		return nextInChainProcess(halvedImage);
	}

	void zeroBuckets() {
		for (int i = 0; i < 4; i++)
			m_bucket[i] = 0;
	}

	void addToBuckets(final int pixel) {
		m_bucket[0] += ((pixel & 0xFF000000) >> 24);
		m_bucket[1] += ((pixel & 0xFF0000) >> 16);
		m_bucket[2] += ((pixel & 0xFF00) >> 8);
		m_bucket[3] += (pixel & 0xFF);
	}

	int averageBuckets() {
		final int pixel =
			m_bucket[0] >> 2 << 24 | m_bucket[1] >> 2 << 16 | m_bucket[2] >> 2 << 8 |
				m_bucket[3] >> 2;

		return pixel;
	}
}
