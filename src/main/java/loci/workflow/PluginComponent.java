/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//TODO
/*
 * Is the name a short name or the full path, i.e. loci.plugin.Whatever?  Why have a set name?
 * Need to instantiate the plugin.
 * Feeding in the image should work; how to set up listener?
 * How to do the linking?  IComponent could have chain() method, delegates to m_linkedPlugin.  How do WorkFlows get
 * chained?
 */

package loci.workflow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import loci.multiinstanceplugin.AbstractPlugin;
import loci.multiinstanceplugin.ILinkedPlugin;
import loci.multiinstanceplugin.IPlugin;
import loci.multiinstanceplugin.LinkedPlugin;
import loci.multiinstanceplugin.PluginClassException;
import loci.plugin.ImageWrapper;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;
import loci.util.xmllight.XMLException;
import loci.util.xmllight.XMLParser;
import loci.util.xmllight.XMLTag;
import loci.util.xmllight.XMLWriter;

/**
 *
 * @author Aivar Grislis
 */
public class PluginComponent implements IComponent {
    public static final String PLUGIN = "plugin";
    ILinkedPlugin m_linkedPlugin;
    Set<String> m_inputNames;
    Set<String> m_outputNames;


    String m_name;
    Map<String, IOutputListener> m_listenerMap = new HashMap<String, IOutputListener>();

    public PluginComponent() {
    }

    /**
     * Create an instance for a given plugin class name.
     *
     * @param pluginClassName
     */
    public PluginComponent(String pluginClassName) throws PluginClassException {
        init(pluginClassName);
    }

    /**
     * Create an instance for a given plugin class.
     *
     * @param className
     */
    public PluginComponent(Class pluginClass) {
        init(pluginClass);
    }

    /**
     * Initializes given a plugin class name.
     *
     * @param className
     */
    private void init(String pluginClassName) {

        // get associated class
        Class pluginClass = null;
        try {
            pluginClass = Class.forName(pluginClassName);
        }
        catch (ClassNotFoundException e) {
            // class cannot be located
            System.out.println("Can't find " + pluginClassName);
        }
        catch (ExceptionInInitializerError e) {
            // initialization provoked by this method fails
            System.out.println("Error initializing " + pluginClassName + " " + e.getStackTrace());
        }
        catch (LinkageError e) {
            // linkage fails
            System.out.println("Linkage error " + pluginClassName + " " + e.getStackTrace());
        }

        // validate class
        boolean success = false;
        if (null != pluginClass) {
            success = true;

            if (!pluginClass.isAssignableFrom(AbstractPlugin.class)) {
                success = false;
                System.out.println("Plugin should extend AbstractPlugin");
            }

            if (!pluginClass.isAssignableFrom(IPlugin.class)) {
                success = false;
                System.out.println("Plugin should implement IPlugin");
            }
        }

        if (success) {
            init(pluginClass);
        }
        else {
            throw new PluginClassException("Plugin class is invalid " + pluginClassName);
        }
    }

    /**
     * Initializes given a plugin class.
     *
     * @param pluginClass
     */
    private void init(Class pluginClass) {
        m_linkedPlugin = new LinkedPlugin(pluginClass);
        m_inputNames = m_linkedPlugin.getInputNames();
        m_outputNames = m_linkedPlugin.getOutputNames();
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
        StringBuilder xmlBuilder = new StringBuilder();
        XMLWriter xmlHelper = new XMLWriter(xmlBuilder);

        // add workflow tag and name
        xmlHelper.addTag(PLUGIN);
        xmlHelper.addTagWithContent(WorkFlow.NAME, getName());

        // add inputs
        xmlHelper.addTag(WorkFlow.INPUTS);
        for (String name : m_inputNames) {
            xmlHelper.addTag(WorkFlow.INPUT);
            xmlHelper.addTagWithContent(WorkFlow.NAME, name);
            xmlHelper.addEndTag(WorkFlow.INPUT);
        }
        xmlHelper.addEndTag(WorkFlow.INPUTS);

        // add outputs
        xmlHelper.addTag(WorkFlow.OUTPUTS);
        for (String name : m_outputNames) {
            xmlHelper.addTag(WorkFlow.OUTPUT);
            xmlHelper.addTagWithContent(WorkFlow.NAME, name);
            xmlHelper.addEndTag(WorkFlow.OUTPUT);
        }
        xmlHelper.addEndTag(WorkFlow.OUTPUTS);

        // end workflow
        xmlHelper.addEndTag(PLUGIN);

        return xmlBuilder.toString();

    }

    /**
     * Restores component from XML string representation.
     *
     * @param xml
     * @return whether successfully parsed
     */
    public boolean fromXML(String xml) {
        boolean success = false;
        XMLParser xmlHelper = new XMLParser();

        try {
            // handle test tag and name
            //
            // <testcomponent>
            //   <name>A</name>

            XMLTag tag = xmlHelper.getNextTag(xml);
            if (!PLUGIN.equals(tag.getName())) {
                throw new XMLException("Missing <plugin> tag");
            }
            xml = tag.getContent();
            tag = xmlHelper.getNextTag(xml);
            if (!WorkFlow.NAME.equals(tag.getName())) {
                throw new XMLException("Missing <name> for <plugin>");
            }
            setName(tag.getContent());
            xml = tag.getRemainder();

            // handle inputs
            //
            //  <inputs>
            //    <input>
            //      <name>RED</name>
            //   </input>
            // </inputs>

            tag = xmlHelper.getNextTag(xml);
            if (!WorkFlow.INPUTS.equals(tag.getName())) {
                throw new XMLException("Missing <inputs> within <plugin>");
            }
            String inputsXML = tag.getContent();
            xml = tag.getRemainder();
            while (!inputsXML.isEmpty()) {
                tag = xmlHelper.getNextTag(inputsXML);
                inputsXML = tag.getRemainder();

                if (tag.getName().isEmpty()) { //TODO don't think these are necessary
                    break;
                }

                if (!WorkFlow.INPUT.equals(tag.getName())) {
                    throw new XMLException("Missing <input> within <inputs>");
                }
                String inputXML = tag.getContent();

                tag = xmlHelper.getNextTag(inputXML);
                inputXML = tag.getRemainder();

                if (!WorkFlow.NAME.equals(tag.getName())) {
                    throw new XMLException("Missing <name> within <input>");
                }
                String inName = tag.getContent();

                m_inputNames.add(inName);
            }

            // handle outputs
            //  <outputs>
            //    <output>
            //      <name>OUTPUT</name>
            //    </output>
            //  </outputs>
            tag = xmlHelper.getNextTag(xml);
            if (!WorkFlow.OUTPUTS.equals(tag.getName())) {
                throw new XMLException("Missing <outputs> within <plugin>");
            }
            String outputsXML = tag.getContent();
            xml = tag.getRemainder();
            while (!outputsXML.isEmpty()) {
                tag = xmlHelper.getNextTag(outputsXML);
                outputsXML = tag.getRemainder();

                if (tag.getName().isEmpty()) { //TODO don't think these are necessary
                    break;
                }

                if (!WorkFlow.OUTPUT.equals(tag.getName())) {
                    throw new XMLException("Missing <output> within <outputs>");
                }
                String outputXML = tag.getContent();

                tag = xmlHelper.getNextTag(outputXML);
                outputXML = tag.getRemainder();

                if (!WorkFlow.NAME.equals(tag.getName())) {
                    throw new XMLException("Missing <name> within <output>");
                }
                String outName = tag.getContent();
                m_outputNames.add(outName);
            }
            success = true;
        }
        catch (XMLException e) {
            System.out.println("XML Exception");
        }
        return success;
    }

    /**
     * Gets input image names.
     *
     * @return
     */
    public String[] getInputNames() {
        return m_inputNames.toArray(new String[0]);
    }

    /**
     * Gets output names.
     *
     * @return
     */
    public String[] getOutputNames() {
        return m_outputNames.toArray(new String[0]);
    }

    /**
     * Furnish input image.
     *
     * @param image
     */
    public void input(ImageWrapper image) {
        input(image, Input.DEFAULT);
    }

    /**
     * Furnish input image
     *
     * @param image
     * @param name
     */
    public void input(ImageWrapper image, String name) {
        m_linkedPlugin.externalPut(name, image); //TODO inconsistency!
    }

    /**
     * Listen for output image.
     *
     * @param listener
     */
    public void setOutputListener(IOutputListener listener) {
        setOutputListener(Output.DEFAULT, listener);
    }

    /**
     * Listen for output image.
     *
     * @param name
     * @param listener
     */
    public void setOutputListener(String name, IOutputListener listener) {
        m_listenerMap.put(name, listener);
        //TODO hook up the listener
    }

}
