/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import java.lang.StringBuilder;

/**
 *
 * @author aivar
 */
public class XMLHelper {
    StringBuilder m_string;
    int m_indent = 0;
    
    public XMLHelper() {
        m_string = null;
    }
    
    public XMLHelper(StringBuilder string) {
        m_string = string;
    }
    
    public XMLTag getNextTag(String xml) throws XMLException {
        return getTag(false, xml);
    }
    
    public XMLTag getNextTagInclusive(String xml) throws XMLException {
        return getTag(true, xml);
    }

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

    public void addTag(String name) {
        doIndent();
        m_string.append('<');
        m_string.append(name);
        m_string.append('>');
        m_string.append('\n');
        ++m_indent;
    }

    public void addEndTag(String name) {
        --m_indent;
        doIndent();
        m_string.append('<');
        m_string.append('/');
        m_string.append(name);
        m_string.append('>');
        m_string.append('\n');
    }

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

    public void add(String output) {
        String lines[] = output.split("\n");
        for (String line: lines) {
            doIndent();
            m_string.append(output);
            m_string.append('\n');
        }
    }

    private void doIndent() {
        for (int i = 0; i < 2 * m_indent; ++i) {
            m_string.append(' ');
        }
    }
}
