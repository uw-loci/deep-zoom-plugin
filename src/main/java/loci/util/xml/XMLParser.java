/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.util.xml;

/**
 * XML-light Parser.
 *
 * @author Aivar Grislis
 */
public class XMLParser {

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

            String remainder = xml.substring(remainderIndex, xml.length());

            return new XMLTag(name.trim(), content.trim(), remainder.trim());
        }
        catch (IndexOutOfBoundsException e) {
            throw new XMLException("Improper XML");
        }
    }
}
