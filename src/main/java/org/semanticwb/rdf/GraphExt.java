/*
 * SemanticWebBuilder es una plataforma para el desarrollo de portales y aplicaciones de integración,
 * colaboración y conocimiento, que gracias al uso de tecnología semántica puede generar contextos de
 * información alrededor de algún tema de interés o bien integrar información y aplicaciones de diferentes
 * fuentes, donde a la información se le asigna un significado, de forma que pueda ser interpretada y
 * procesada por personas y/o sistemas, es una creación original del Fondo de Información y Documentación
 * para la Industria INFOTEC, cuyo registro se encuentra actualmente en trámite.
 *
 * INFOTEC pone a su disposición la herramienta SemanticWebBuilder a través de su licenciamiento abierto al público ('open source'),
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
 * dirección electrónica: http://www.semanticwebbuilder.org.mx
 */
package org.semanticwb.rdf;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Interface that defines methods to implement different extended Graphs. An extended Graph is a Graph
 * with additional methods for specific scenarios.
 * @author javier.solis.g
 */
public interface GraphExt {
    /**
     * Counts triples in graph using a {@link TripleMatch}.
     * @param tm    {@link TripleMatch} object.
     * @param stype Class type inferred from URI
     * @return Number of triples matching criteria.
     */
    long count(TripleMatch tm, String stype);
    
    /**
     * Builds and executes an SQL-like triple query.
     * @param tm        {@link TripleMatch} object
     * @param stype     Class type inferred from URI
     * @param limit     Number of results to return
     * @param offset    Record offset
     * @param sortBy    Sort string one of "subj", "prop", "obj", "sort", "timems", "stype"
     * @return Triple iterator
     */
     ExtendedIterator<Triple> find(TripleMatch tm, String stype, Long limit, Long offset, String sortBy);
}
