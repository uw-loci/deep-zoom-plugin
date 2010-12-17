/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 * An interface to build a workflow based on components that are chained
 * together.  The workflow itself is also a component.
 *
 * Building a workflow should take place in three phases:
 *   I.    Add components
 *   II.   Chain components together
 *   III.  Chain workflow inputs and outputs.
 *
 * @author Aivar Grislis
 */
public interface IWorkFlow extends IComponent {

    /**
     * Adds a component to the workflow in phase I.
     *
     * @param component
     */
    void add(IComponent component);

    /**
     * Chains default output of one component to default input of another.
     * Phase II.
     *
     * @param source
     * @param dest
     */
    void chain(IComponent source, IComponent dest);

    /**
     * Chains named output of one component to default input of another.
     * Phase II.
     *
     * @param source
     * @param sourceName
     * @param dest
     */
    void chain(IComponent source, String sourceName, IComponent dest);

    /**
     * Chains default output of one component to named input of another.
     * Phase II.
     *
     * @param source
     * @param dest
     * @param destName
     */
    void chain(IComponent source, IComponent dest, String destName);

    /**
     * Chains named output of one component to named input of another.
     * Phase II.
     *
     * @param source
     * @param sourceName
     * @param dest
     * @param destName
     */
    void chain(IComponent source, String sourceName, IComponent dest, String destName);

    /**
     * Gets the current chains.  Should be called after Phase II.
     *
     * @return array of chains
     */
    Chain[] getChains();

    /**
     * Chains default workflow input to default input of component.
     * Phase III.
     *
     * @param dest
     */
    void chainInput(IComponent dest);

    /**
     * Chains default workflow input to named input of component.
     * Phase III.
     *
     * @param dest
     * @param destName
     */
    void chainInput(IComponent dest, String destName);

    /**
     * Chains named workflow input to default input of component.
     * Phase III.
     *
     * @param inName
     * @param dest
     */
    void chainInput(String inName, IComponent dest);

    /**
     * Chains named workflow input to named input of component.
     * Phase III.
     *
     * @param inName
     * @param dest
     * @param destName
     */
    void chainInput(String inName, IComponent dest, String destName);

    /**
     * Chains default component output to default workflow output.
     * Phase III.
     *
     * @param source
     */
    void chainOutput(IComponent source);

    /**
     * Chains named component output to default workflow output.
     * Phase III.
     *
     * @param source
     * @param sourceName
     */
    void chainOutput(IComponent source, String sourceName);

    /**
     * Chains default component output to named workflow output.
     * Phase III.
     * 
     * @param outName
     * @param source
     */
    void chainOutput(String outName, IComponent source);

    /**
     * Chains named component output to named workflow output.
     * Phase III.
     *
     * @param outName
     * @param source
     * @param sourceName
     */
    void chainOutput(String outName, IComponent source, String sourceName);

    /**
     * Saves chained components as XML string representation.
     * Only after Phase III is complete.
     *
     * @return
     */
    String toXML();

    /**
     * Restores chained components from XML string representation.
     * Accomplishes Phases I-III.
     *
     * @param xml
     * @return whether successfully parsed
     */
    boolean fromXML(String xml);
}
