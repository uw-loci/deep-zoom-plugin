/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import loci.util.xml.XMLParser;
import loci.util.xml.XMLException;
import loci.util.xml.XMLTag;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author aivar
 */
public class TestComponentFactory implements IComponentFactory {
    private static Map<String, IComponent> s_map = new HashMap<String, IComponent>();

    public IComponent create(String xml) throws XMLException {
        IComponent component = null;
        XMLParser xmlHelper = new XMLParser();
        XMLTag tag = xmlHelper.getNextTagInclusive(xml);
        if (WorkFlow.WORKFLOW.equals(tag.getName())) {
            component = WorkFlowFactory.create(tag.getContent());

        }
        else { //TODO if (WorkFlow.COMPONENT.equals(tag.getName())) {
            component = s_map.get(xml);
        }
        //else {
        //    throw new XMLException("Invalid tag " + tag.getName());
        //}
        return component;
    }

    void set(String xml, IComponent component) {
        System.out.println("put [" + xml + "] " + component.getName());
        s_map.put(xml, component);
    }
}
