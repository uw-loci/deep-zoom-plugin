/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 *
 * @author aivar
 */
public class ComponentFactory implements IModuleFactory {
    private static ComponentFactory s_instance = null;

    private ComponentFactory() {
    }

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
    public IModule create(String xml) {
        Component component = new Component();
        component.fromXML(xml);
        return component;
    }
}
