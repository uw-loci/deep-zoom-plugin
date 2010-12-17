/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author aivar
 */
public class TestComponentFactory implements IComponentFactory {
    Map<String, IComponent> m_map = new HashMap<String, IComponent>();

    public IComponent create(String xml) {
        System.out.println("create [" + xml + "] " + m_map.get(xml));
        return m_map.get(xml);
    }

    void set(String xml, IComponent component) {
        System.out.println("put [" + xml + "] " + component.getName());
        m_map.put(xml, component);
    }
}
