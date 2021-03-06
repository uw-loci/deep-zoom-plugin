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

package loci.workflow;

import java.util.ArrayList;
import java.util.List;

import loci.deepzoom.plugin.annotations.Input;
import loci.deepzoom.plugin.annotations.Output;
import loci.deepzoom.util.xmllight.XMLException;
import loci.deepzoom.util.xmllight.XMLParser;
import loci.deepzoom.util.xmllight.XMLTag;
import loci.deepzoom.util.xmllight.XMLWriter;
import loci.deepzoom.workflow.IModule;
import loci.deepzoom.workflow.IOutputListener;
import loci.deepzoom.workflow.WorkFlow;
import loci.deepzoom.workflow.plugin.IPluginLauncher;
import loci.deepzoom.workflow.plugin.ItemWrapper;

/**
 * TODO
 *
 * @author Aivar Grislis
 */
public class TestComponent implements IModule {

	public static final String TESTCOMPONENT = "testcomponent";
	String m_name;
	List<String> m_inputNames = new ArrayList<String>();
	List<String> m_outputNames = new ArrayList<String>();

	public void setInputNames(final String[] inputNames) {
		for (final String name : inputNames) {
			m_inputNames.add(name);
		}
	}

	public void setOutputNames(final String[] outputNames) {
		for (final String name : outputNames) {
			m_outputNames.add(name);
		}
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
		return null;
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

		// add workflow tag and name
		xmlHelper.addTag(TESTCOMPONENT);
		xmlHelper.addTagWithContent(WorkFlow.NAME, getName());

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
		xmlHelper.addEndTag(TESTCOMPONENT);

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
			// <testcomponent>
			// <name>A</name>

			XMLTag tag = xmlHelper.getNextTag(xml);
			if (!TESTCOMPONENT.equals(tag.getName())) {
				throw new XMLException("Missing <testcomponent> tag");
			}
			xml = tag.getContent();
			tag = xmlHelper.getNextTag(xml);
			if (!WorkFlow.NAME.equals(tag.getName())) {
				throw new XMLException("Missing <name> for <workflow>");
			}
			setName(tag.getContent());
			xml = tag.getRemainder();

			// handle inputs
			//
			// <inputs>
			// <input>
			// <name>RED</name>
			// </input>
			// </inputs>

			tag = xmlHelper.getNextTag(xml);
			if (!WorkFlow.INPUTS.equals(tag.getName())) {
				throw new XMLException("Missing <inputs> within <testcomponent>");
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
					throw new XMLException("Missing <input> within <inputs");
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
				throw new XMLException("Missing <outputs> within <testcomponent>");
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
	 * Furnish input image
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
		// TODO
	}

	/**
	 * Listen for output image.
	 *
	 * @param name
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
		// TODO
	}
}
