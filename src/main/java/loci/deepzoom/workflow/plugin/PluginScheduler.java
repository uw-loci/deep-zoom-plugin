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

package loci.deepzoom.workflow.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Schedules named image passing among plugins.
 *
 * @author Aivar Grislis
 */
public class PluginScheduler {

	private static PluginScheduler INSTANCE = null;
	private static final Object m_synchObject = new Object();
	private volatile boolean m_quit;
	private final Map<String, BlockingQueue<ItemWrapper>> m_queueMap =
		new HashMap<String, BlockingQueue<ItemWrapper>>();

	/**
	 * Singleton, with private constructor.
	 */
	private PluginScheduler() {}

	/**
	 * Gets the singleton.
	 *
	 * @return singleton instance
	 */
	public static synchronized PluginScheduler getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new PluginScheduler();
		}
		return INSTANCE;
	}

	/**
	 * Tears down the chained nodes.
	 */
	public void quit() {
		m_quit = true;
		// delete the queues; they will be rebuilt on demand
		synchronized (m_synchObject) {
			m_queueMap.clear();
		}
	}

	/**
	 * Chains the named image from one plugin to another.
	 *
	 * @param out source plugin
	 * @param outName source plugin's name
	 * @param in destination plugin
	 * @param inName destination plugin's name
	 */
	public void chain(final IPluginLauncher out, final String outName,
		final IPluginLauncher in, final String inName)
	{
		// patch for test components that don't actually have a launcher
		if (null == in) {
			return;
		}

		// build a fully-qualified destination name
		final String fullInName = in.uniqueName(inName);

		// make sure there is a queue for this name
		getQueue(fullInName);

		// within the source plugin instance, save the association of its output
		// name with fully-qualified input name
		out.associate(outName, fullInName);
	}

	/**
	 * Passes image to fully-qualified name.
	 *
	 * @param fullInName
	 * @param image
	 */
	public void put(final String fullInName, final ItemWrapper image) {
		boolean success = false;
		final BlockingQueue<ItemWrapper> queue = getQueue(fullInName);
		// TODO currently using an unlimited LinkedBlockingQueue, so will never
		// block
		while (!success) {
			try {
				success = queue.offer(image, 100, TimeUnit.MILLISECONDS);
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
	 * Gets image for fully-qualified name.
	 *
	 * @param fullInName
	 * @return image
	 */
	public ItemWrapper get(final String fullInName) {
		ItemWrapper image = null;
		final BlockingQueue<ItemWrapper> queue = getQueue(fullInName);
		while (null == image) {
			try {
				image = queue.poll(100, TimeUnit.MILLISECONDS);
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Get interrupted");
			}
			if (m_quit) {
				throw new TeardownException("Teardown");
			}
		}
		return image;
	}

	/**
	 * This is just for debugging.
	 *
	 * @param name
	 */
	public synchronized void reportNewPlugin(final String name) {
		System.out.println("Running " + name);
	}

	/**
	 * Gets the queue for a given, fully-qualified input name. Creates it if
	 * necessary.
	 *
	 * @param fullInName
	 * @return the queue
	 */
	private BlockingQueue<ItemWrapper> getQueue(final String fullInName) {
		BlockingQueue<ItemWrapper> queue = null;
		synchronized (m_synchObject) {
			queue = m_queueMap.get(fullInName);
			if (null == queue) {
				queue = new LinkedBlockingQueue<ItemWrapper>();
				m_queueMap.put(fullInName, queue);
			}
		}
		return queue;
	}
}
