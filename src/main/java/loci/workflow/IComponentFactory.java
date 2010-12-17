/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 *
 * @author Aivar Grislis
 */
public interface IComponentFactory {

    public IComponent create(String xml);
}
