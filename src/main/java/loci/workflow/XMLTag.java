/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 *
 * @author Aivar Grislis
 */
public class XMLTag {
    static final String EMPTY_STRING = "";
    private String m_name;
    private String m_content;
    private String m_remainder;
    
    public XMLTag() {
        m_name = EMPTY_STRING;
        m_content = EMPTY_STRING;
        m_remainder = EMPTY_STRING;
    }

    public XMLTag(String name, String content, String remainder) {
        m_name = name;
        m_content = content;
        m_remainder = remainder;
    }

    public String getName() {
        return m_name;
    }

    public String getContent() {
        return m_content;
    }

    public String getRemainder() {
        return m_remainder;
    }
}
