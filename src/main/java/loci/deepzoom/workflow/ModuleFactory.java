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

package loci.deepzoom.workflow;

import java.util.HashMap;
import java.util.Map;

import loci.deepzoom.util.xmllight.XMLException;
import loci.deepzoom.util.xmllight.XMLParser;
import loci.deepzoom.util.xmllight.XMLTag;

/**
 * TODO
 *
 * @author Aivar Grislis
 */
public class ModuleFactory implements IModuleFactory {

	private static ModuleFactory s_instance;
	private final Map<String, IModuleFactory> m_factories =
		new HashMap<String, IModuleFactory>();

	private ModuleFactory() {
		register(WorkFlow.WORKFLOW, WorkFlowFactory.getInstance());
		// register(Component.COMPONENT, ComponentFactory.getInstance());
		register(PluginModule.PLUGIN, PluginModuleFactory.getInstance());
	}

	/**
	 * Gets singleton instance.
	 *
	 * @return instance
	 */
	public static synchronized ModuleFactory getInstance() {
		if (null == s_instance) {
			s_instance = new ModuleFactory();
		}
		return s_instance;
	}

	public void register(final String tagName, final IModuleFactory factory) {
		m_factories.put(tagName, factory);
	}

	/**
	 * Creates a component from XML.
	 *
	 * @param xml
	 * @return
	 */
	@Override
	public IModule create(final String xml) throws XMLException {
		IModule module = null;
		final XMLParser xmlHelper = new XMLParser();
		final XMLTag tag = xmlHelper.getNextTag(xml);
		final IModuleFactory factory = m_factories.get(tag.getName());
		if (null != factory) {
			module = factory.create(xml);
		}
		else {
			throw new XMLException("Invalid tag " + tag.getName());
		}
		return module;
	}
}
