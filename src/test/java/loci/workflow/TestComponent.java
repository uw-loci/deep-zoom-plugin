/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import loci.plugin.ImageWrapper;

/**
 *
 * @author aivar
 */
public class TestComponent implements IComponent {
    String m_name;
    String m_xml;
    String[] m_inputNames;
    String[] m_outputNames;

    // TEST SETUP METHODS

    public void setXML(String xml) {
        m_xml = xml;
    }

    public void setInputNames(String[] inputNames) {
        m_inputNames = inputNames;
    }

    public void setOutputNames(String[] outputNames) {
        m_outputNames = outputNames;
    }

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
        return m_xml;
    }

    /**
     * Restores component from XML string representation.
     *
     * @param xml
     * @return whether successfully parsed
     */
    public boolean fromXML(String xml) {
        boolean success = xml.equals(m_xml);
        Assert.assertTrue(success);
        return success;
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
     * @param name
     * @param listener
     */
    public void setOutputListener(String name, IOutputListener listener) {
        
    }

}
