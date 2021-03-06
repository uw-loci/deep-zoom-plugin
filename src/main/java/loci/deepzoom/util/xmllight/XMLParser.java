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
 * XML-light Parser.
 *
 * @author Aivar Grislis
 */
public class XMLParser {

	/**
	 * Parses the XML string and returns the XMLTag data structure for the first
	 * tag it encounters. For {@code <one>abc</one><two>def</two>},
	 * {@code XMLTag.name} is {@code one}, {@code XMLTag.content} is {@code abc},
	 * and {@code XMLTag.remainder} is {@code <two>def</two>}.
	 */
	public XMLTag getNextTag(String xml) throws XMLException {
		xml = xml.trim();
		if (xml.isEmpty()) {
			return new XMLTag();
		}
		if (!xml.startsWith("<") || !xml.endsWith(">")) {
			throw new XMLException("Mismatched '<' '>'");
		}
		try {
			final int endBracketIndex = xml.indexOf('>');
			final int startContentIndex = endBracketIndex + 1;

			final String name = xml.substring(1, endBracketIndex);

			final String endTag = "</" + name + ">";
			final int endTagIndex = xml.indexOf(endTag);
			if (-1 == endTagIndex) {
				throw new XMLException("Missing " + endTag);
			}
			final int remainderIndex = endTagIndex + endTag.length();

			final String content = xml.substring(startContentIndex, endTagIndex);

			final String remainder = xml.substring(remainderIndex, xml.length());

			return new XMLTag(name.trim(), content.trim(), remainder.trim());
		}
		catch (final IndexOutOfBoundsException e) {
			throw new XMLException("Improper XML");
		}
	}
}
