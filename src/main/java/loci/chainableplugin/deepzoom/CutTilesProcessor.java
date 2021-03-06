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
 * A processor that takes an image and chops it up into tiles.
 *
 * @author Aivar Grislis
 */
public class CutTilesProcessor extends AbstractPipelineProcessor {

	public static final String X = CutTilesProcessor.class + "_X";
	public static final String Y = CutTilesProcessor.class + "_Y";
	private static final int DEFAULT_TILE_WIDTH = 256;
	private static final int DEFAULT_TILE_HEIGHT = 256;
	int m_tileWidth;
	int m_tileHeight;
	int m_overlap;

	/**
	 * Default constructor.
	 */
	public CutTilesProcessor() {
		m_tileWidth = DEFAULT_TILE_WIDTH;
		m_tileHeight = DEFAULT_TILE_HEIGHT;
		m_overlap = 0;
	}

	/**
	 * Constructor to specify width, height, and overlap of tiles.
	 *
	 * @param tileWidth
	 * @param tileHeight
	 * @param overlap
	 */
	public CutTilesProcessor(final int tileWidth, final int tileHeight,
		final int overlap)
	{
		m_tileWidth = tileWidth;
		m_tileHeight = tileHeight;
		m_overlap = overlap;
	}

	/**
	 * Does the image processing.
	 *
	 * @param image
	 * @return status code
	 */
	@Override
	public int process(final ImageWrapper image) {
		final String name = image.getName();

		final int[] srcPixels = image.getPixels();
		final int srcRowSize = image.getWidth();

		int yTileNo = 0;
		int remainingHeight = image.getHeight();

		while (remainingHeight > m_overlap) {
			int tileHeight = m_tileHeight + m_overlap;
			if (tileHeight > remainingHeight) {
				tileHeight = remainingHeight;
			}

			int ySrc = yTileNo * m_tileHeight;
			if (ySrc > 0) {
				ySrc -= m_overlap;
				tileHeight += m_overlap;
			}

			int xTileNo = 0;
			int remainingWidth = image.getWidth();

			while (remainingWidth > m_overlap) {
				int tileWidth = m_tileWidth + m_overlap;
				if (tileWidth > remainingWidth) {
					tileWidth = remainingWidth;
				}

				int xSrc = xTileNo * m_tileWidth;
				if (xSrc > 0) {
					xSrc -= m_overlap;
					tileWidth += m_overlap;
				}

				final ImageWrapper imageTile =
					new ImageWrapper(image, name + "_tile_" + xTileNo + '_' + yTileNo,
						tileWidth, tileHeight);

				imageTile.getProperties().set(X, new Integer(xTileNo));
				imageTile.getProperties().set(Y, new Integer(yTileNo));

				final int srcIndex = xSrc + ySrc * srcRowSize;
				final int[] dstPixels = imageTile.getPixels();
				copyPixels(tileWidth, tileHeight, srcIndex, srcPixels, srcRowSize, 0,
					dstPixels, tileWidth);

				// hand off the image tile
				nextInChainProcess(imageTile);
				xTileNo++;
				remainingWidth -= m_tileWidth;
			}
			yTileNo++;
			remainingHeight -= m_tileHeight;
		}
		return 0;
	}

	/**
	 * Helper function, copies pixels from image to tile.
	 *
	 * @param width
	 * @param height
	 * @param srcIndex
	 * @param srcPixels
	 * @param srcRowSize
	 * @param dstIndex
	 * @param dstPixels
	 * @param dstRowSize
	 */
	void copyPixels(final int width, final int height, int srcIndex,
		final int[] srcPixels, final int srcRowSize, int dstIndex,
		final int[] dstPixels, final int dstRowSize)
	{
		for (int y = 0; y < height; y++) {
			int s = srcIndex;
			int d = dstIndex;
			for (int x = 0; x < width; x++) {
				dstPixels[(d++)] = srcPixels[(s++)];
			}
			srcIndex += srcRowSize;
			dstIndex += dstRowSize;
		}
	}
}
