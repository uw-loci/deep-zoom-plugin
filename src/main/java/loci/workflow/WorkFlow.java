/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import loci.plugin.ImageWrapper;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

/**
 *
 * @author Aivar Grislis
 */
public class WorkFlow implements IWorkFlowComponent {
    String m_name;
    Map<String, IWorkFlowComponent> m_componentMap = new HashMap<String, IWorkFlowComponent>();
    List<String> m_inputNames = new ArrayList<String>();
    List<String> m_outputNames = new ArrayList<String>();
    List<Chain> m_chains = new ArrayList<Chain>();
    Map<String, IWorkFlowComponent> m_inputComponents = new HashMap<String, IWorkFlowComponent>();
    Map<String, String> m_inputComponentNames = new HashMap<String, String>();
    Map<String, IOutputListener> m_listeners = new HashMap<String, IOutputListener>();

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String[] getInputNames() {
        return m_inputNames.toArray(new String[0]);
    }

    public String[] getOutputNames() {
        return m_outputNames.toArray(new String[0]);
    }

    public void setXML(String xml) {

    }

    public String getXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<workflow>\n");
        xml.append(" <name>");
        xml.append(getName());
        xml.append("</name>\n");
        xml.append("<components>\n");
       // for (IWorkFlowComponent component : m_componentMap.entrySet()) {

       // }
        xml.append("  <component>\n");
        xml.append("  </component>\n");
        xml.append("<chains>\n");
        for (Chain chain : m_chains) {
            xml.append("<chain>\n");
            xml.append("<source>\n");
            xml.append(chain.getSource().getName() + '\n');
            xml.append("<image>\n");
            xml.append(chain.getSourceName() + '\n');
            xml.append("</source>\n");
            xml.append("<dest>\n");
            xml.append(chain.getDest().getName() + '\n');
            xml.append("<image>\n");
            xml.append(chain.getDestName() + '\n');
            xml.append("</image>\n");
            xml.append("</dest>\n");
            xml.append("</chain>\n");
        }
        xml.append("</chains>\n");
        xml.append("</workflow>\n");
        return null;
    }

    public void add(IWorkFlowComponent component) {
        m_componentMap.put(component.getName(), component);
    }

    public void chain(IWorkFlowComponent source, IWorkFlowComponent dest) {
        chain(source, Output.DEFAULT, dest, Input.DEFAULT);
    }

    public void chain(IWorkFlowComponent source, String sourceName, IWorkFlowComponent dest) {
        chain(source, sourceName, dest, Input.DEFAULT);
    }

    public void chain(IWorkFlowComponent source, IWorkFlowComponent dest, String destName) {
        chain(source, Output.DEFAULT, dest, destName);
    }

    public void chain(IWorkFlowComponent source, String sourceName, IWorkFlowComponent dest, String destName) {
        Chain chain = new Chain(source, sourceName, dest, destName);
        m_chains.add(chain);
    }

    public void chainInput(String inName, IWorkFlowComponent dest) {
        chainInput(inName, dest, Input.DEFAULT);
    }

    public void chainInput(String inName, IWorkFlowComponent dest, String destName) {
        m_inputNames.add(inName);
        m_inputComponents.put(inName, dest);
        m_inputComponentNames.put(inName, destName);
    }

    public void chainOutput(IWorkFlowComponent source, String outName) {
        chainOutput(source, Output.DEFAULT, outName);
    }

    public void chainOutput(IWorkFlowComponent source, String sourceName, String outName) {
        m_outputNames.add(outName);
        //TODO set a listener, forwards to our listener, if any; source & sourceName might be misnomer
    }
    
    public void input(ImageWrapper image, String name) {
        if (m_inputNames.contains(name)) {
            IWorkFlowComponent dest = m_inputComponents.get(name);
            String destName = m_inputComponentNames.get(name);
            dest.input(image, destName);
        }
        else {
            System.out.println("input name not found: " + name);
        }
    }

    public void setOutputListener(String name, IOutputListener listener) {
        if (m_outputNames.contains(name)) {
            m_listeners.put(name, listener);
        }
        else {
            System.out.println("output name not found: " + name);
        }
    }

    /**
     * Keeps track of a chained connection.
     */
    private class Chain {
        IWorkFlowComponent m_source;
        String m_sourceName;
        IWorkFlowComponent m_dest;
        String m_destName;

        Chain(IWorkFlowComponent source, String sourceName, IWorkFlowComponent dest, String destName) {
            m_source = source;
            m_sourceName = sourceName;
            m_dest = dest;
            m_destName = destName;
        }

        IWorkFlowComponent getSource() {
            return m_source;
        }

        String getSourceName() {
            return m_sourceName;
        }

        IWorkFlowComponent getDest() {
            return m_dest;
        }

        String getDestName() {
            return m_destName;
        }
    }
}
