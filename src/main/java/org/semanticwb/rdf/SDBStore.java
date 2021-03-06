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

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;
import org.semanticwb.base.db.DBConnectionPool;

import java.util.Iterator;
import java.util.List;

/**
 * SQL based Triple Store implementation.
 * @author jei
 */
public class SDBStore implements AbstractStore {
    private static final Logger LOG = SWBUtils.getLogger(SDBStore.class);

    /**
     * The Dataset
     */
    private Dataset set;

    public void init() {
        DBConnectionPool pool = SWBUtils.DB.getDefaultPool();
        String databaseType = SWBUtils.DB.getDatabaseType(pool.getName());

        try {
            StoreDesc sd = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.fetch(databaseType));
            SDBConnection con = new SDBConnection(SWBUtils.DB.getDefaultPool().newAutoConnection());

            Store store = SDBFactory.connectStore(con, sd);
            //Revisar si las tablas existen
            List list = store.getConnection().getTableNames();

            if (!(list.contains("nodes") || list.contains("NODES") || list.contains("Nodes"))
                    && !(list.contains("triples") || list.contains("TRIPLES") || list.contains("Triples"))
                    && !(list.contains("quads") || list.contains("QUADS") || list.contains("Quads"))) //MAPS74 Oracle maneja los nombres en MAYUSCULAS, MySQL usa Capitalizados
            {
                LOG.event("Formating Database Tables...");
                store.getTableFormatter().create();
            }

            set = SDBFactory.connectDataset(store);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    public void removeModel(String name) {
        Model model = loadModel(name);
        if (model != null) {
            model.removeAll();
        }
    }

    public Model loadModel(String name) {
        return set.getNamedModel(name);
    }

    public Iterator<String> listModelNames() {
        return set.listNames();
    }

    public Model getModel(String name) {
        Iterator<String> it = listModelNames();
        while (it.hasNext()) {
            String mname = it.next();
            if (mname.equals(name)) {
                return loadModel(name);
            }
        }
        return null;
    }

    public void close() {
        set.close();
    }

    public Dataset getDataset(String defaultName) {
        return set;
    }

}
