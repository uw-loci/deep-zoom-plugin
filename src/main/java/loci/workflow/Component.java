/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import java.util.HashMap;
import java.util.Map;

import loci.plugin.ImageWrapper;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

/**
 *
 * @author Aivar Grislis
 */
public class Component implements IComponent {
    String m_name;
    String[] m_inputNames;
    String[] m_outputNames;
    Map<String, IOutputListener> m_listenerMap = new HashMap<String, IOutputListener>();

    /**
     * Gets name of component.
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets name of component.
     *
     * @param name
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Saves component as XML string representation.
     *
     * @return
     */
    public String toXML() {
        return null;
    }

    /**
     * Restores component from XML string representation.
     *
     * @param xml
     * @return whether successfully parsed
     */
    public boolean fromXML(String xml) {
        return true;
    }

    /**
     * Gets input image names.
     *
     * @return
     */
    public String[] getInputNames() {
        return m_inputNames;
    }

    /**
     * Gets output names.
     *
     * @return
     */
    public String[] getOutputNames() {
        return m_outputNames;
    }

    /**
     * Furnish input image.
     *
     * @param image
     */
    public void input(ImageWrapper image) {
        input(image, Input.DEFAULT);
    }

    /**
     * Furnish input image
     *
     * @param image
     * @param name
     */
    public void input(ImageWrapper image, String name) {

    }

    /**
     * Listen for output image.
     *
     * @param listener
     */
    public void setOutputListener(IOutputListener listener) {
        setOutputListener(Output.DEFAULT, listener);
    }

    /**
     * Listen for output image.
     *
     * @param name
     * @param listener
     */
    public void setOutputListener(String name, IOutputListener listener) {
        m_listenerMap.put(name, listener);
    }

}
