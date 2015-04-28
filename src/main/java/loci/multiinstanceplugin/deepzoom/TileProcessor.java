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

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;
import loci.deepzoom.plugin.ImageWrapper;
import loci.deepzoom.plugin.annotations.Input;
import loci.multiinstanceplugin.AbstractPlugin;
import loci.multiinstanceplugin.IPlugin;

/**
 * XInnerX class that chains from the CutTileProcessor.  Writes out the tiles
 * in appropriate folder with appropriate file name.
 *
 * Handles PNG and JPG.
 */
@Input
public class TileProcessor extends AbstractPlugin implements IPlugin
{
    //TODO
    /*
     * Here is a simple kludge to get around a problem.
     *
     * Formerly TileProcessor was an inner class to DeepZoomExporter so it could see
     * the member variables m_folder and m_name and the static constants FILES_SUFFIX
     * and FORMAT.
     *
     * Unfortunately the Class.newInstance() method I am using to launch the plugins
     * within PluginLauncher won't instantiate an inner class (even though inner class
     * is public with public constructor).
     *
     * So I need some way of passing these settings to this tile processor.
     *
     * One way would be to implement TileProcessor as some new sort of ImageListener, not an
     * IPlugin.  Then it could go back to being an inner class, doesn't get instantiated by
     * PluginLauncher.
     *
     * Another approach is to feed LinkedPlugin settings.  These could be name/value pairs that
     * are subsequently available to new instantiations.  We do need such a scheme in general to
     * pass settings around.
     *
     * Lastly this approach just stuffs the settings into this class as static variables.
     */
    public static String s_folder = null;
    public static String s_name = null;
    public static String s_suffix = null;
    public static String s_format = null;

    public TileProcessor()
    {
    }

    public void process() {
        ImageWrapper image = get();

        int level = ((Integer)image.getProperties().get(DeepZoomExporter.LEVEL)).intValue();
        int xTile = ((Integer)image.getProperties().get(CutTilesProcessor.X)).intValue();
        int yTile = ((Integer)image.getProperties().get(CutTilesProcessor.Y)).intValue();

        //TODO OLD String fileName = m_folder + '/' + m_name + FILES_SUFFIX + '/' + level + '/' + xTile + '_' + yTile + '.' + FORMAT;
        String fileName = s_folder + '/' + s_name + s_suffix + '/' + level + '/' + xTile + '_' + yTile + '.' + s_format;
        ImageProcessor imageProcessor = image.getImageProcessor();
        ImagePlus imagePlus = new ImagePlus("tile", imageProcessor);
        FileSaver fileSaver = new FileSaver(imagePlus);
        //TODO OLD if (FORMAT.equals("png")) {
        if ("png".equals(s_format)) {
            fileSaver.saveAsPng(fileName);
        }
        //TODO OLD else if (FORMAT.equals("jpg")) {
        else if ("jpg".equals(s_format)) {
            fileSaver.saveAsJpeg(fileName);
        }
        else {
            //TODO OLD System.out.println("UNKNOWN FORMAT " + FORMAT);
            System.out.println("Unknown format " + s_format);
        }
    }
}

