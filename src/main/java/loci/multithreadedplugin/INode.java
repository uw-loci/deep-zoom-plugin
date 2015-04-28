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

/**
 * Defines call that are made externally to an INode.
 *
 * @author Aivar Grislis
 */
public interface INode extends IScheduledNode {

	/**
	 * Default input/output data name if none is specified.
	 */
	public static final String DEFAULT = "DEFAULT";

	/**
	 * Chains default output of this node to default input of next.
	 *
	 * @param next
	 */
	public void chainNext(IScheduledNode next);

	/**
	 * Chains named output of this node to default input of next.
	 *
	 * @param outName
	 * @param next
	 */
	public void chainNext(String outName, IScheduledNode next);

	/**
	 * Chains default output of this node to named input of next.
	 *
	 * @param next
	 * @param inName
	 */
	public void chainNext(IScheduledNode next, String inName);

	/**
	 * Chains named output of this node to named output of next.
	 *
	 * @param outName
	 * @param next
	 * @param inName
	 */
	public void chainNext(String outName, IScheduledNode next, String inName);

	/**
	 * Chains default input of this node to default output of previous.
	 *
	 * @param previous
	 */
	public void chainPrevious(IScheduledNode previous);

	/**
	 * Chains named input of this node to default output of previous.
	 *
	 * @param inName
	 * @param previous
	 */
	public void chainPrevious(String inName, IScheduledNode previous);

	/**
	 * Chains default input of this node to named output of previous.
	 *
	 * @param previous
	 * @param outName
	 */
	public void chainPrevious(IScheduledNode previous, String outName);

	/**
	 * Chains named input of this node to named output of previous.
	 *
	 * @param inName
	 * @param previous
	 * @param outName
	 */
	public void chainPrevious(String inName, IScheduledNode previous,
		String outName);

	/**
	 * Used to put default data from outside the node. An external put provides
	 * data for an internal get.
	 *
	 * @param data
	 */
	public void externalPut(Object data);

	/**
	 * Used to put named data from outside the node. Am external put provides data
	 * for an internal get.
	 *
	 * @param inName
	 * @param data
	 */
	public void externalPut(String inName, Object data);

	/**
	 * Stops this node.
	 */
	public void quit();

	/**
	 * The implementation of this method gets and puts data to do the data
	 * processing work of the node.
	 */
	void run();

	/**
	 * Used within the run method. Gets default input data.
	 *
	 * @return data
	 */
	Object get();

	/**
	 * Used within the run method. Gets named input data.
	 *
	 * @param inName
	 * @return data
	 */
	Object get(String inName);

	/**
	 * Used within the run method. Puts default output data.
	 *
	 * @param data
	 */
	void put(Object data);

	/**
	 * Used within the run method. Puts named output data.
	 *
	 * @param outName
	 * @param data
	 */
	void put(String outName, Object data);
}
