/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multiinstanceplugin;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import loci.plugin.annotations.Img;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

/**
 * The PluginAnnotations class keeps sets of input and output names based
 * on plugin class annotations.
 *
 * @author Aivar Grislis
 */
public class PluginAnnotations {
    enum InputOutput { INPUT, OUTPUT };
    Set<String> m_inputNames;
    Set<String> m_outputNames;

    /**
     * Creates an instance for a given plugin class.
     *
     * @param pluginClass
     */
    PluginAnnotations(Class pluginClass) {
        // build sets of input and output names from annotations
        m_inputNames = getInputNamesFromAnnotations(pluginClass);
        m_outputNames = getOutputNamesFromAnnotations(pluginClass);
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
     * Checks whether a given name appears in the annotations for input or
     * output images.  Puts out an error message.
     *
     * @param input whether input or output
     * @param name putative input/output name
     * @return whether or not annotated
     */
    public boolean isAnnotatedName(InputOutput inOut, String name) {
        boolean returnValue = true;
        Set<String> names = (InputOutput.INPUT == inOut) ? getInputNames() : getOutputNames();
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
    static void nameNotAnnotated(InputOutput inOut, String name) {
        System.out.println("Missing annotation: @" + ((InputOutput.INPUT == inOut) ? "In" : "Out") + "put({@Img=\"" + name + "\"})" );
    }

    /**
     * Builds a set of input object names from the subclass annotations.
     *
     * @param pluginClass
     * @return set of names
     */
    private Set<String> getInputNamesFromAnnotations(Class pluginClass) {
        Set<String> set = new HashSet<String>();
        if (null != pluginClass) {
            Annotation annotation = pluginClass.getAnnotation(Input.class);
            if (annotation instanceof Input) {
                Input inputs = (Input) annotation;
                Img images[] = inputs.value();
                if (0 == images.length) {
                    set.add(Input.DEFAULT);
                }
                else {
                    for (Img image : images) {
                        set.add(image.value());
                    }
                }
            }
        }
        return set;
    }

    /**
     * Builds a set of output image names from the subclass annotations.
     *
     * @param pluginClass
     * @return set of names
     */
    private Set<String> getOutputNamesFromAnnotations(Class pluginClass) {
        Set<String> set = new HashSet<String>();
        if (null != pluginClass) {
            Annotation annotation = pluginClass.getAnnotation(Output.class);
            if (annotation instanceof Output) {
                Output inputs = (Output) annotation;
                Img images[] = inputs.value();
                if (0 == images.length) {
                    set.add(Output.DEFAULT);
                }
                else {
                    for (Img image : images) {
                        set.add(image.value());
                    }
                }
            }
        }
        return set;
    }
}
