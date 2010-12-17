/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 * Data structure that describes a chained connection.
 *
 * @author Aivar Grislis
 */
public class Chain {
    final IComponent m_source;
    final String m_sourceName;
    final IComponent m_dest;
    final String m_destName;

    Chain(IComponent source, String sourceName, IComponent dest, String destName) {
        m_source = source;
        m_sourceName = sourceName;
        m_dest = dest;
        m_destName = destName;
    }

    IComponent getSource() {
        return m_source;
    }

    String getSourceName() {
        return m_sourceName;
    }

    IComponent getDest() {
        return m_dest;
    }

    String getDestName() {
        return m_destName;
    }
}

