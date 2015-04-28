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

import loci.deepzoom.plugin.annotations.Img;
import loci.deepzoom.plugin.annotations.Input;
import loci.deepzoom.plugin.annotations.Output;
import loci.deepzoom.workflow.plugin.AbstractPlugin;
import loci.deepzoom.workflow.plugin.IPlugin;
import loci.deepzoom.workflow.plugin.ItemWrapper;

/**
 * TODO
 *
 * @author Aivar Grislis
 */
@Input
@Output({ @Img(TestPlugin.UPPER), @Img(TestPlugin.LOWER) })
public class TestPlugin extends AbstractPlugin implements IPlugin {

	static final String UPPER = "UPPER";
	static final String LOWER = "LOWER";

	@Override
	public void process() {
		System.out.println("in TestPlugin");
		final ItemWrapper item1 = get();
		final String string1 = (String) item1.getItem();
		final String string2 = string1.toUpperCase();
		final String string3 = string1.toLowerCase();
		final ItemWrapper item2 = new ItemWrapper(string2);
		final ItemWrapper item3 = new ItemWrapper(string3);
		put(UPPER, item2);
		put(LOWER, item3);
	}
}
