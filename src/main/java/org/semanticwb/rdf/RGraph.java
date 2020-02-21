/*
 * SemanticWebBuilder es una plataforma para el desarrollo de portales y aplicaciones de integración,
 * colaboración y conocimiento, que gracias al uso de tecnología semántica puede generar contextos de
 * información alrededor de algún tema de interés o bien integrar información y aplicaciones de diferentes
 * fuentes, donde a la información se le asigna un significado, de forma que pueda ser interpretada y
 * procesada por personas y/o sistemas, es una creación original del Fondo de Información y Documentación
 * para la Industria INFOTEC, cuyo registro se encuentra actualmente en trámite.
 *
 * INFOTEC pone a su disposición la herramienta SemanticWebBuilder a través de su licenciamiento abierto al público (‘open source’),
 * en virtud del cual, usted podrá usarlo en las mismas condiciones con que INFOTEC lo ha diseñado y puesto a su disposición;
 * aprender de él; distribuirlo a terceros; acceder a su código fuente y modificarlo, y combinarlo o enlazarlo con otro software,
 * todo ello de conformidad con los términos y condiciones de la LICENCIA ABIERTA AL PÚBLICO que otorga INFOTEC para la utilización
 * del SemanticWebBuilder 4.0.
 *
 * INFOTEC no otorga garantía sobre SemanticWebBuilder, de ninguna especie y naturaleza, ni implícita ni explícita,
 * siendo usted completamente responsable de la utilización que le dé y asumiendo la totalidad de los riesgos que puedan derivar
 * de la misma.
 *
 * Si usted tiene cualquier duda o comentario sobre SemanticWebBuilder, INFOTEC pone a su disposición la siguiente
 * dirección electrónica:
 *  http://www.semanticwebbuilder.org
 */
package org.semanticwb.rdf;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;

/**
 * Interface to define RDF Graphs and operations.
 * @author javier.solis.g
 */
public interface RGraph {
    /**
     * Gets graph ID.
     * @return Graph ID.
     */
    int getId();

    /**
     * Adds a triple to the graph.
     * @param triple Triple to add.
     * @param id Triple ID.
     */
    void performAdd(Triple triple, Long id);

    /**
     * Removes a triple from the graph.
     * @param triple Triple to remove.
     * @param id Triple ID.
     */
    void performDelete(Triple triple, Long id);

    /**
     * Gets the {@link TransactionHandler} for this Graph.
     * @return TransactionHandler
     */
    TransactionHandler getTransactionHandler();

    /**
     * Encodes subject component of an RDF Node (subject, property, object).
     * @param node RDF node
     * @return Encoded subject component.
     */
    String encodeSubject(Node node);

    /**
     * Encodes property component of an RDF Node (subject, property, object).
     * @param node RDF node
     * @return Encoded property component
     */
    String encodeProperty(Node node);

    /**
     * Encodes object component of an RDF Node (subject, property, object).
     * @param node RDF node
     * @return Encoded object component
     */
    String encodeObject(Node node);

    /**
     * Decodes (from a String) the subject component for an RDF Node (subject, property, object).
     * @param sub String to decode.
     * @return Decoded subject component on RDF node.
     */
    Node decodeSubject(String sub, String ext);

    /**
     * Decodes (from a String) the property component for an RDF Node (subject, property, object).
     * @param prop String to decode.
     * @return Decoded subject component on RDF node.
     */
    Node decodeProperty(String prop, String ext);

    /**
     * Decodes (from a String) the object component for an RDF Node (subject, property, object).
     * @param obj String to decode.
     * @return Decoded subject component on RDF node.
     */
    Node decodeObject(String obj, String ext);
}