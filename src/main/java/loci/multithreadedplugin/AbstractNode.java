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

package loci.multithreadedplugin;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import loci.deepzoom.plugin.annotations.Img;
import loci.deepzoom.plugin.annotations.Input;
import loci.deepzoom.plugin.annotations.Output;

/**
 * This abstract base class handles interaction with the NodeScheduler.
 *
 * @author Aivar Grislis
 */
public abstract class AbstractNode extends Thread implements INode {

	private enum InputOutput {
		INPUT, OUTPUT
	}

	UUID m_id = UUID.randomUUID();
	volatile boolean m_idle = true;
	Map<String, String> m_map = new HashMap();
	Set<String> m_inputNames = getInputNamesFromAnnotations();
	Set<String> m_outputNames = getOutputNamesFromAnnotations();
	Map<String, Object> m_inputs = new HashMap();

	/**
	 * Builds a name that is tied to this instance of the subclass. Used to create
	 * a unique input name.
	 *
	 * @param name unqualified name
	 * @return qualified name
	 */
	@Override
	public String uniqueInstance(final String name) {
		return (m_id.toString() + '-' + name);
	}

	/**
	 * When chaining associates a named output with a named input tied to a
	 * particular instance of chained subclass.
	 *
	 * @param outName
	 * @param fullName
	 */
	@Override
	public void associate(final String outName, final String fullName) {
		m_map.put(outName, fullName);
	}

	/**
	 * This is the body of the plugin, defined in subclass.
	 */
	abstract public void process();

	/**
	 * Gets the default input data from previous in chain. Called from subclass.
	 *
	 * @return data
	 */
	@Override
	public Object get() {
		return get(INode.DEFAULT);
	}

	/**
	 * Gets a named input image from previous in chain. Called from subclass.
	 *
	 * @param inName
	 * @return image
	 */
	@Override
	public Object get(final String inName) {
		System.out.println("get " + inName);
		final Object input = m_inputs.get(inName);
		if (null == input) {
			// run-time request disagrees with annotation
			nameNotAnnotated(InputOutput.INPUT, inName);
		}
		return input;
	}

	/**
	 * Puts the default output data to next in chain (if any). Called from
	 * subclass.
	 *
	 * @param data
	 */
	@Override
	public void put(final Object data) {
		put(INode.DEFAULT, data);
	}

	/**
	 * Puts named output data to next in chain (if any). Called from subclass.
	 *
	 * @param outName
	 * @param data
	 */
	@Override
	public void put(final String outName, final Object data) {
		System.out.println("put " + outName);
		if (isAnnotatedName(InputOutput.OUTPUT, outName)) {
			System.out.println("was annotated");
			// anyone interested in this output data?
			final String fullName = m_map.get(outName);
			System.out.println("full name is " + fullName);
			if (null != fullName) {
				// yes, pass it on
				NodeScheduler.getInstance().put(fullName, data);
			}
		}
	}

	/**
	 * Chains default output of this node to default input of next node.
	 *
	 * @param next node
	 */
	@Override
	public void chainNext(final IScheduledNode next) {
		chainNext(INode.DEFAULT, next, INode.DEFAULT);
	}

	/**
	 * Chains named output of this node to default input of next node.
	 *
	 * @param outName
	 * @param next node
	 */
	@Override
	public void chainNext(final String outName, final IScheduledNode next) {
		chainNext(outName, next, INode.DEFAULT);
	}

	/**
	 * Chains default output of this node to named input of next node.
	 *
	 * @param next node
	 * @param inName
	 */
	@Override
	public void chainNext(final IScheduledNode next, final String inName) {
		chainNext(INode.DEFAULT, next, inName);
	}

	/**
	 * Chains named output of this node to named input of next node.
	 *
	 * @param outName
	 * @param next node
	 * @param inName
	 */
	@Override
	public void chainNext(final String outName, final IScheduledNode next,
		final String inName)
	{
		NodeScheduler.getInstance().chain(this, outName, next, inName);
	}

	/**
	 * Chains default input of this node to default output of previous node.
	 *
	 * @param previous node
	 */
	@Override
	public void chainPrevious(final IScheduledNode previous) {
		chainPrevious(INode.DEFAULT, previous, INode.DEFAULT);
	}

	/**
	 * Chains named input of this node to default output of previous node.
	 *
	 * @param inName
	 * @param previous node
	 */

	@Override
	public void chainPrevious(final String inName, final IScheduledNode previous)
	{
		chainPrevious(inName, previous, INode.DEFAULT);
	}

	/**
	 * Chains default input of this node to named output of previous node.
	 *
	 * @param previous node
	 * @param outName
	 */
	@Override
	public void
		chainPrevious(final IScheduledNode previous, final String outName)
	{
		chainPrevious(INode.DEFAULT, previous, outName);
	}

	/**
	 * Chains named input of this node to named output of previous node.
	 *
	 * @param inName
	 * @param previous node
	 * @param outName
	 */
	@Override
	public void chainPrevious(final String inName, final IScheduledNode previous,
		final String outName)
	{
		NodeScheduler.getInstance().chain(previous, outName, this, inName);
	}

	/**
	 * Loops until quitting time.
	 */
	@Override
	public void run() {
		System.out.println("Node " + m_id + " initiated");
		try {
			while (true) {
				System.out.println("input names " + getInputNames());
				// wait for all the input data as annotated
				for (final String inputName : getInputNames()) {
					final Object data = internalGet(inputName);
					System.out.println("got " + inputName);
					// save data in local map
					m_inputs.put(inputName, data);
				}
				System.out.println("now run " + m_inputs.keySet());
				// now run the subclass main routine
				m_idle = false;
				process();
				m_idle = true;

				// done with input data map
				m_inputs.clear();
			}
		}
		catch (final TeardownException e) {
			System.out.println("Node " + m_id + " terminated " +
				(m_idle ? "" : "not ") + "idle");
			m_inputs.clear();
		}
	}

	/**
	 * Signals quitting time.
	 */
	@Override
	public void quit() {
		NodeScheduler.getInstance().quit();
	}

	/**
	 * Feeds an image to the default input of the subclass.
	 *
	 * @param image
	 */
	@Override
	public void externalPut(final Object object) {
		externalPut(INode.DEFAULT, object);
	}

	/**
	 * Feeds an image to a named input of the subclass.
	 *
	 * @param inName
	 * @param data
	 */
	@Override
	public void externalPut(final String inName, final Object object) {
		if (isAnnotatedName(InputOutput.INPUT, inName)) {
			final String fullInName = uniqueInstance(inName);
			NodeScheduler.getInstance().put(fullInName, object);
		}
	}

	/**
	 * Gets the set of annotated input names.
	 *
	 * @return set of names
	 */
	public Set<String> getInputNames() {
		return m_inputNames;
	}

	/**
	 * Gets the set of annotated output names.
	 *
	 * @return set of names
	 */
	public Set<String> getOutputNames() {
		return m_outputNames;
	}

	/**
	 * Builds a set of input object names from the subclass annotations.
	 * 
	 * @param nodeClass
	 * @return set of names
	 */
	private Set<String> getInputNamesFromAnnotations() {
		final Set<String> set = new HashSet<String>();
		final Annotation annotation = this.getClass().getAnnotation(Input.class);
		if (annotation instanceof Input) {
			final Input inputs = (Input) annotation;
			final Img images[] = inputs.value();
			for (final Img image : images) {
				set.add(image.value());
			}
		}
		return set;
	}

	/**
	 * Builds a set of output image names from the subclass annotations.
	 *
	 * @param nodeClass
	 * @return
	 */
	private Set<String> getOutputNamesFromAnnotations() {
		final Set<String> set = new HashSet<String>();
		final Annotation annotation = this.getClass().getAnnotation(Output.class);
		if (annotation instanceof Output) {
			final Output inputs = (Output) annotation;
			final Img images[] = inputs.value();
			for (final Img image : images) {
				set.add(image.value());
			}
		}
		return set;
	}

	/**
	 * Checks whether a given name appears in the annotations for input or output
	 * images. Puts out an error message.
	 *
	 * @param input whether input or output
	 * @param name putative input/output name
	 * @return whether or not annotated
	 */
	private boolean isAnnotatedName(final InputOutput inOut, final String name) {
		boolean returnValue = true;
		final Set<String> names =
			(InputOutput.INPUT == inOut) ? getInputNames() : getOutputNames();
		if (!names.contains(name)) {
			nameNotAnnotated(inOut, name);
			returnValue = false;
		}
		return returnValue;
	}

	/**
	 * Puts out an error message that an annotation is missing.
	 *
	 * @param inOut whether input or output
	 * @param name
	 */
	private void nameNotAnnotated(final InputOutput inOut, final String name) {
		System.out.println("Missing annotation: @" +
			((InputOutput.INPUT == inOut) ? "In" : "Out") + "put({@Img=\"" + name +
			"\"})");
	}

	/**
	 * Gets an input image from the node scheduler.
	 *
	 * @param inName
	 * @return Image
	 */
	private Image internalGet(final String inName) {
		final String fullName = uniqueInstance(inName);
		return (Image) NodeScheduler.getInstance().get(fullName);
	}

}
