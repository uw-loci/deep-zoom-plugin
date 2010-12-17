/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import loci.util.xmllight.XMLParser;
import loci.util.xmllight.XMLException;
import loci.util.xmllight.XMLTag;

/**
 *
 * @author Aivar Grislis
 */
public class ComponentFactory {
    private static ComponentFactory s_instance;
    
    private ComponentFactory() {
    }

    /**
     * Gets singleton instance.
     *
     * @return instance
     */
    public static synchronized ComponentFactory getInstance() {
        if (null == s_instance) {
            s_instance = new ComponentFactory();
        }
        return s_instance;
    }

    /**
     * Creates a component from XML.
     *
     * @param xml
     * @return
     */
    public static IComponent create(String xml) throws XMLException {
        IComponent component = null;
        XMLParser xmlHelper = new XMLParser();
        XMLTag tag = xmlHelper.getNextTag(xml);
        if (WorkFlow.WORKFLOW.equals(tag.getName())) {
            component = WorkFlowFactory.getInstance().create(xml);

        }
        else if (WorkFlow.COMPONENT.equals(tag.getName())) {
            component = new Component();
            component.fromXML(xml);
        }
        else {
            throw new XMLException("Invalid tag " + tag.getName());
        }
        return component;
    }
}
