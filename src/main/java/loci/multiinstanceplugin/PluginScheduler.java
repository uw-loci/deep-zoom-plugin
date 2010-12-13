/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multiinstanceplugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import loci.plugin.ImageWrapper;

/**
 * Handles named image passing among plugins.
 *
 * @author Aivar Grislis
 */
public class PluginScheduler {
    private static PluginScheduler INSTANCE = null;
    private static final Object m_synchObject = new Object();
    private volatile boolean m_quit;
    private Map<String, BlockingQueue<ImageWrapper>> m_queueMap = new HashMap<String, BlockingQueue<ImageWrapper>>();

    /**
     * Singleton, with private constructor.
     */
    private PluginScheduler() { }

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
    }

    /**
     * Chains the named image from one plugin to another.
     *
     * @param out source plugin
     * @param outName source plugin's name
     * @param in destination plugin
     * @param inName destination plugin's name
     */
    public void chain(IPluginLauncher out, String outName, IPluginLauncher in, String inName) {
       // build a fully-qualified destination name
       String fullInName = in.uniqueName(inName);

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
    public void put(String fullInName, ImageWrapper image) {
        boolean success = false;
        BlockingQueue<ImageWrapper> queue = getQueue(fullInName);
        while (!success) {
            try {
                success = queue.offer(image, 100, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
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
    public ImageWrapper get(String fullInName) {
        ImageWrapper image = null;
        BlockingQueue<ImageWrapper> queue = getQueue(fullInName);
        while (null == image) {
            try {
                image = queue.poll(100, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Get interrupted");
            }
            if (m_quit) {
                throw new TeardownException("Teardown");
            }
        }
        return image;
    }

    public synchronized void reportNewPlugin(String name) {
        System.out.println("Running " + name);
    }

    /**
     * Gets the queue for a given, fully-qualified input name.  Creates it if
     * necessary.
     *
     * @param fullInName
     * @return the queue
     */
    private BlockingQueue<ImageWrapper> getQueue(String fullInName) {
        BlockingQueue<ImageWrapper> queue = null;
        synchronized (m_synchObject) {
            queue = m_queueMap.get(fullInName);
            if (null == queue) {
                queue = new LinkedBlockingQueue<ImageWrapper>();
                m_queueMap.put(fullInName, queue);
            }
        }
        return queue;
    }
}