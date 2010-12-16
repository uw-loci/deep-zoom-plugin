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
public interface IWorkFlowComponent {

    /**
     * Gets name of component.
     *
     * @return
     */
    public String getName();

    /**
     * Sets name of component.
     *
     * @param name
     */
    public void setName(String name);

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
     * Furnish input image
     *
     * @param image
     * @param name
     */
    public void input(ImageWrapper image, String name);

    /**
     * Listen for output image.
     *
     * @param name
     * @param listener
     */
    public void setOutputListener(String name, IOutputListener listener);
}
