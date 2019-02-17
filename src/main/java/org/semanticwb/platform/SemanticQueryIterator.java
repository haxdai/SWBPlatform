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
package org.semanticwb.platform;

import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;
import org.semanticwb.rdf.RGraph;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator for resources returned from a semantic query as {@link SemanticObject} instances.
 * @author javier.solis.g
 */
public class SemanticQueryIterator<T extends SemanticObject> implements Iterator {
    private static final Logger LOG = SWBUtils.getLogger(SemanticQueryIterator.class);
    private boolean next = false;
    private SemanticObject act = null;
    private SemanticModel model;
    private Connection con;
    private ResultSet rs;
    private RGraph graph;

    /**
     * Constructor. Creates a new {@link SemanticQueryIterator}.
     * @param model Model to get resources from.
     * @param rs ResultSet with query results.
     * @param con TripleStore connection object.
     */
    public SemanticQueryIterator(SemanticModel model, ResultSet rs, Connection con) {
        this.model = model;
        graph = (RGraph) model.getRDFModel().getGraph();
        this.rs = rs;
        this.con = con;

        try {
            next = rs.next();
            if (!next) {
                close();
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }


    @Override
    public boolean hasNext() {
        return next;
    }

    @Override
    public T next() {
        T ret = null;
        try {
            String subj = rs.getString(1);
            String ext = null;
            InputStream sext = rs.getAsciiStream(2);
            if (sext != null) {
                ext = SWBUtils.IO.getStringFromInputStream(sext);
            }

            String uri = graph.decodeSubject(subj, ext).getURI();
            ret = (T) SemanticObject.createSemanticObject(uri);
        } catch (Exception e) {
            LOG.error(e);
        }

        try {
            next = rs.next();
            if (!next) {
                close();
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        if (null == ret) {
            throw new NoSuchElementException();
        }
        return ret;
    }

    @Override
    public void remove() {
        if (act != null) {
            act.remove();
        }
    }

    public void close() {
        try {
            rs.close();
            con.close();
        } catch (Exception e) {
            LOG.error(e);
        }
    }
}
