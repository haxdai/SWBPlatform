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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;
import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of an in memory cached Graph.
 * @author javier.solis
 */
public class GraphCached extends GraphMemFaster implements GraphListener {

    /**
     * The base model.
     */
    private Graph base;

    /**
     * Constructor. Creates a new {@link GraphCached} using <code>base</code>.
     * @param base the base model.
     */
    public GraphCached(Graph base) {
        super();
        super.getBulkUpdateHandler().add(base);
        this.base = base;
        getEventManager().register(this);
    }

    /**
     * Notifies a triple addition.
     *
     * @param g the graph
     * @param t the triple
     */
    @Override
    public void notifyAddTriple(Graph g, Triple t) {
        base.add(t);
    }

    /**
     * Notifies a triple array addition.
     *
     * @param g the graph
     * @param triples the triples array
     */
    @Override
    public void notifyAddArray(Graph g, Triple[] triples) {
        base.getBulkUpdateHandler().add(triples);
    }

    /**
     * Notifies a triple list addition.
     *
     * @param g the graph
     * @param triples the triple list
     */
    @Override
    public void notifyAddList(Graph g, List<Triple> triples) {
        base.getBulkUpdateHandler().add(triples);
    }

    /**
     * Notifies a triple iterator addition.
     *
     * @param g the graph
     * @param it the triple iterator
     */
    @Override
    public void notifyAddIterator(Graph g, Iterator<Triple> it) {
        base.getBulkUpdateHandler().add(it);
    }

    /**
     * Notifies a graph addition.
     *
     * @param g the graph
     * @param added the added graph
     */
    @Override
    public void notifyAddGraph(Graph g, Graph added) {
        base.getBulkUpdateHandler().add(added);
    }

    /**
     * Notifies a triple deletion.
     *
     * @param g the graph
     * @param t the triple
     */
    @Override
    public void notifyDeleteTriple(Graph g, Triple t) {
        base.delete(t);
    }

    /**
     * Notifies a triple list deletion.
     *
     * @param g the graph
     * @param triples the triple list
     */
    @Override
    public void notifyDeleteList(Graph g, List<Triple> triples) {
        base.getBulkUpdateHandler().delete(triples);
    }

    /**
     * Notifies a triple array deletion.
     *
     * @param g the graph
     * @param triples the triple
     */
    @Override
    public void notifyDeleteArray(Graph g, Triple[] triples) {
        base.getBulkUpdateHandler().delete(triples);
    }

    /**
     * Notifies a triple iterator deletion.
     *
     * @param g the graph
     * @param it the triple iterator
     */
    @Override
    public void notifyDeleteIterator(Graph g, Iterator<Triple> it) {
        base.getBulkUpdateHandler().delete(it);
    }

    /**
     * Notifies a graph deletion.
     *
     * @param g the graph
     * @param removed the graph to remove
     */
    @Override
    public void notifyDeleteGraph(Graph g, Graph removed) {
        base.getBulkUpdateHandler().delete(removed);
    }

    /**
     * Notifies a generic event.
     *
     * @param source the source of the event
     * @param value  the changed value
     */
    @Override
    public void notifyEvent(Graph source, Object value) {
    }

    /**
     * Gets the prefix mapping.
     *
     * @return the prefix mapping
     */
    @Override
    public PrefixMapping getPrefixMapping() {
        return base.getPrefixMapping();
    }

    public Graph getGraphBase() {
        return base;
    }

    @Override
    public TransactionHandler getTransactionHandler() {
        TransactionHandler ret = base.getTransactionHandler();
        if (ret == null) {
            ret = super.getTransactionHandler();
        }
        return ret;
    }

    @Override
    public BulkUpdateHandler getBulkUpdateHandler() {
        BulkUpdateHandler ret = base.getBulkUpdateHandler();
        if (ret == null) {
            ret = super.getBulkUpdateHandler();
        }
        return ret;
    }
}
