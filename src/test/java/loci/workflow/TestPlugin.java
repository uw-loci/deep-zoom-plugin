/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import loci.workflow.plugin.AbstractPlugin;
import loci.workflow.plugin.IPlugin;
import loci.workflow.plugin.ItemWrapper;
import loci.plugin.annotations.Img;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

/**
 *
 * @author aivar
 */
@Input
@Output({ @Img(TestPlugin.PURPLE), @Img(TestPlugin.ORANGE) })
public class TestPlugin extends AbstractPlugin implements IPlugin {
    static final String PURPLE = "PURPLE";
    static final String ORANGE = "ORANGE";

    public void process() {
        System.out.println("in TestPlugin");
        ItemWrapper item1 = get();
        String string1 = (String) item1.getItem();
        String string2 = string1.toUpperCase();
        String string3 = string1.toLowerCase();
        ItemWrapper item2 = new ItemWrapper(string2);
        ItemWrapper item3 = new ItemWrapper(string3);
        put(PURPLE, item2);
        put(ORANGE, item3);
    }
}
