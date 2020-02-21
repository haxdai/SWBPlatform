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

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.db.impl.*;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;
import org.semanticwb.base.db.DBConnectionPool;

import java.util.Iterator;

/**
 * Implementation of a relational database TripleStore.
 * @author jei
 */
public class RDBStore implements AbstractStore {
    private static Logger LOG = SWBUtils.getLogger(RDBStore.class);
    private ModelMaker maker;
    private IDBConnection conn;

    public void init() {
        DBConnectionPool pool = SWBUtils.DB.getDefaultPool();
        String mDb = SWBUtils.DB.getDatabaseType(pool.getName());

        // Create database connection
        conn = new DBConnection(SWBUtils.DB.getDefaultPool().newAutoConnection(), mDb);

        IRDBDriver driver = null;
        switch (mDb) {
            case SWBUtils.DB.DBTYPE_MySQL:
                driver = new Driver_MySQL_SWB();
                break;
            case SWBUtils.DB.DBTYPE_Derby:
                driver = new Driver_Derby_SWB();
                break;
            case SWBUtils.DB.DBTYPE_HSQLDB:
                driver = new Driver_HSQLDB_SWB();
                break;
            case SWBUtils.DB.DBTYPE_MsSQL:
                driver = new Driver_MsSQL_SWB();
                break;
            case SWBUtils.DB.DBTYPE_MsSQL2008:
                driver = new Driver_MsSQL2008_SWB();
                break;
            case SWBUtils.DB.DBTYPE_Oracle:
                driver = new Driver_Oracle_SWB();
                break;
            case SWBUtils.DB.DBTYPE_PostgreSQL:
                driver = new Driver_PostgreSQL_SWB();
                break;
        }

        if (null != driver) {
            driver.setConnection(conn);
            conn.setDriver(driver);
            conn.getDriver().setTableNamePrefix("swb_");
            conn.getDriver().setDoDuplicateCheck(false);

            maker = ModelFactory.createModelRDBMaker(conn);
        } else {
            LOG.error("Error initializing RDBStore: Invalid driver");
        }
    }

    public void removeModel(String name) {
        maker.removeModel(name);
    }

    public Model loadModel(String name) {
        Model ret = maker.openModel(name);
        ((ModelRDB) (ret)).setDoFastpath(false);
        ((ModelRDB) (ret)).setQueryOnlyAsserted(true);
        return ret;
    }

    public Iterator<String> listModelNames() {
        return maker.listModels();
    }

    public void close() {
        maker.close();
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                LOG.error(e);
            }
        }
    }

    public Dataset getDataset(String name) {
        return null;
    }

    public Model getModel(String name) {
        if (maker.hasModel(name)) {
            return loadModel(name);
        }
        return null;
    }
}