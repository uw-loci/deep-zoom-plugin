/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import loci.plugin.ImageWrapper;

/**
 *
 * @author Aivar Grislis
 */
public interface IModule {

    /**
     * Gets name of module.
     *
     * @return
     */
    public String getName();

    /**
     * Sets name of module.
     *
     * @param name
     */
    public void setName(String name);

    /**
     * Saves module as XML string representation.
     *
     * @return
     */
    String toXML();

    /**
     * Restores module from XML string representation.
     *
     * @param xml
     * @return whether successfully parsed
     */
    boolean fromXML(String xml);

    /**
     * Gets input image names.
     *
     * @return
     */
    public String[] getInputNames();

    /**
     * Gets output names.
     *
     * @return
     */
    public String[] getOutputNames();

    /**
     * Furnish default input image
     *
     * @param image
     * @param name
     */
    public void input(ImageWrapper image);

    /**
     * Furnish named input image
     *
     * @param image
     * @param name
     */
    public void input(ImageWrapper image, String name);

    /**
     * Listen for default output image.
     *
     * @param name
     * @param listener
     */
    public void setOutputListener(IOutputListener listener);

    /**
     * Listen for named output image.
     *
     * @param name
     * @param listener
     */
    public void setOutputListener(String name, IOutputListener listener);
}
