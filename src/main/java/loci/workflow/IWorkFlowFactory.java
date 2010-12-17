/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 *
 * @author Aivar Grislis
 */
public interface IWorkFlowFactory {

    /**
     * Creates a workflow from XML.
     *
     * @param xml
     * @return
     */
    public IWorkFlow create(String xml);
}
