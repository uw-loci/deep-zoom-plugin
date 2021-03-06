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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Handles named data passing among nodes.
 *
 * @author Aivar Grislis
 */
public class NodeScheduler {

	private static NodeScheduler INSTANCE = null;
	private static final Object m_synchObject = new Object();
	private volatile boolean m_quit;
	private final Map<String, BlockingQueue<Object>> m_queueMap =
		new HashMap<String, BlockingQueue<Object>>();

	/**
	 * Singleton, with private constructor.
	 */
	private NodeScheduler() {}

	/**
	 * Gets the singleton.
	 *
	 * @return singleton instance
	 */
	public static synchronized NodeScheduler getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new NodeScheduler();
		}
		return INSTANCE;
	}

	/**
	 * Tears down the chained nodes.
	 */
	public void quit() {
		m_quit = true;
	}

	/**
	 * Chains the named data from one plugin to another.
	 *
	 * @param outNode source node
	 * @param outName source node's name
	 * @param inNode destination node
	 * @param inName destination node's name
	 */
	public void chain(final IScheduledNode outNode, final String outName,
		final IScheduledNode inNode, final String inName)
	{
		// build a full destination name tied to this particular destination node
		// instance
		final String fullInName = inNode.uniqueInstance(inName);

		// make sure there is a queue for destination node + name
		getQueue(fullInName);

		// within the source node instance, save the association of its output name
		// with destination node + name
		outNode.associate(outName, fullInName);
	}

	/**
	 * Passes data to destination node + name.
	 * 
	 * @param fullInName
	 * @param data
	 */
	public void put(final String fullInName, final Object data) {
		boolean success = false;
		final BlockingQueue<Object> queue = getQueue(fullInName);
		while (!success) {
			try {
				success = queue.offer(data, 100, TimeUnit.MILLISECONDS);
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Put interrupted");
			}
			if (m_quit) {
				throw new TeardownException("Teardown");
			}
		}
	}

	/**
	 * Gets data for destination node + name.
	 *
	 * @param fullInName
	 * @return data
	 */
	public Object get(final String fullInName) {
		Object data = null;
		final BlockingQueue<Object> queue = getQueue(fullInName);
		while (null == data) {
			try {
				data = queue.poll(100, TimeUnit.MILLISECONDS);
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Get interrupted");
			}
			if (m_quit) {
				throw new TeardownException("Teardown");
			}
		}
		return data;
	}

	/**
	 * Gets the queue for a given destination node + name. Creates it if
	 * necessary.
	 *
	 * @param fullInName
	 * @return the queue
	 */
	private BlockingQueue<Object> getQueue(final String fullInName) {
		BlockingQueue<Object> queue = null;
		synchronized (m_synchObject) {
			queue = m_queueMap.get(fullInName);
			if (null == queue) {
				queue = new LinkedBlockingQueue<Object>();
				// queue = new LinkedBlockingQueue<Object>(1);
				m_queueMap.put(fullInName, queue);
			}
		}
		return queue;
	}
}
