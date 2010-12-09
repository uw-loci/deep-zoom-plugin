/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multiinstanceplugin;

import java.util.Map;

import loci.plugin.ImageWrapper;

/**
 *
 * @author aivar
 */
public interface IPluginInternal {

    /**
     * Starts up processing.  Called from plugin launcher.
     *
     * @param inputImages
     * @param outputNames
     */
    public void start(
            Map<String, ImageWrapper> inputImages,
            Map<String, String> outputNames);
}
