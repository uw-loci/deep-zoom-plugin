/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multiinstanceplugin;

import java.util.Set;

import loci.plugin.ImageWrapper;

/**
 *
 * @author aivar
 */

public interface ILinkedPlugin {
    /**
     * Gets the set of annotated input names.
     *
     * @return set of names
     */
    public Set<String> getInputNames();

    /**
     * Gets the set of annotated output names.
     *
     * @return set of names
     */
    public Set<String> getOutputNames();

    /**
     * Chains default output of this node to default input of next.
     *
     * @param next
     */
    public void chainNext(ILinkedPlugin next);

    /**
     * Chains named output of this node to default input of next.
     *
     * @param outName
     * @param next
     */
    public void chainNext(String outName, ILinkedPlugin next);

    /**
     * Chains default output of this node to named input of next.
     *
     * @param next
     * @param inName
     */
    public void chainNext(ILinkedPlugin next, String inName);

    /**
     * Chains named output of this node to named output of next.
     *
     * @param outName
     * @param next
     * @param inName
     */
    public void chainNext(String outName, ILinkedPlugin next, String inName);

    /**
     * Chains default input of this node to default output of previous.
     *
     * @param previous
     */
    public void chainPrevious(ILinkedPlugin previous);

    /**
     * Chains named input of this node to default output of previous.
     *
     * @param inName
     * @param previous
     */
    public void chainPrevious(String inName, ILinkedPlugin previous);

    /**
     * Chains default input of this node to named output of previous.
     *
     * @param previous
     * @param outName
     */
    public void chainPrevious(ILinkedPlugin previous, String outName);

    /**
     * Chains named input of this node to named output of previous.
     *
     * @param inName
     * @param previous
     * @param outName
     */
    public void chainPrevious(String inName, ILinkedPlugin previous, String outName);

    /**
     * Used to put default image from outside the plugin.  An external put provides
     * image for an internal get from within this plugin.
     *
     * @param image
     */
    public void externalPut(ImageWrapper image);

    /**
     * Used to put named image from outside the plugin.  Am external put provides
     * image for an internal get from within this plugin.
     *
     * @param inName
     * @param image
     */
    public void externalPut(String inName, ImageWrapper image);

    /**
     * Gets the plugin launcher for this linked plugin.
     *
     * @return launcher
     */
    public IPluginLauncher getLauncher();

    /**
     * Quits processing images.
     */
    public void quit();
}
