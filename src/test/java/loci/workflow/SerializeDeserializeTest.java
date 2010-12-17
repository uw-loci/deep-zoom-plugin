/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

/**
 * Unit test for save/restore to/from XML.
 */
public class SerializeDeserializeTest extends TestCase
{
    private static final String XML_A = "<testA>whatever</testA>";
    private static final String XML_B = "<testB>whatever</testB>";

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SerializeDeserializeTest(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(SerializeDeserializeTest.class);
    }

    /**
     * Round trip to/from XML.
     */
    public void testXML()
    {
        TestComponent testComponentA = new TestComponent();
        testComponentA.setName("A");
        testComponentA.setInputNames(new String[] { "ONE", "TWO" });
        testComponentA.setOutputNames(new String[] { Output.DEFAULT });
        testComponentA.setXML(XML_A);
        TestComponent testComponentB = new TestComponent();
        testComponentB.setName("B");
        testComponentB.setInputNames(new String[] { Input.DEFAULT } );
        testComponentB.setOutputNames(new String[] { Output.DEFAULT });
        testComponentB.setXML(XML_B);

        TestComponentFactory componentFactory = new TestComponentFactory();
        componentFactory.set(XML_A, testComponentA);
        componentFactory.set(XML_B, testComponentB);

        WorkFlow workFlow1 = new WorkFlow();
        workFlow1.setName("workFlow1");
        workFlow1.setComponentFactory(componentFactory);
        System.out.println("just set component factory");
        workFlow1.add(testComponentA);
        workFlow1.add(testComponentB);
        workFlow1.chain(testComponentA, testComponentB);
        workFlow1.chainInput("RED", testComponentA, "ONE");
        workFlow1.chainInput("BLUE", testComponentA, "TWO");
        workFlow1.chainOutput(testComponentB);

        String xml1 = workFlow1.toXML();
        System.out.println("workFlow1 XML:\n" + xml1);

        WorkFlow workFlow2 = new WorkFlow();
        workFlow2.fromXML(xml1);
        String xml2 = workFlow2.toXML();

        System.out.println("workFlow2 XML:\n" + xml2);

        assertTrue(xml1.equals(xml2));
    }
}
