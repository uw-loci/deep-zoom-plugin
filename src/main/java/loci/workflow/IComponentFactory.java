/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import loci.util.xmllight.XMLException;

/**
 *
 * @author Aivar Grislis
 */
public interface IComponentFactory {

    /**
     * Creates a component from XML.
     *
     * @param xml
     * @return
     */
    public IComponent create(String xml) throws XMLException;
}
