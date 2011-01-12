/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import loci.workflow.plugin.ItemWrapper;

/**
 *
 * @author aivar
 */
public class PluginWiringTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PluginWiringTest(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(PluginWiringTest.class);
    }

    public void testPluginWiring()
    {
        // create some test plugin modules
        PluginModule module1 = new PluginModule("loci.workflow.TestPlugin");
        PluginModule module2 = new PluginModule("loci.workflow.TestPlugin2");

        // create workflow, add & wire modules
        IWorkFlow workflow = new WorkFlow();
        workflow.setName("My Workflow");
        workflow.add(module1);
        workflow.add(module2);
        workflow.wire(module1, TestPlugin.ORANGE, module2, TestPlugin2.GREEN);
        workflow.wire(module1, TestPlugin.PURPLE, module2, TestPlugin2.BLUE);
        workflow.finalize();

        // create input item, start workflow
        ItemWrapper item = new ItemWrapper("HELLO");
        workflow.input(item);

        System.out.println("workflow [" + workflow.toXML() + "]");
    }
}
