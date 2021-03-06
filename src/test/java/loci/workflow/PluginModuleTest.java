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
import loci.deepzoom.workflow.IModule;
import loci.deepzoom.workflow.ModuleFactory;
import loci.deepzoom.workflow.PluginModule;

/**
 * TODO
 *
 * @author Aivar Grislis
 */
public class PluginModuleTest extends TestCase {

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public PluginModuleTest(final String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(PluginModuleTest.class);
	}

	public void testPluginModule() {
		final PluginModule module1 = new PluginModule("loci.workflow.TestPlugin");
		final String xml1 = module1.toXML();

		String xml2 = null;
		try {
			final IModule module2 = ModuleFactory.getInstance().create(xml1);
			xml2 = module2.toXML();
		}
		catch (final Exception e) {
			System.out
				.println("exception creating plugin from XML " + e.getMessage());
		}
		assertTrue(xml1.equals(xml2));

		System.out.println("XML is [[" + xml2 + "]]");
	}
}
