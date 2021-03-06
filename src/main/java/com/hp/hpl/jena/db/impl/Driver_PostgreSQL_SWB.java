/**
 * SemanticWebBuilder es una plataforma para el desarrollo de portales y aplicaciones de integración,
 * colaboración y conocimiento, que gracias al uso de tecnología semántica puede generar contextos de
 * información alrededor de algún tema de interés o bien integrar información y aplicaciones de diferentes
 * fuentes, donde a la información se le asigna un significado, de forma que pueda ser interpretada y
 * procesada por personas y/o sistemas, es una creación original del Fondo de Información y Documentación
 * para la Industria INFOTEC, cuyo registro se encuentra actualmente en trámite.
 * <p>
 * INFOTEC pone a su disposición la herramienta SemanticWebBuilder a través de su licenciamiento abierto al público ('open source'),
 * en virtud del cual, usted podrá usarlo en las mismas condiciones con que INFOTEC lo ha diseñado y puesto a su disposición;
 * aprender de él; distribuirlo a terceros; acceder a su código fuente y modificarlo, y combinarlo o enlazarlo con otro software,
 * todo ello de conformidad con los términos y condiciones de la LICENCIA ABIERTA AL PÚBLICO que otorga INFOTEC para la utilización
 * del SemanticWebBuilder 4.0.
 * <p>
 * INFOTEC no otorga garantía sobre SemanticWebBuilder, de ninguna especie y naturaleza, ni implícita ni explícita,
 * siendo usted completamente responsable de la utilización que le dé y asumiendo la totalidad de los riesgos que puedan derivar
 * de la misma.
 * <p>
 * Si usted tiene cualquier duda o comentario sobre SemanticWebBuilder, INFOTEC pone a su disposición la siguiente
 * dirección electrónica: http://www.semanticwebbuilder.org.mx
 **/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.db.IDBConnection;
import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;

/**
 * Jena {@link Driver_MySQL} extension for SWB.
 *
 * @author javier.solis
 */
public class Driver_PostgreSQL_SWB extends Driver_MySQL {

    /** The log. */
    private static Logger LOG = SWBUtils.getLogger(Driver_PostgreSQL_SWB.class);

    /**
     * Instantiates a new driver_ postgre sq l_ swb.
     */
    public Driver_PostgreSQL_SWB() {
        super();
    }

    @Override
    protected String IDtoString(int dbID, String table, String RDBcode) {
        RDBLongObject lobj = IDtoLongObject(dbID, table);
        if (lobj == null) {
            LOG.error("Invalid Long literal ID: " + dbID);
            return "0::";
        } else {
            return super.IDtoString(dbID, table, RDBcode);
        }
    }

    @Override
    public void setConnection(IDBConnection dbcon) {
        m_dbcon = dbcon;
        try {
            if (SWBPlatform.createInstance().getStatementsCache()) {
                m_sql = new SQLCache_SWB(SQL_FILE, null, dbcon, ID_SQL_TYPE);
            } else {
                m_sql = new SQLCache_SWBBase(SQL_FILE, null, dbcon, ID_SQL_TYPE);
            }
        } catch (Exception e) {
            LOG.error("Unable to set connection for Driver:", e);
        }
    }
}
