/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multiinstanceplugin;

import java.util.Map;

import loci.plugin.ImageWrapper;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

/**
 *
 * @author aivar
 */
public abstract class AbstractPlugin implements IPluginInternal, IPlugin {
    Map<String, ImageWrapper> m_inputImages;
    Map<String, String> m_outputNames;

    /**
     * Starts up processing.  Called from plugin launcher.
     *
     * @param inputImages
     * @param outputNames
     */
    public void start(
            Map<String, ImageWrapper> inputImages,
            Map<String, String> outputNames) {
        m_inputImages = inputImages;
        m_outputNames = outputNames;

        process();

        m_inputImages = null;
    }

    /**
     * Gets the default input image from previous in chain.  Called from subclass.
     *
     * @return image
     */
    public ImageWrapper get() {
        return get(Input.DEFAULT);
    }

    /**
     * Gets a named input image from previous in chain.  Called from subclass.
     *
     * @param inName
     * @return image
     */
    public ImageWrapper get(String inName) {
        ImageWrapper input = m_inputImages.get(inName);
        if (null == input) {
            // run-time request disagrees with annotation
            PluginAnnotations.nameNotAnnotated(PluginAnnotations.InputOutput.INPUT, inName);
        }
        return input;
    }

    /**
     * Puts the default output image to next in chain (if any).  Called from subclass.
     *
     * @param image
     */
    public void put(ImageWrapper image) {
        put(Output.DEFAULT, image);
    }

    /**
     * Puts named output image to next in chain (if any).  Called from subclass.
     *
     * @param outName
     * @param image
     */
    public void put(String outName, ImageWrapper image) {
        //TODO how to check annotation?
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
        String fullInName = m_outputNames.get(outName);
        PluginScheduler.getInstance().put(fullInName, image);
    }
}
