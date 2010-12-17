/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 *
 * @author Aivar Grislis
 */
public class WorkFlowFactory {

    /**
     * Creates a workflow from XML.
     *
     * @param xml
     * @return
     */
    public static IWorkFlow create(String xml) {
        IWorkFlow workFlow = new WorkFlow();
        workFlow.fromXML(xml);
        return workFlow;
    }

}
