/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 *
 * @author aivar
 */
public interface IComponentFactory {

    public IComponent create(String xml);
}
