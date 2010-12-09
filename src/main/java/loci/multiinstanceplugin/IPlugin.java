/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multiinstanceplugin;

import loci.plugin.ImageWrapper;

/**
 *
 * @author Aivar Grislis
 */
public interface IPlugin {

    /**
     * Gets the default input image from previous in chain.  Called from subclass.
     *
     * @return image
     */
    ImageWrapper get();

    /**
     * Gets a named input image from previous in chain.  Called from subclass.
     *
     * @param inName
     * @return image
     */
    ImageWrapper get(String inName);

    /**
     * This is the body of the plugin, defined in subclass.
     */
    void process();

    /**
     * Puts the default output image to next in chain (if any).  Called from subclass.
     *
     * @param image
     */
    void put(ImageWrapper image);

    /**
     * Puts named output image to next in chain (if any).  Called from subclass.
     *
     * @param outName
     * @param image
     */
    void put(String outName, ImageWrapper image);
}
