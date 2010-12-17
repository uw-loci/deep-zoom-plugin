/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import java.lang.StringBuilder;

/**
 * XML Parser & Writer.
 *
 * @author Aivar Grislis
 */
public class XMLHelper {
    StringBuilder m_string;
    int m_indent = 0;

    /**
     * Constructs a helper for parsing XML.
     */
    public XMLHelper() {
        m_string = null;
    }


    /**
     * Constructs a helper for writing to given
     * string.
     *
     * @param string
     */
    public XMLHelper(StringBuilder string) {
        m_string = string;
    }

    /**
     * Parses the string and gets the tag data structure for the first tag.
     *
     * @param xml
     * @return
     * @throws XMLException
     */
    public XMLTag getNextTag(String xml) throws XMLException {
        return getTag(false, xml);
    }

    /**
     * Parses the string and gets the tag data structure for the first tag.
     * This version includes the start and end tag in the content.
     *
     * @param xml
     * @return
     * @throws XMLException
     */
    public XMLTag getNextTagInclusive(String xml) throws XMLException {
        return getTag(true, xml);
    }

    /**
     * Parses the string and gets the tag data structure for the first tag.
     *
     * @param inclusive
     * @param xml
     * @return
     * @throws XMLException
     */
    private XMLTag getTag(boolean inclusive, String xml) throws XMLException {
        xml = xml.trim();
        if (xml.isEmpty()) {
            return new XMLTag();
        }
        if (!xml.startsWith("<") || !xml.endsWith(">")) {
            throw new XMLException("Mismatched '<' '>'");
        }
        try {
            int endBracketIndex = xml.indexOf('>');
            int startContentIndex = endBracketIndex + 1;
            String name = xml.substring(1, endBracketIndex);
            System.out.println("tag is [" + name + "]");
            String endTag = "</" + name + ">";
            int endTagIndex = xml.indexOf(endTag);
            int remainderIndex = endTagIndex + endTag.length();

            String content;
            if (inclusive) {
                content = xml.substring(0, remainderIndex);
            }
            else {
                content = xml.substring(startContentIndex, endTagIndex);
            }
            //System.out.println("content is [" + content + "]");

            String remainder = xml.substring(remainderIndex, xml.length());
            //System.out.println("remainder is [" + remainder + "]");

            return new XMLTag(name, content.trim(), remainder.trim());
        }
        catch (IndexOutOfBoundsException e) {
            throw new XMLException("Improper XML");
        }
    }

    /**
     * For writing, starts a new tag.
     *
     * @param name
     */
    public void addTag(String name) {
        doIndent();
        m_string.append('<');
        m_string.append(name);
        m_string.append('>');
        m_string.append('\n');
        ++m_indent;
    }

    /**
     * For writing, ends a tag.
     *
     * @param name
     */
    public void addEndTag(String name) {
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
    public void addTagWithContent(String name, String content) {
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
    public void add(String output) {
        String lines[] = output.split("\n");
        for (String line: lines) {
            doIndent();
            m_string.append(output);
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
