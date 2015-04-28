/*
 * #%L
 * Deep Zoom plugin for ImageJ.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.workflow;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import loci.deepzoom.util.xmllight.XMLException;
import loci.deepzoom.workflow.IModule;
import loci.deepzoom.workflow.IWorkFlow;
import loci.deepzoom.workflow.ModuleFactory;
import loci.deepzoom.workflow.PluginModule;
import loci.deepzoom.workflow.WorkFlow;
import loci.deepzoom.workflow.plugin.ItemWrapper;

/**
 * TODO
 *
 * @author Aivar Grislis
 */
public class PluginWiringTest extends TestCase {
    String m_xml;

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
        workflow.wire(module1, TestPlugin.LOWER, module2, TestPlugin2.SECOND);
        workflow.wire(module1, TestPlugin.UPPER, module2, TestPlugin2.FIRST);
        workflow.finalize();

        // roundtrip workflow to/from XML
        String xml = workflow.toXML();
        IModule workflow2 = null;
        try {
            workflow2 = ModuleFactory.getInstance().create(xml);
        }
        catch (XMLException e) {
            System.out.println("XML problem " + e.getMessage());
        }

        // create input item, start workflow
        ItemWrapper item = new ItemWrapper("HELLO");
        workflow2.input(item);

        System.out.println("workflow [" + workflow.toXML() + "]");
    }
}
