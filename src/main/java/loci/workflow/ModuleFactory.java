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
public class ModuleFactory {
    private static ModuleFactory s_instance;
    
    private ModuleFactory() {
    }

    /**
     * Gets singleton instance.
     *
     * @return instance
     */
    public static synchronized ModuleFactory getInstance() {
        if (null == s_instance) {
            s_instance = new ModuleFactory();
        }
        return s_instance;
    }

    /**
     * Creates a component from XML.
     *
     * @param xml
     * @return
     */
    public static IModule create(String xml) throws XMLException {
        IModule module = null;
        XMLParser xmlHelper = new XMLParser();
        XMLTag tag = xmlHelper.getNextTag(xml);
        if (WorkFlow.WORKFLOW.equals(tag.getName())) {
            module = WorkFlowFactory.getInstance().create(xml);

        }
        else if (Component.COMPONENT.equals(tag.getName())) {
            module = ComponentFactory.getInstance().create(xml);
        }
        else {
            throw new XMLException("Invalid tag " + tag.getName());
        }
        return module;
    }
}
