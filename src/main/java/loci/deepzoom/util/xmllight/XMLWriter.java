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

package loci.deepzoom.util.xmllight;

/**
 * XML-light Writer.
 *
 * @author Aivar Grislis
 */
public class XMLWriter {

	StringBuilder m_string;
	int m_indent = 0;

	/**
	 * Constructs a helper for writing to given string.
	 *
	 * @param string
	 */
	public XMLWriter(final StringBuilder string) {
		m_string = string;
	}

	/**
	 * Starts a new tag.
	 *
	 * @param name
	 */
	public void addTag(final String name) {
		doIndent();
		m_string.append('<');
		m_string.append(name);
		m_string.append('>');
		m_string.append('\n');
		++m_indent;
	}

	/**
	 * Ends a tag.
	 *
	 * @param name
	 */
	public void addEndTag(final String name) {
		--m_indent;
		doIndent();
		m_string.append('<');
		m_string.append('/');
		m_string.append(name);
		m_string.append('>');
		m_string.append('\n');
	}

	/**
	 * Adds a tag with some content on a single line.
	 *
	 * @param name
	 * @param content
	 */
	public void addTagWithContent(final String name, final String content) {
		doIndent();
		m_string.append('<');
		m_string.append(name);
		m_string.append('>');
		m_string.append(content);
		m_string.append('<');
		m_string.append('/');
		m_string.append(name);
		m_string.append('>');
		m_string.append('\n');
	}

	/**
	 * Adds an embedded XML string with proper indent.
	 *
	 * @param output
	 */
	public void add(final String output) {
		final String lines[] = output.split("\n");
		for (final String line : lines) {
			doIndent();
			m_string.append(line);
			m_string.append('\n');
		}
	}

	/**
	 * Does indentation.
	 */
	private void doIndent() {
		for (int i = 0; i < 2 * m_indent; ++i) {
			m_string.append(' ');
		}
	}
}
