/*
 * #%L
 * Deep Zoom plugin for ImageJ.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

//TODO
/*
 * Is the name a short name or the full path, i.e. loci.plugin.Whatever?  Why have a set name?
 * Need to instantiate the plugin.
 * Feeding in the image should work; how to set up listener?
 * How to do the linking?  IComponent could have chain() method, delegates to m_linkedPlugin.  How do WorkFlows get
 * chained?
 */

package loci.deepzoom.workflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import loci.deepzoom.plugin.annotations.Input;
import loci.deepzoom.plugin.annotations.Output;
import loci.deepzoom.util.xmllight.XMLException;
import loci.deepzoom.util.xmllight.XMLParser;
import loci.deepzoom.util.xmllight.XMLTag;
import loci.deepzoom.util.xmllight.XMLWriter;
import loci.deepzoom.workflow.plugin.AbstractPlugin;
import loci.deepzoom.workflow.plugin.IPlugin;
import loci.deepzoom.workflow.plugin.IPluginLauncher;
import loci.deepzoom.workflow.plugin.ItemWrapper;
import loci.deepzoom.workflow.plugin.PluginAnnotations;
import loci.deepzoom.workflow.plugin.PluginClassException;
import loci.deepzoom.workflow.plugin.PluginLauncher;

/**
 * TODO
 *
 * @author Aivar Grislis
 */
public class PluginModule implements IModule {

	public static final String PLUGIN = "plugin";
	public static final String CLASSNAME = "classname";
	String m_pluginClassName;
	String m_name;
	PluginAnnotations m_annotations;
	IPluginLauncher m_launcher;
	Set<String> m_inputNames = Collections.EMPTY_SET;
	Set<String> m_outputNames = Collections.EMPTY_SET;
	Map<String, IOutputListener> m_listenerMap =
		new HashMap<String, IOutputListener>();

	public PluginModule() {}

	/**
	 * Create an instance for a given plugin class name.
	 *
	 * @param pluginClassName
	 */
	public PluginModule(final String pluginClassName) throws PluginClassException
	{
		init(pluginClassName);
	}

	/**
	 * Create an instance for a given plugin class.
	 *
	 * @param pluginClass
	 */
	public PluginModule(final Class<?> pluginClass) {
		init(pluginClass);
	}

	/**
	 * Initializes given a plugin class name.
	 *
	 * @param pluginClassName
	 */
	private void init(final String pluginClassName) {

		// get associated class
		Class<?> pluginClass = null;
		try {
			pluginClass = Class.forName(pluginClassName);
		}
		catch (final ClassNotFoundException e) {
			// class cannot be located
			System.out.println("Can't find " + pluginClassName);
		}
		catch (final ExceptionInInitializerError e) {
			// initialization provoked by this method fails
			System.out.println("Error initializing " + pluginClassName + " " +
				e.getStackTrace());
		}
		catch (final LinkageError e) {
			// linkage fails
			System.out.println("Linkage error " + pluginClassName + " " +
				e.getStackTrace());
		}

		// validate class
		boolean success = false;
		if (null != pluginClass) {
			success = true;

			System.out.println(pluginClass.toString());

			if (!pluginClass.isAssignableFrom(AbstractPlugin.class)) {
				// success = false; //TODO fails this!!
				System.out.println("Plugin " + pluginClassName +
					" should extend AbstractPlugin");
			}

			if (!pluginClass.isAssignableFrom(IPlugin.class)) {
				// success = false; //TODO fails this!!
				System.out.println("Plugin " + pluginClassName +
					" should implement IPlugin");
			}
		}

		if (success) {
			init(pluginClass);
		}
		else {
			throw new PluginClassException("Plugin class is invalid " +
				pluginClassName);
		}
	}

	/**
	 * Initializes given a plugin class.
	 *
	 * @param pluginClass
	 */
	private void init(final Class<?> pluginClass) {
		m_pluginClassName = pluginClass.getName();
		final int lastDotIndex = m_pluginClassName.lastIndexOf('.');
		m_name =
			m_pluginClassName.substring(lastDotIndex + 1, m_pluginClassName.length());

		// examine annotations
		m_annotations = new PluginAnnotations(pluginClass);
		m_inputNames = m_annotations.getInputNames();
		m_outputNames = m_annotations.getOutputNames();

		// create launcher
		m_launcher = new PluginLauncher(pluginClass, m_annotations);
	}

	/**
	 * Gets name of component.
	 *
	 * @return
	 */
	@Override
	public String getName() {
		return m_name;
	}

	/**
	 * Sets name of component.
	 *
	 * @param name
	 */
	@Override
	public void setName(final String name) {
		m_name = name;
	}

	@Override
	public IPluginLauncher getLauncher() {
		return m_launcher;
	}

	/**
	 * Saves component as XML string representation.
	 *
	 * @return
	 */
	@Override
	public String toXML() {
		final StringBuilder xmlBuilder = new StringBuilder();
		final XMLWriter xmlHelper = new XMLWriter(xmlBuilder);

		// add workflow tag, name, and class name
		xmlHelper.addTag(PLUGIN);
		xmlHelper.addTagWithContent(WorkFlow.NAME, getName());
		xmlHelper.addTagWithContent(CLASSNAME, m_pluginClassName);

		// add inputs
		xmlHelper.addTag(WorkFlow.INPUTS);
		for (final String name : m_inputNames) {
			xmlHelper.addTag(WorkFlow.INPUT);
			xmlHelper.addTagWithContent(WorkFlow.NAME, name);
			xmlHelper.addEndTag(WorkFlow.INPUT);
		}
		xmlHelper.addEndTag(WorkFlow.INPUTS);

		// add outputs
		xmlHelper.addTag(WorkFlow.OUTPUTS);
		for (final String name : m_outputNames) {
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
	@Override
	public boolean fromXML(String xml) {
		boolean success = false;
		final XMLParser xmlHelper = new XMLParser();

		try {
			// handle test tag and name
			//
			// <plugin>
			// <name>A</name>

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

			// handle class name
			tag = xmlHelper.getNextTag(xml);
			if (!CLASSNAME.equals(tag.getName())) {
				throw new XMLException("Missing <classname> for <plugin>");
			}
			init(tag.getContent());
			if (true) return true; // TODO the follow code analyzes given input/output
															// names, which are merely a descriptive nicety;
															// could compare with annotated input/output
															// names.

			// handle inputs
			//
			// <inputs>
			// <input>
			// <name>RED</name>
			// </input>
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

				if (tag.getName().isEmpty()) { // TODO don't think these are necessary
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
				final String inName = tag.getContent();

				m_inputNames.add(inName);
			}

			// handle outputs
			// <outputs>
			// <output>
			// <name>OUTPUT</name>
			// </output>
			// </outputs>
			tag = xmlHelper.getNextTag(xml);
			if (!WorkFlow.OUTPUTS.equals(tag.getName())) {
				throw new XMLException("Missing <outputs> within <plugin>");
			}
			String outputsXML = tag.getContent();
			xml = tag.getRemainder();
			while (!outputsXML.isEmpty()) {
				tag = xmlHelper.getNextTag(outputsXML);
				outputsXML = tag.getRemainder();

				if (tag.getName().isEmpty()) { // TODO don't think these are necessary
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
				final String outName = tag.getContent();
				m_outputNames.add(outName);
			}
			success = true;
		}
		catch (final XMLException e) {
			System.out.println("XML Exception");
		}
		return success;
	}

	/**
	 * Gets input image names.
	 *
	 * @return
	 */
	@Override
	public String[] getInputNames() {
		return m_inputNames.toArray(new String[0]);
	}

	/**
	 * Gets output names.
	 *
	 * @return
	 */
	@Override
	public String[] getOutputNames() {
		return m_outputNames.toArray(new String[0]);
	}

	/**
	 * Furnish input image.
	 *
	 * @param image
	 */
	@Override
	public void input(final ItemWrapper image) {
		input(image, Input.DEFAULT);
	}

	/**
	 * Furnish input image
	 *
	 * @param image
	 * @param name
	 */
	@Override
	public void input(final ItemWrapper image, final String name) {
		m_launcher.externalPut(name, image); // TODO order inconsistency!
	}

	/**
	 * Listen for output image.
	 *
	 * @param listener
	 */
	@Override
	public void setOutputListener(final IOutputListener listener) {
		setOutputListener(Output.DEFAULT, listener);
	}

	/**
	 * Listen for output image.
	 *
	 * @param name
	 * @param listener
	 */
	@Override
	public void setOutputListener(final String name,
		final IOutputListener listener)
	{
		m_listenerMap.put(name, listener);
		// TODO hook up the listener
	}

}
