/*
Deep Zoom Plugin.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
  * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
  * Neither the name of the UW-Madison LOCI nor the
    names of its contributors may be used to endorse or promote products
    derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.deepzoomplugin;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.util.prefs.Preferences;

/**
 *
 * @author Aivar Grislis
 */
public class DeepZoom implements PlugIn {
    private static final String OUTPUT = "OUTPUT";
    private static final String NAME ="NAME";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String WIDTH = "WIDTH";
    private static final String HEIGHT = "HEIGHT";
    private static final String LAUNCH = "LAUNCH";
    private static final String URL = "URL";
    private enum Implementation { CHAINED, MULTITHREADED, SINGLETHREADED };
    private static final String[] m_choices = { Implementation.CHAINED.name(), Implementation.MULTITHREADED.name() };
    private Preferences m_prefs = Preferences.userRoot().node(this.getClass().getName());

    public void run(String arg) {
        process(Implementation.CHAINED);
    }

    private void process(Implementation implementation)
    {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (null == imp) {
            return;
        }
        ImageProcessor ip = imp.getChannelProcessor();

        String folder = m_prefs.get(OUTPUT, "");
        String name = m_prefs.get(NAME, "image");
        String description = m_prefs.get(DESCRIPTION, "Zoomable image");
        int width = m_prefs.getInt(WIDTH, 640);
        int height = m_prefs.getInt(HEIGHT, 480);
        boolean launch = m_prefs.getBoolean(LAUNCH, true);
        String url = m_prefs.get(URL, "");

        GenericDialog dialog = new GenericDialog("Save Image to Deep Zoom");
        dialog.addStringField("Output folder: ", folder);
        dialog.addStringField("HTML file name: ", name);
        dialog.addStringField("HTML title: ", description);
        dialog.addNumericField("Image window width: ", width, 0);
        dialog.addNumericField("Image window height: ", height, 0);
        dialog.addCheckbox("Launch browser: ", launch);
        dialog.addStringField("URL (optional): ", url);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }

        folder = dialog.getNextString();
        name = dialog.getNextString();
        description = dialog.getNextString();
        width = (int) dialog.getNextNumber();
        height = (int) dialog.getNextNumber();
        launch = dialog.getNextBoolean();
        url = dialog.getNextString();

        m_prefs.put(OUTPUT, folder);
        m_prefs.put(NAME, name);
        m_prefs.put(DESCRIPTION, description);
        m_prefs.putInt(WIDTH, width);
        m_prefs.putInt(HEIGHT, height);
        m_prefs.putBoolean(LAUNCH, launch);
        m_prefs.put(URL, url);

        switch (implementation) {
            case CHAINED:
            case MULTITHREADED:
            case SINGLETHREADED:
                loci.chainableplugin.deepzoom.DeepZoomExporter deepZoomExporter = new loci.chainableplugin.deepzoom.DeepZoomExporter(launch, false, folder, url, name, description, width, height);
                loci.chainableplugin.ImageWrapper imageWrapper = new loci.chainableplugin.ImageWrapper(ip);
                deepZoomExporter.process(imageWrapper);
                break;
        }
    }

    /**
     * Main method used for testing only.  This allows the tester to compare
     * different implementations of the plugin that use different frameworks
     * to chain the processor components together.
     *
     * @param args the command line arguments
     */
    public static void main(String [] args)
    {
        new ImageJ();

        // ask for file to load
        GenericDialog dialog = new GenericDialog("Choose Image");
        dialog.addStringField("File: ", "");
        dialog.addChoice("Implementation: ", m_choices, Implementation.CHAINED.toString());
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }
        String file = dialog.getNextString();
        int choiceIndex = dialog.getNextChoiceIndex();
        
        IJ.open(file);

        ImagePlus imp = WindowManager.getCurrentImage();
        imp.hide();
        System.out.println("imp size is " + imp.getStackSize());
        System.out.println("imp type is " + imp.getType());
        //ij.process.ImageConverter converter = new ij.process.ImageConverter(imp); //FAIL for some reason the image type is IMGLIB and conversion fails
        //converter.convertRGBStackToRGB();
        convertRGBStackToRGB(imp);
        imp.show();

        // run plugin
        DeepZoom plugin = new DeepZoom();
        plugin.process(Implementation.values()[choiceIndex]);

        System.exit(0); //TODO just for testing.
    }

    /**
     * This is just a hack to make the main method work:
     */

    	/** Converts a 2 or 3 slice 8-bit stack to RGB. */
	public static void convertRGBStackToRGB(ImagePlus imp) {
		int stackSize = imp.getStackSize();
                int type = imp.getType();
		//if (stackSize<2 || stackSize>3 || type!=ImagePlus.GRAY8)       //FAIL, the ImageConverter version encounters ImagePlus.IMGLIB == 5
		//	throw new IllegalArgumentException("2 or 3 slice 8-bit stack required");
		int width = imp.getWidth();
		int height = imp.getHeight();
		ij.ImageStack stack = imp.getStack();
		byte[] R = (byte[])stack.getPixels(1);
		byte[] G = (byte[])stack.getPixels(2);
		byte[] B;
		if (stackSize>2)
			B = (byte[])stack.getPixels(3);
		else
			B = new byte[width*height];
		imp.trimProcessor();
		ij.process.ColorProcessor cp = new ij.process.ColorProcessor(width, height);
		cp.setRGB(R, G, B);
		if (imp.isInvertedLut())
			cp.invert();
		imp.setImage(cp.createImage());
		imp.killStack();
	}

}