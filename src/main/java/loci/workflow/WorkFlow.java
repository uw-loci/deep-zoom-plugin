/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import java.lang.StringBuilder;
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
public class WorkFlow implements IComponent, IWorkFlow {
    static final String WORKFLOW = "workflow";
    static final String NAME = "name";
    static final String COMPONENTS = "components";
    static final String COMPONENT = "component";
    static final String CHAINS = "chains";
    static final String CHAIN = "chain";
    static final String DEST = "dest";
    static final String SRC = "src";
    static final String INPUTS = "inputs";
    static final String INPUT = "input";
    static final String OUTPUTS = "outputs";
    static final String OUTPUT = "output";

    static IComponentFactory s_componentFactory;
    String m_name;
    Map<String, IComponent> m_componentMap = new HashMap<String, IComponent>();
    List<String> m_inputNames = new ArrayList<String>();
    List<String> m_outputNames = new ArrayList<String>();
    List<Chain> m_chains = new ArrayList<Chain>();
    Map<String, IComponent> m_inputComponents = new HashMap<String, IComponent>();
    Map<String, String> m_inputComponentNames = new HashMap<String, String>();
    Map<String, IOutputListener> m_listeners = new HashMap<String, IOutputListener>();
    Map<String, IComponent> m_outputComponents = new HashMap<String, IComponent>();
    Map<String, String> m_outputComponentNames = new HashMap<String, String>();
    IOutputListener m_listener = new OutputListener();
    Object m_synchObject = new Object();

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

    public void setComponentFactory(IComponentFactory componentFactory) {
        System.out.println("in set component factory");
        s_componentFactory = componentFactory;
        if (s_componentFactory == null) {
            System.out.println("its null");
        }
    }

    public boolean fromXML(String xml) {
        boolean success = false;
        XMLHelper xmlHelper = new XMLHelper();

        try {
            XMLTag tag = xmlHelper.getNextTag(xml);
            if (!WORKFLOW.equals(tag.getName())) {
                throw new XMLException("Missing <workflow> tag");
            }
            xml = tag.getContent();
            tag = xmlHelper.getNextTag(xml);
            if (!NAME.equals(tag.getName())) {
                throw new XMLException("Missing <name> for <workflow>");
            }
            setName(tag.getContent());
            xml = tag.getRemainder();
            tag = xmlHelper.getNextTag(xml);
            if (!COMPONENTS.equals(tag.getName())) {
                throw new XMLException("Missing <components> for <workflow>");
            }
            String componentsXML = tag.getContent();
            xml = tag.getRemainder();
            while (!componentsXML.isEmpty()) {
                tag = xmlHelper.getNextTag(componentsXML);
                componentsXML = tag.getRemainder();

                if (tag.getName().isEmpty()) {
                    break;
                }
                if (!COMPONENT.equals(tag.getName())) {
                    throw new XMLException("Missing <component> within <components>");
                }
                String componentXML = tag.getContent();
                tag = xmlHelper.getNextTag(componentXML);
                if (!NAME.equals(tag.getName())) {
                    throw new XMLException("Missing <name> within <component>");
                }
                System.out.println("component XML is [" + tag.getRemainder() + "]");
                if (null == s_componentFactory) {
                    System.out.println("Component Factory is null");
                }
                IComponent component = s_componentFactory.create(tag.getRemainder());
                add(component);
            }

            tag = xmlHelper.getNextTag(xml);
            if (!CHAINS.equals(tag.getName())) {
                throw new XMLException("Missing <chains> within <workflow>");
            }
            String chainsXML = tag.getContent();
            xml = tag.getRemainder();
            while (!chainsXML.isEmpty()) {
                tag = xmlHelper.getNextTag(chainsXML);
                chainsXML = tag.getRemainder();

                if (tag.getName().isEmpty()) {
                    break;
                }
                if (!CHAIN.equals(tag.getName())) {
                    throw new XMLException("Missing <chain> within <chains>");
                }
                String chainXML = tag.getContent();
                tag = xmlHelper.getNextTag(chainXML);
                if (!SRC.equals(tag.getName())) {
                    throw new XMLException("Missing <src> within <chain>");
                }
                

            }
            success = true;
        }
        catch (XMLException e) {
            System.out.println("XML Exception");
        }
        return success;
    }

    public String toXML() {
        StringBuilder xmlBuilder = new StringBuilder();
        XMLHelper xmlHelper = new XMLHelper(xmlBuilder);

        // add workflow tag and name
        xmlHelper.addTag(WORKFLOW);
        xmlHelper.addTagWithContent(NAME, getName());

        // add components
        xmlHelper.addTag(COMPONENTS);
        for (String name: m_componentMap.keySet()) {
            xmlHelper.addTag(COMPONENT);
            xmlHelper.addTagWithContent(NAME, name);
            xmlHelper.add(m_componentMap.get(name).toXML());
            xmlHelper.addEndTag(COMPONENT);
        }
        for (IComponent component: m_componentMap.values()) {
            xmlHelper.addTag(COMPONENT);
            xmlHelper.addTagWithContent(NAME, component.getName());
            xmlHelper.addEndTag(COMPONENT);
        }
        xmlHelper.addTag(COMPONENT);
        xmlHelper.addEndTag(COMPONENT);
        xmlHelper.addEndTag(COMPONENTS);

        // add chains
        xmlHelper.addTag(CHAINS);
        for (Chain chain: m_chains) {
            xmlHelper.addTag(CHAIN);
            xmlHelper.addTag(SRC);
            xmlHelper.addTagWithContent(COMPONENT, chain.getSource().getName());
            xmlHelper.addTagWithContent(NAME, chain.getSourceName());
            xmlHelper.addEndTag(SRC);
            xmlHelper.addTag(DEST);
            xmlHelper.addTagWithContent(COMPONENT, chain.getDest().getName());
            xmlHelper.addTagWithContent(NAME, chain.getDestName());
            xmlHelper.addEndTag(DEST);
            xmlHelper.addEndTag(CHAIN);
        }
        xmlHelper.addEndTag(CHAINS);

        // add inputs
        xmlHelper.addTag(INPUTS);
        for (String name : m_inputNames) {
            xmlHelper.addTag(INPUT);
            xmlHelper.addTagWithContent(NAME, name);
            xmlHelper.addTag(DEST);
            xmlHelper.addTagWithContent(COMPONENT, m_inputComponents.get(name).getName());
            xmlHelper.addTagWithContent(NAME, m_inputComponentNames.get(name));
            xmlHelper.addEndTag(SRC);
            xmlHelper.addEndTag(INPUT);
        }
        xmlHelper.addEndTag(INPUTS);

        // add outputs
        xmlHelper.addTag(OUTPUTS);
        for (String name : m_outputNames) {
            xmlHelper.addTag(OUTPUT);
            xmlHelper.addTagWithContent(NAME, name);
            xmlHelper.addTag(SRC);
            xmlHelper.addTagWithContent(COMPONENT, m_outputComponents.get(name).getName());
            xmlHelper.addTagWithContent(NAME, m_outputComponentNames.get(name));
            xmlHelper.addEndTag(SRC);
            xmlHelper.addEndTag(OUTPUT);
        }
        xmlHelper.addEndTag(OUTPUTS);

        // end workflow
        xmlHelper.addEndTag(WORKFLOW);

        return xmlBuilder.toString();
    }

    public void add(IComponent component) {
        m_componentMap.put(component.getName(), component);
    }

    public void chain(IComponent source, IComponent dest) {
        chain(source, Output.DEFAULT, dest, Input.DEFAULT);
    }

    public void chain(IComponent source, String sourceName, IComponent dest) {
        chain(source, sourceName, dest, Input.DEFAULT);
    }

    public void chain(IComponent source, IComponent dest, String destName) {
        chain(source, Output.DEFAULT, dest, destName);
    }

    public void chain(IComponent source, String sourceName, IComponent dest, String destName) {
        Chain chain = new Chain(source, sourceName, dest, destName);
        m_chains.add(chain);
    }

    public void chainInput(IComponent dest) {
        chainInput(Input.DEFAULT, dest, Input.DEFAULT);
    }

    public void chainInput(IComponent dest, String destName) {
        chainInput(Input.DEFAULT, dest, destName);
    }

    public void chainInput(String inName, IComponent dest) {
        chainInput(inName, dest, Input.DEFAULT);
    }

    public void chainInput(String inName, IComponent dest, String destName) {
        // note new input name
        m_inputNames.add(inName);

        // save associated component
        m_inputComponents.put(inName, dest);

        // associate dest name with input name
        m_inputComponentNames.put(inName, destName);
    }

    public void chainOutput(IComponent source) {
        chainOutput(Output.DEFAULT, source, Output.DEFAULT);
    }

    public void chainOutput(IComponent source, String sourceName) {
        chainOutput(Output.DEFAULT, source, sourceName);
    }

    public void chainOutput(String outName, IComponent source) {
        chainOutput(Output.DEFAULT, source, outName);
    }

    public void chainOutput(String outName, IComponent source, String sourceName) {
        // note new output name
        m_outputNames.add(outName);

        // save associated component
        m_outputComponents.put(outName, source);

        // associate source name with output name
        m_outputComponentNames.put(sourceName, outName);

        // listen for source name from source component
        source.setOutputListener(sourceName, m_listener);
    }
    
    public void input(ImageWrapper image, String name) {
        if (m_inputNames.contains(name)) {
            IComponent dest = m_inputComponents.get(name);
            String destName = m_inputComponentNames.get(name);
            dest.input(image, destName);
        }
        else {
            System.out.println("input name not found: " + name);
        }
    }

    public void setOutputListener(IOutputListener listener) {
        synchronized (m_synchObject) {
            setOutputListener(Output.DEFAULT, listener);
        }
    }

    public void setOutputListener(String name, IOutputListener listener) {
        synchronized (m_synchObject) {
            m_listeners.put(name, listener);
        }
    }
    
    /**
     * Listens for output images, passes them on to external listeners.
     */
    private class OutputListener implements IOutputListener {

        public void outputImage(String name, ImageWrapper image) {
            // get output name associated with this source name
            String outName = m_outputComponentNames.get(name);
            IOutputListener listener = m_listeners.get(outName);
            if (null != listener) {
                listener.outputImage(outName, image);
            }
        }
    }

    /**
     * Keeps track of a chained connection.
     */
    private class Chain {
        IComponent m_source;
        String m_sourceName;
        IComponent m_dest;
        String m_destName;

        Chain(IComponent source, String sourceName, IComponent dest, String destName) {
            m_source = source;
            m_sourceName = sourceName;
            m_dest = dest;
            m_destName = destName;
        }

        IComponent getSource() {
            return m_source;
        }

        String getSourceName() {
            return m_sourceName;
        }

        IComponent getDest() {
            return m_dest;
        }

        String getDestName() {
            return m_destName;
        }
    }
}
