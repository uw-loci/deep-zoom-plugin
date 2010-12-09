/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multiinstanceplugin;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import loci.plugin.ImageWrapper;

/**
 *
 * @author Aivar Grislis
 */
public class PluginLauncher implements IPluginLauncher {
    public static boolean s_singleInstance = false;
    UUID m_id = UUID.randomUUID();
    Class m_pluginClass;
    PluginAnnotations m_annotations;
    Thread m_thread;
    volatile boolean m_quit = false;
    Map<String, String> m_outputNames = new HashMap();

    public PluginLauncher(Class pluginClass, PluginAnnotations annotations) {
        m_pluginClass = pluginClass;
        m_annotations = annotations;
        m_thread = new LauncherThread();
        m_thread.setDaemon(true);
        m_thread.start();
    }

    public void chainNext(String outName, IPluginLauncher next, String inName) {
        PluginScheduler.getInstance().chain(this, outName, next, inName);
    }
    
    public void chainPrevious(String inName, IPluginLauncher previous, String outName) {
        PluginScheduler.getInstance().chain(previous, outName, this, inName);
    }

    public void externalPut(String name, ImageWrapper image) {
        String fullInName = uniqueName(name);
        PluginScheduler.getInstance().put(fullInName, image);
    }

    public String uniqueName(String name) {
        return m_id.toString() + '-' + name;
    }

    public void associate(String outName, String fullInName) {
        m_outputNames.put(outName, fullInName);
    }

    public void quit() {
        m_quit = true;
    }

    private class LauncherThread extends Thread {
        public void run() {
            Set<String> inputNames = m_annotations.getInputNames();
            while (!m_quit) {
                // assemble a set of input images
                Map<String, ImageWrapper> inputImages = new HashMap();
                for (String inputName : inputNames) {
                    String fullInName = uniqueName(inputName);
                    ImageWrapper image = PluginScheduler.getInstance().get(fullInName);
                    inputImages.put(inputName, image);
                }

                //TODO Good place to throttle thread creation here
                PluginScheduler.getInstance().reportNewPlugin(m_pluginClass.getSimpleName());

                // launch the plugin for this set of images
                Thread pluginThread = new PluginThread(inputImages);
                pluginThread.start();

                if (s_singleInstance) {
                    try {
                        pluginThread.join();
                    }
                    catch (InterruptedException e) {
                        System.out.println("LauncherThread.run() insterrupted on join");
                    }
                }
            }
        }
    }

    private class PluginThread extends Thread {
        Map<String, ImageWrapper> m_inputImages;
        IPluginInternal m_pluginInstance;

        PluginThread(Map<String, ImageWrapper> inputImages) {
            m_inputImages = inputImages;
            m_pluginInstance = null;
            try {
                m_pluginInstance = (IPluginInternal) m_pluginClass.newInstance();
            }
            catch (InstantiationException e) {
                System.out.println("m_pluginClass " + m_pluginClass);
                System.out.println("Problem instantiating plugin " + m_pluginClass.getSimpleName() + ' ' + e.getMessage());
            }
            catch (IllegalAccessException e) {
                System.out.println("Illegal access instantiating plugin " + m_pluginClass.getSimpleName() + ' ' + e.getMessage());
            }
        }

        public void run() {
            if (null != m_pluginInstance) {
                m_pluginInstance.start(m_inputImages, m_outputNames);
            }
        }
    }
}
