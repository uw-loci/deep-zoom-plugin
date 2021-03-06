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

package loci.deepzoom.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import loci.deepzoom.plugin.annotations.Input;
import loci.deepzoom.plugin.annotations.Output;
import loci.deepzoom.util.xmllight.XMLException;
import loci.deepzoom.util.xmllight.XMLParser;
import loci.deepzoom.util.xmllight.XMLTag;
import loci.deepzoom.util.xmllight.XMLWriter;
import loci.deepzoom.workflow.plugin.IPluginLauncher;
import loci.deepzoom.workflow.plugin.ItemWrapper;
import loci.deepzoom.workflow.plugin.PluginScheduler;

/**
 * Builds a workflow consisting of chained components. A component could also be
 * another workflow. Saves setup as XML file and restores from XML file.
 *
 * @author Aivar Grislis
 */
public class WorkFlow implements IWorkFlow {

	public static final String WORKFLOW = "workflow";
	public static final String NAME = "name";
	public static final String MODULES = "modules";
	public static final String MODULE = "module";
	public static final String WIRES = "wires";
	public static final String WIRE = "wire";
	public static final String DST = "dst";
	public static final String SRC = "src";
	public static final String INPUTS = "inputs";
	public static final String INPUT = "input";
	public static final String OUTPUTS = "outputs";
	public static final String OUTPUT = "output";

	IModuleFactory m_moduleFactory = ModuleFactory.getInstance();
	String m_name;
	Map<String, IModule> m_moduleMap = new HashMap<String, IModule>();
	List<String> m_inputNames = new ArrayList<String>();
	List<String> m_outputNames = new ArrayList<String>();
	List<Wire> m_wires = new ArrayList<Wire>();
	Map<String, IModule> m_inputModules = new HashMap<String, IModule>();
	Map<String, String> m_inputModuleNames = new HashMap<String, String>();
	Map<String, IOutputListener> m_listeners =
		new HashMap<String, IOutputListener>();
	Map<String, IModule> m_outputModules = new HashMap<String, IModule>();
	Map<String, String> m_outputModuleNames = new HashMap<String, String>();
	IOutputListener m_listener = new OutputListener();
	Object m_synchObject = new Object();

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public void setName(final String name) {
		m_name = name;
	}

	/**
	 * Gets launcher.
	 */
	// TODO shouldn't a workflow have a launcher? Perhaps we just wire all the
	// PluginModules together.
	@Override
	public IPluginLauncher getLauncher() {
		return null;
	}

	@Override
	public String[] getInputNames() {
		return m_inputNames.toArray(new String[0]);
	}

	@Override
	public String[] getOutputNames() {
		return m_outputNames.toArray(new String[0]);
	}

	@Override
	public boolean fromXML(String xml) {
		boolean success = false;
		final XMLParser xmlHelper = new XMLParser();

		try {
			// handle workflow tag and name
			//
			// <workflow>
			// <name>workFlow1</name>

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

			// handle modules
			//
			// <modules>
			// <module>
			// <name>A</name>
			// <testA>whatever</testA>
			// </module>
			// <module>
			// <name>B</name>
			// <testB>whatever</testB>
			// </module>
			// </modules>

			tag = xmlHelper.getNextTag(xml);
			if (!MODULES.equals(tag.getName())) {
				throw new XMLException("Missing <modules> for <workflow>");
			}
			String modulesXML = tag.getContent();
			xml = tag.getRemainder();
			while (!modulesXML.isEmpty()) {
				tag = xmlHelper.getNextTag(modulesXML);
				modulesXML = tag.getRemainder();

				if (tag.getName().isEmpty()) {
					break;
				}
				if (!MODULE.equals(tag.getName())) {
					throw new XMLException("Missing <module> within <modules>");
				}
				final String moduleXML = tag.getContent();
				tag = xmlHelper.getNextTag(moduleXML);
				if (!NAME.equals(tag.getName())) {
					throw new XMLException("Missing <name> within <module>");
				}
				final IModule module = m_moduleFactory.create(tag.getRemainder());
				add(module);
			}

			// handle wires
			//
			// <wires>
			// <wire>
			// <src>
			// <module>A</module>
			// <name>OUTPUT</name>
			// </src>
			// <dst>
			// <module>B</module>
			// <name>INPUT</name>
			// </dst>
			// </wire>
			// </wires>

			tag = xmlHelper.getNextTag(xml);
			if (!WIRES.equals(tag.getName())) {
				throw new XMLException("Missing <wires> within <workflow>");
			}
			String wiresXML = tag.getContent();
			xml = tag.getRemainder();
			while (!wiresXML.isEmpty()) {
				tag = xmlHelper.getNextTag(wiresXML);
				wiresXML = tag.getRemainder();

				if (tag.getName().isEmpty()) {
					break;
				}
				if (!WIRE.equals(tag.getName())) {
					throw new XMLException("Missing <wire> within <wires>");
				}
				String wireXML = tag.getContent();
				tag = xmlHelper.getNextTag(wireXML);
				wireXML = tag.getRemainder();
				if (!SRC.equals(tag.getName())) {
					throw new XMLException("Missing <src> within <wire>");
				}
				final String srcXML = tag.getContent();
				final ModuleAndName srcCAN = parseModuleAndName(xmlHelper, srcXML);

				tag = xmlHelper.getNextTag(wireXML);
				if (!DST.equals(tag.getName())) {
					throw new XMLException("Missing <dst> within <wire>");
				}
				final String dstXML = tag.getContent();
				final ModuleAndName dstCAN = parseModuleAndName(xmlHelper, dstXML);

				// do the wiring
				wire(srcCAN.getModule(), srcCAN.getName(), dstCAN.getModule(), dstCAN
					.getName());
			}

			// handle inputs
			//
			// <inputs>
			// <input>
			// <name>RED</name>
			// <dst>
			// <module>A</module>
			// <name>ONE</name>
			// </dst>
			// </input>
			// </inputs>

			tag = xmlHelper.getNextTag(xml);
			if (!INPUTS.equals(tag.getName())) {
				throw new XMLException("Missing <inputs> within <workflow>");
			}
			String inputsXML = tag.getContent();
			xml = tag.getRemainder();
			while (!inputsXML.isEmpty()) {
				tag = xmlHelper.getNextTag(inputsXML);
				inputsXML = tag.getRemainder();

				if (tag.getName().isEmpty()) { // TODO don't think these are necessary
					break;
				}

				if (!INPUT.equals(tag.getName())) {
					throw new XMLException("Missing <input> within <inputs>");
				}
				String inputXML = tag.getContent();

				tag = xmlHelper.getNextTag(inputXML);
				inputXML = tag.getRemainder();

				if (!NAME.equals(tag.getName())) {
					throw new XMLException("Missing <name> within <input>");
				}
				final String inName = tag.getContent();

				tag = xmlHelper.getNextTag(inputXML);
				if (!DST.equals(tag.getName())) {
					throw new XMLException("Missing <dest> within <input>");
				}
				final String destXML = tag.getContent();
				final ModuleAndName destMAN = parseModuleAndName(xmlHelper, destXML);

				wireInput(inName, destMAN.getModule(), destMAN.getName());
			}

			// handle outputs
			// <outputs>
			// <output>
			// <name>OUTPUT</name>
			// <src>
			// <module>B</module>
			// <name>OUTPUT</name>
			// </src>
			// </output>
			// </outputs>
			tag = xmlHelper.getNextTag(xml);
			if (!OUTPUTS.equals(tag.getName())) {
				throw new XMLException("Missing <outputs> within <workflow>");
			}
			String outputsXML = tag.getContent();
			xml = tag.getRemainder();
			while (!outputsXML.isEmpty()) {
				tag = xmlHelper.getNextTag(outputsXML);
				outputsXML = tag.getRemainder();

				if (tag.getName().isEmpty()) { // TODO don't think these are necessary
					break;
				}

				if (!OUTPUT.equals(tag.getName())) {
					throw new XMLException("Missing <output> within <outputs>");
				}
				String outputXML = tag.getContent();

				tag = xmlHelper.getNextTag(outputXML);
				outputXML = tag.getRemainder();

				if (!NAME.equals(tag.getName())) {
					throw new XMLException("Missing <name> within <output>");
				}
				final String outName = tag.getContent();

				tag = xmlHelper.getNextTag(outputXML);
				if (!SRC.equals(tag.getName())) {
					throw new XMLException("Missing <src> within <output>");
				}
				final String srcXML = tag.getContent();
				final ModuleAndName srcMAN = parseModuleAndName(xmlHelper, srcXML);

				wireOutput(outName, srcMAN.getModule(), srcMAN.getName());
			}

			// finish the wiring
			finalize();

			success = true;
		}
		catch (final XMLException e) {
			System.out.println("XML Exception " + e.getMessage());
		}
		return success;
	}

	private ModuleAndName
		parseModuleAndName(final XMLParser xmlHelper, String xml)
			throws XMLException
	{
		XMLTag tag = xmlHelper.getNextTag(xml);
		if (!MODULE.equals(tag.getName())) {
			throw new XMLException("Missing <module> tag");
		}
		final String moduleName = tag.getContent();
		xml = tag.getRemainder();
		tag = xmlHelper.getNextTag(xml);
		if (!NAME.equals(tag.getName())) {
			throw new XMLException("Missing <name> tag");
		}
		final String name = tag.getContent();

		return new ModuleAndName(m_moduleMap.get(moduleName), name);
	}

	@Override
	public String toXML() {
		final StringBuilder xmlBuilder = new StringBuilder();
		final XMLWriter xmlHelper = new XMLWriter(xmlBuilder);

		// add workflow tag and name
		xmlHelper.addTag(WORKFLOW);
		xmlHelper.addTagWithContent(NAME, getName());

		// add modules
		xmlHelper.addTag(MODULES);
		for (final String name : m_moduleMap.keySet()) {
			xmlHelper.addTag(MODULE);
			xmlHelper.addTagWithContent(NAME, name);
			final String moduleXML = m_moduleMap.get(name).toXML();
			xmlHelper.add(moduleXML);
			xmlHelper.addEndTag(MODULE);
		}
		xmlHelper.addEndTag(MODULES);

		// add wires
		xmlHelper.addTag(WIRES);
		for (final Wire wire : m_wires) {
			xmlHelper.addTag(WIRE);
			xmlHelper.addTag(SRC);
			xmlHelper.addTagWithContent(MODULE, wire.getSource().getName());
			xmlHelper.addTagWithContent(NAME, wire.getSourceName());
			xmlHelper.addEndTag(SRC);
			xmlHelper.addTag(DST);
			xmlHelper.addTagWithContent(MODULE, wire.getDest().getName());
			xmlHelper.addTagWithContent(NAME, wire.getDestName());
			xmlHelper.addEndTag(DST);
			xmlHelper.addEndTag(WIRE);
		}
		xmlHelper.addEndTag(WIRES);

		// add inputs
		xmlHelper.addTag(INPUTS);
		for (final String name : m_inputNames) {
			xmlHelper.addTag(INPUT);
			xmlHelper.addTagWithContent(NAME, name);
			xmlHelper.addTag(DST);
			xmlHelper.addTagWithContent(MODULE, m_inputModules.get(name).getName());
			xmlHelper.addTagWithContent(NAME, m_inputModuleNames.get(name));
			xmlHelper.addEndTag(DST);
			xmlHelper.addEndTag(INPUT);
		}
		xmlHelper.addEndTag(INPUTS);

		// add outputs
		xmlHelper.addTag(OUTPUTS);
		for (final String name : m_outputNames) {
			xmlHelper.addTag(OUTPUT);
			xmlHelper.addTagWithContent(NAME, name);
			xmlHelper.addTag(SRC);
			xmlHelper.addTagWithContent(MODULE, m_outputModules.get(name).getName());
			xmlHelper.addTagWithContent(NAME, m_outputModuleNames.get(name));
			xmlHelper.addEndTag(SRC);
			xmlHelper.addEndTag(OUTPUT);
		}
		xmlHelper.addEndTag(OUTPUTS);

		// end workflow
		xmlHelper.addEndTag(WORKFLOW);

		return xmlBuilder.toString();
	}

	@Override
	public void add(final IModule component) {
		m_moduleMap.put(component.getName(), component);
	}

	@Override
	public void wire(final IModule source, final IModule dest) {
		wire(source, Output.DEFAULT, dest, Input.DEFAULT);
	}

	@Override
	public void wire(final IModule source, final String sourceName,
		final IModule dest)
	{
		wire(source, sourceName, dest, Input.DEFAULT);
	}

	@Override
	public void wire(final IModule source, final IModule dest,
		final String destName)
	{
		wire(source, Output.DEFAULT, dest, destName);
	}

	@Override
	public void wire(final IModule source, final String sourceName,
		final IModule dest, final String destName)
	{
		final Wire wire = new Wire(source, sourceName, dest, destName);
		m_wires.add(wire);
	}

	@Override
	public Wire[] getWires() {
		return m_wires.toArray(new Wire[0]);
	}

	@Override
	public void finalize() {
		// do the wiring
		for (final Wire wire : m_wires) {
			final IPluginLauncher out = wire.getSource().getLauncher();
			final String outName = wire.getSourceName();
			final IPluginLauncher in = wire.getDest().getLauncher();
			final String inName = wire.getDestName();
			PluginScheduler.getInstance().chain(out, outName, in, inName);
		}

		// promote leftover inputs and outputs to workflow inputs and outputs
		for (final IModule module : m_moduleMap.values()) {
			for (final String name : module.getInputNames()) {
				if (!isWiredAsInput(module, name)) {
					wireInput(name, module, name);
				}
			}
			for (final String name : module.getOutputNames()) {
				if (!isWiredAsOutput(module, name)) {
					wireOutput(name, module, name);
				}
			}
		}
	}

	private boolean isWiredAsInput(final IModule module, final String name) {
		boolean found = false;

		// is this already an input?
		for (final String inName : m_inputNames) {
			if (m_inputModules.get(inName).equals(module) &&
				m_inputModuleNames.get(inName).equals(name))
			{
				found = true;
			}
		}

		if (!found) {
			// is this the destination of some internal wire?
			for (final Wire wire : m_wires) {
				if (wire.getDest().equals(module) && wire.getDestName().equals(name)) {
					found = true;
				}
			}
		}
		return found;
	}

	private boolean isWiredAsOutput(final IModule module, final String name) {
		boolean found = false;

		// is this already an output?
		if (null != m_outputModuleNames.get(name)) { // TODO see wireOutput; this is
																									// inadequate
			found = true;
		}

		if (!found) {
			// is this the source of some internal wire?
			for (final Wire wire : m_wires) {
				if (wire.getSource().equals(module) &&
					wire.getSourceName().equals(name))
				{
					found = true;
				}
			}
		}
		return found;
	}

	@Override
	public void wireInput(final IModule dest) {
		wireInput(Input.DEFAULT, dest, Input.DEFAULT);
	}

	@Override
	public void wireInput(final IModule dest, final String destName) {
		wireInput(Input.DEFAULT, dest, destName);
	}

	@Override
	public void wireInput(final String inName, final IModule dest) {
		wireInput(inName, dest, Input.DEFAULT);
	}

	@Override
	public void wireInput(final String inName, final IModule dest,
		final String destName)
	{
		// note new input name
		m_inputNames.add(inName);

		// save associated module
		m_inputModules.put(inName, dest);

		// associate dest name with input name
		m_inputModuleNames.put(inName, destName);
	}

	@Override
	public void wireOutput(final IModule source) {
		wireOutput(Output.DEFAULT, source, Output.DEFAULT);
	}

	@Override
	public void wireOutput(final IModule source, final String sourceName) {
		wireOutput(Output.DEFAULT, source, sourceName);
	}

	@Override
	public void wireOutput(final String outName, final IModule source) {
		wireOutput(Output.DEFAULT, source, outName);
	}

	@Override
	public void wireOutput(final String outName, final IModule source,
		final String sourceName)
	{
		// note new output name
		m_outputNames.add(outName);

		// save associated module
		m_outputModules.put(outName, source);

		// associate source name with output name
		m_outputModuleNames.put(sourceName, outName); // TODO WRONG!!! sourceName is
																									// not unique for all modules

		// listen for source name from source module
		source.setOutputListener(sourceName, m_listener);
	}

	@Override
	public void input(final ItemWrapper image) {
		input(image, Input.DEFAULT);
	}

	@Override
	public void input(final ItemWrapper image, final String name) {
		if (m_inputNames.contains(name)) {
			final IModule dest = m_inputModules.get(name);
			final String destName = m_inputModuleNames.get(name);
			dest.input(image, destName);
		}
		else {
			System.out.println("input name not found: " + name);
		}
	}

	@Override
	public void setOutputListener(final IOutputListener listener) {
		synchronized (m_synchObject) {
			setOutputListener(Output.DEFAULT, listener);
		}
	}

	@Override
	public void setOutputListener(final String name,
		final IOutputListener listener)
	{
		synchronized (m_synchObject) {
			m_listeners.put(name, listener);
		}
	}

	@Override
	public void quit() {
		PluginScheduler.getInstance().quit();
	}

	@Override
	public void clear() {
		// TODO more
		m_wires.clear();
		m_inputNames.clear();
		m_outputNames.clear();
		synchronized (m_synchObject) {
			m_listeners.clear();
		}
	}

	/**
	 * Listens for output images, passes them on to external listeners.
	 */
	private class OutputListener implements IOutputListener {

		@Override
		public void outputImage(final String name, final ItemWrapper image) {
			// get output name associated with this source name
			final String outName = m_outputModuleNames.get(name);
			final IOutputListener listener = m_listeners.get(outName);
			if (null != listener) {
				listener.outputImage(outName, image);
			}
		}
	}

	/**
	 * Data structure that keeps track of IModule and name.
	 */
	private class ModuleAndName {

		final IModule m_module;
		final String m_name;

		ModuleAndName(final IModule module, final String name) {
			m_module = module;
			m_name = name;
		}

		public IModule getModule() {
			return m_module;
		}

		public String getName() {
			return m_name;
		}
	}
}
