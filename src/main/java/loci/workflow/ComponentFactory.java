/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import loci.util.xml.XMLParser;
import loci.util.xml.XMLException;
import loci.util.xml.XMLTag;

/**
 *
 * @author Aivar Grislis
 */
public class ComponentFactory {

    /**
     * Creates a component from XML.
     *
     * @param xml
     * @return
     */
    public static IComponent create(String xml) throws XMLException {
        IComponent component = null;
        XMLParser xmlHelper = new XMLParser();
        XMLTag tag = xmlHelper.getNextTagInclusive(xml);
        if (WorkFlow.WORKFLOW.equals(tag.getName())) {
            component = WorkFlowFactory.create(tag.getContent());

        }
        else if (WorkFlow.COMPONENT.equals(tag.getName())) {
            component = new Component();
            component.fromXML(tag.getContent());
        }
        else {
            throw new XMLException("Invalid tag " + tag.getName());
        }
        return component;
    }
}
