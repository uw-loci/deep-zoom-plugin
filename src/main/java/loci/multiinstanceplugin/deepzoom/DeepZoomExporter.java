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

package loci.multiinstanceplugin.deepzoom;

import com.centerkey.utils.BareBonesBrowserLaunch;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import loci.deepzoom.plugin.ImageWrapper;
import loci.deepzoom.plugin.annotations.Img;
import loci.deepzoom.plugin.annotations.Input;
import loci.deepzoom.plugin.annotations.Output;
import loci.multiinstanceplugin.AbstractPlugin;
import loci.multiinstanceplugin.ILinkedPlugin;
import loci.multiinstanceplugin.LinkedPlugin;

/**
 * TODO
 *
 * @author Aivar Grislis
 */
public class DeepZoomExporter // TODO this itself might be a plugin; s/b easy to
															// make a pipeline into a plugin
{

	static final String LEVEL = DeepZoomExporter.class + "_LEVEL";
	static final String FILES_SUFFIX = "_files";
	static final String HTML_SUFFIX = ".html";
	static final String DZI_SUFFIX = ".dzi";
	static final String CRLF = "\r\n";
	static final String FORMAT = "png";
	static final int TILESIZE = 256;
	static final int OVERLAP = 1;
	boolean m_launch;
	boolean m_silverlight;
	String m_folder;
	String m_url;
	String m_name;
	String m_description;
	int m_width;
	int m_height;

	/**
	 * Creates an instance to export a particular image.
	 *
	 * @param launch whether or not to launch a browser window to view results
	 * @param silverlight whether or not to use Microsoft Silverlight (tm)
	 *          technology to view the image
	 * @param folder output folder
	 * @param url when launching a browser, url to use; otherwise uses file
	 * @param name of HMTL file
	 * @param description that goes in HTML page title
	 * @param width of image window
	 * @param height of image window
	 */
	public DeepZoomExporter(final boolean launch, final boolean silverlight,
		final String folder, final String url, final String name,
		final String description, final int width, final int height)
	{
		m_launch = launch;
		m_silverlight = silverlight;
		m_folder = new File(folder).getAbsolutePath();
		m_url = url;
		m_name = name;
		m_description = description;
		m_width = width;
		m_height = height;
	}

	/**
	 * Processes a given image.
	 * 
	 * @param image
	 * @return
	 */
	public int process(final ImageWrapper image) {
		System.out.println("Multiple Instance Plugin Chaining");

		// Calculate the number of levels needed to get down to a single pixel.
		//
		// Note that the total number of levels is not specified in the HTML or
		// XML but implicitly derived from the image size. If the number of
		// levels generated is off the image will not display correctly.
		// In practice I experimentally determined we could omit the tiles
		// for folders 0..5 on a 13 folder image. In that case the level 6
		// tiles, although still tiny, were used to make a smooth animation when
		// drawing the image initially in the SeaDragon JavaScript.
		//
		final int xLevels = getLevels(image.getWidth(), 1);
		final int yLevels = getLevels(image.getHeight(), 1);
		final int levels = Math.max(xLevels, yLevels);
		image.getProperties().set(LEVEL, Integer.valueOf(levels));

		for (int level = 0; level <= levels; level++) {
			final File newFolder =
				new File(m_folder + '/' + m_name + FILES_SUFFIX + '/' + level);
			System.out.println("Create folder " + level + ' ' + newFolder.mkdirs());
		}

		final String xml =
			createDZI(m_folder, m_name, 256, 1, FORMAT, image.getWidth(), image
				.getHeight());

		createHTML(m_silverlight, m_folder, m_name, m_description, m_width,
			m_height, xml);

		// here the processors are chained together
		final ILinkedPlugin levelPlugin = new LinkedPlugin(LevelProcessor.class);
		final ILinkedPlugin cutTilesPlugin =
			new LinkedPlugin(CutTilesProcessor.class);
		final ILinkedPlugin tilePlugin = new LinkedPlugin(TileProcessor.class);
		final ILinkedPlugin halfPlugin =
			new LinkedPlugin(ScaleInHalfProcessor.class);

		levelPlugin.chainNext(LevelProcessor.TILE, cutTilesPlugin);
		cutTilesPlugin.chainNext(tilePlugin);

		levelPlugin.chainNext(LevelProcessor.HALF, halfPlugin);
		halfPlugin.chainNext(levelPlugin); // recursive

		// TODO this is a major kludge for now
		// this was formely an inner class that could see these member variables
		// we need a way to pass settings to the plugin classes that get
		// instantiated
		// these settings will be unique per LinkedPlugin instance
		TileProcessor.s_folder = m_folder;
		TileProcessor.s_name = m_name;
		TileProcessor.s_suffix = FILES_SUFFIX;
		TileProcessor.s_format = FORMAT;

		// run the processing chain with our input image
		levelPlugin.externalPut(image);

		if (m_launch) {
			launchBrowser(m_folder, m_url, m_name);
		}

		// TODO no way of knowing when the pipeline finishes right now!!
		// (which also means no timing benchmarks yet)
		try {
			Thread.sleep(60000);
		}
		catch (final InterruptedException e) {

		}
		return 0;
	}

	/**
	 * Calculates how many levels of halving the image are necesary.
	 *
	 * @param srcWidth initial width
	 * @param dstWidth required width
	 * @return number of levels
	 */
	private int getLevels(int srcWidth, final int dstWidth) {
		int level = 1;
		while (srcWidth > dstWidth) {
			srcWidth /= 2;
			level++;
		}
		return level;
	}

	/**
	 * Creates and writes out a HTML file.
	 *
	 * @param silverlight whether or not to use Microsoft Silverlight (tm)
	 *          technology to view the image
	 * @param folder folder to write to
	 * @param name name of HTML file
	 * @param description HTML title
	 * @param width window width
	 * @param height window height
	 * @param xml XML incantation that describes the image
	 */
	private void createHTML(final boolean silverlight, final String folder,
		final String name, final String description, final int width,
		final int height, final String xml)
	{
		final File file = new File(folder + '/' + name + HTML_SUFFIX);
		PrintStream printStream;
		try {
			printStream = new PrintStream(new FileOutputStream(file));
		}
		catch (final FileNotFoundException e) {
			System.out.println("FILE NOT FOUND " + e);
			return;
		}

		if (silverlight) {
			createSilverlightHTML(printStream, name, description, width, height, xml);
		}
		else {
			createSeadragonHTML(printStream, name, description, width, height, xml);
		}
		printStream.close();
	}

	private void createSilverlightHTML(final PrintStream printStream,
		final String name, final String description, final int width,
		final int height, final String xml)
	{
		// TODO
	}

	/**
	 * Writes HTML to use Seadragon technology.
	 *
	 * @param printStream
	 * @param name
	 * @param description
	 * @param width
	 * @param height
	 * @param xml
	 */
	private void createSeadragonHTML(final PrintStream printStream,
		final String name, final String description, final int width,
		final int height, final String xml)
	{
		printStream.print("<!DOCTYPE html>\r\n");
		printStream.print("<html>\r\n");
		printStream.print(" <head>\r\n");
		printStream.print("  <title>" + m_description + "</title>" + "\r\n");
		printStream
			.print("  <script type=\"text/javascript\" src=\"http://seadragon.com/ajax/0.8/seadragon-min.js\">\r\n");
		printStream.print("  </script>\r\n");
		printStream.print("  <script type=\"text/javascript\">\r\n");
		printStream.print("    var viewer = null;\r\n");
		printStream.print("    function init() {\r\n");
		printStream.print("     viewer = new Seadragon.Viewer(\"container\");\r\n");
		printStream.print("     viewer.openDzi(\"" + name + ".xml\",'" + xml +
			"');" + "\r\n");
		printStream.print("    }\r\n");
		printStream
			.print("    Seadragon.Utils.addEvent(window, \"load\", init);\r\n");
		printStream.print("  </script>\r\n");
		printStream.print("  <style type=\"text/css\">\r\n");
		printStream.print("    #container {\r\n");
		printStream.print("     width: " + width + "px;" + "\r\n");
		printStream.print("     height: " + height + "px;" + "\r\n");
		printStream.print("     background-color: black;\r\n");
		printStream.print("     border: 1px solid black;\r\n");
		printStream.print("     color: white;\r\n");
		printStream.print("    }\r\n");
		printStream.print("   </style>\r\n");
		printStream.print("  </head>\r\n");
		printStream.print("  <body bgcolor=\"#000000\">\r\n");
		printStream.print("   <div id=\"container\"></div>\r\n");
		printStream.print("  </body>\r\n");
		printStream.print("</html>\r\n");
	}

	/**
	 * Writes XML incantation file describing the tiled image. Returns the
	 * incantation to be embedded in the HTML also.
	 *
	 * @param folder
	 * @param name
	 * @param tileSize
	 * @param overlap
	 * @param format
	 * @param width
	 * @param height
	 * @return XML
	 */
	private String createDZI(final String folder, final String name,
		final int tileSize, final int overlap, final String format,
		final int width, final int height)
	{
		final String xml =
			"<?xml version=\"1.0\" encoding=\"utf-8\"?><Image TileSize=\"" +
				tileSize + "\"" + " Overlap=\"" + overlap + "\"" + " Format=\"" +
				format + "\"" + " ServerFormat=\"Default\"" +
				" xmlns=\"http://schemas.microsoft.com/deepzoom/\"" + ">" +
				" <Size Width=\"" + width + "\" Height=\"" + height + "\" />" +
				"</Image>";

		final File file = new File(folder + '/' + name + ".xml");
		PrintStream printStream;
		try {
			printStream = new PrintStream(new FileOutputStream(file));
		}
		catch (final FileNotFoundException e) {
			System.out.println("FILE NOT FOUND " + e);
			return null;
		}

		printStream.print(xml);
		printStream.close();

		return xml;
	}

	/**
	 * Launches a browser window with the tiled image.
	 *
	 * @param folder file folder to use
	 * @param url URL to use; may be empty string, then use file folder
	 * @param name name of HTML file
	 */
	private void launchBrowser(final String folder, final String url,
		final String name)
	{
		String destUrl = url;
		if (null == url || "".equals(url)) {
			destUrl =
				"file:///" + folder.replaceAll(" ", "%20") + '/' +
					name.replaceAll(" ", "%20") + HTML_SUFFIX;
		}
		else {
			if (!url.endsWith(HTML_SUFFIX)) {
				if (!url.endsWith("/")) {
					destUrl += '/';
				}
				destUrl += name + HTML_SUFFIX;
			}
		}
		BareBonesBrowserLaunch.openURL(destUrl);
	}

	/**
	 * Inner class that chains from the CutTileProcessor. Writes out the tiles in
	 * appropriate folder with appropriate file name. Handles PNG and JPG.
	 */
	@Input
	public class TileProcessorX extends AbstractPlugin {

		public TileProcessorX() {}

		@Override
		public void process() {
			final ImageWrapper image = get();

			final int level =
				((Integer) image.getProperties().get(DeepZoomExporter.LEVEL))
					.intValue();
			final int xTile =
				((Integer) image.getProperties().get(CutTilesProcessor.X)).intValue();
			final int yTile =
				((Integer) image.getProperties().get(CutTilesProcessor.Y)).intValue();

			final String fileName =
				m_folder + '/' + m_name + FILES_SUFFIX + '/' + level + '/' + xTile +
					'_' + yTile + '.' + FORMAT;
			final ImageProcessor imageProcessor = image.getImageProcessor();
			final ImagePlus imagePlus = new ImagePlus("tile", imageProcessor);
			final FileSaver fileSaver = new FileSaver(imagePlus);
			if (FORMAT.equals("png")) {
				fileSaver.saveAsPng(fileName);
			}
			else if (FORMAT.equals("jpg")) {
				fileSaver.saveAsJpeg(fileName);
			}
			else {
				System.out.println("UNKNOWN FORMAT " + FORMAT);
			}
		}
	}

	/**
	 * Inner class at the top of the image chain. Passes image on to
	 * CutTilesProcessor and also ScaleInHalfProcessor. Note that this assumes
	 * that the CutTilesProcessor handles the image in a read-only fashion. When
	 * we get back from the inital chain to the CutTilesProcessor all of the tiles
	 * for this image level will have been cut and written out.
	 */
	@Input
	@Output({ @Img(LevelProcessor.TILE), @Img(LevelProcessor.HALF) })
	public class LevelProcessorX extends AbstractPlugin {

		static final String TILE = "TILE";
		static final String HALF = "HALF";

		public LevelProcessorX() {}

		@Override
		public void process() {
			final ImageWrapper image = get();

			put(TILE, image);

			int level =
				((Integer) image.getProperties().get(DeepZoomExporter.LEVEL))
					.intValue();
			if (level > 0) {
				level--;
				image.getProperties().set(DeepZoomExporter.LEVEL, new Integer(level));

				put(HALF, image); // TODO we are assuming tile & halve are
													// non-destructive!!!!
			}
		}
	}
}
