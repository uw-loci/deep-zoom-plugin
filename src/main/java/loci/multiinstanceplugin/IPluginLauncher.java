/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multiinstanceplugin;

import loci.plugin.ImageWrapper;

/**
 *
 * @author aivar
 */
public interface IPluginLauncher {

    public void chainNext(String outName, IPluginLauncher next, String inName);

    public void chainPrevious(String inName, IPluginLauncher previous, String outName);

    public void externalPut(String name, ImageWrapper image);

    public String uniqueName(String name);

    public void associate(String outName, String fullInName);

    public void quit();
}
