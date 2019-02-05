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
 * dirección electrónica:
 * http://www.semanticwebbuilder.org.mx
 **/

package com.hp.hpl.jena.db.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.base.db.AutoConnection;
import org.semanticwb.platform.ThreadObserver;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.shared.JenaException;

/**
 * The Class SQLCache_SWB.
 *
 * @author Javier Solís {javier.solis}
 */
public class SQLCache_SWB extends SQLCache_SWBBase implements ThreadObserver {

    /** The log. */
    private static final Logger log = SWBUtils.getLogger(SQLCache_SWB.class);

    ConcurrentHashMap<Long, Connection> thcon = new ConcurrentHashMap<>();

    /**
     * Constructor. Creates a new cache sql statements for interfacing to
     * a specific database.
     *
     * @param sqlFile the name of the file of sql statements to load, this is
     * loaded from the classpath.
     * @param defaultOps Properties table which provides the default
     * sql statements, any definitions of a given operation in the loaded file
     * will override the default.
     * @param connection the jdbc connection to the database itself
     * @param idType the sql string to use for id types (substitutes for $id in files)
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public SQLCache_SWB(String sqlFile, Properties defaultOps, IDBConnection connection, String idType) throws IOException {
        super(sqlFile, defaultOps, connection, idType);
        setCachePreparedStatements(false);
        SWBPlatform.createInstance().registerThreadObserver(this);
    }

    private Connection getThreadConnection() {
        long th = Thread.currentThread().getId();
        Connection con = thcon.get(th);
        if (con == null) {
            String name = Thread.currentThread().getName();
            if (!name.equals(SWBPlatform.getThreadName())) {
                con = SWBUtils.DB.getDefaultConnection(name);
            } else {
                con = SWBUtils.DB.getDefaultPool().newAutoConnection();
            }
            thcon.put(th, con);
        }
        return con;
    }

    private void returnThreadConnection(Connection con) {
        //Nada que hacer
    }

    private void closeThreadConnection() {
        long th = Thread.currentThread().getId();
        Connection con = thcon.remove(th);
        try {
            if (con != null) {
                if (con instanceof AutoConnection) {
                    log.error("Autoconnection is closing in thread:" + Thread.currentThread().getName());
                }
                con.close();
            }
        } catch (Exception e) {
            log.error(e);
        }
    }


    /**
     * Gets the stack trace.
     *
     * @return the stack trace
     */
    String getStackTrace() {
        StringBuilder ret = new StringBuilder();
        StackTraceElement [] ste = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : ste) {
            ret.append(element.toString() + "\n");
        }
        return ret.toString();
    }

    /**
     * Prepare a SQL statement for the given statement string.
     *
     * <p>Only works for single statements, not compound statements.
     *
     * @param sql the sql
     * @return a prepared SQL statement appropriate for the JDBC connection
     * used when this SQLCache was constructed or null if there is no such
     * connection.
     * @throws SQLException the sQL exception
     */
    private PreparedStatement doPrepareSQLStatement(String sql) throws SQLException {
        if (m_connection == null) {
            return null;
        }

        Connection con = getThreadConnection();
        return con.prepareStatement(sql);
    }

    /**
     * Return a prepared SQL statement for the given statement string.
     * The statement should either be closed after use.
     *
     * <p>Only works for single statements, not compound statements.
     *
     * @param sql the sql
     * @return a prepared SQL statement appropriate for the JDBC connection
     * used when this SQLCache was constructed or null if there is no such
     * connection.
     * @throws SQLException the sQL exception
     */
    @Override
    public PreparedStatement prepareSQLStatement(String sql) throws SQLException {
        if (m_connection == null) {
            return null;
        }
        return doPrepareSQLStatement(sql);
    }

    /**
     * Gets the prepared sql statement.
     *
     * @param opname the opname
     * @param attr the attr
     * @return the prepared sql statement
     * @throws SQLException the sQL exception
     */
    @Override
    public PreparedStatement getPreparedSQLStatement(String opname, String[] attr) throws SQLException {
        //TODO extended calling format or statement format to support different result sets and conconcurrency modes.

        PreparedStatement ps = null;
        if (m_connection == null || opname == null) {
            return null;
        }
        int attrCnt = (attr == null) ? 0 : attr.length;
        String aop = opname;
        if (attrCnt > 0) {
            aop = concatOpName(aop, attr[0]);
        }
        if (attrCnt > 1) {
            aop = concatOpName(aop, attr[1]);
        }
        if (attrCnt > 2) {
            aop = concatOpName(aop, attr[2]);
        }
        if (attrCnt > 3) {
            throw new JenaException("Too many arguments");
        }

        List<PreparedStatement> psl = m_preparedStatements.get(aop);
        // OVERRIDE: added proper PreparedStatement removal.
        if (psl != null && !psl.isEmpty()) {
            ps = psl.remove(0);
            try {
                ps.clearParameters();
            } catch (SQLException e) {
                ps.close();
            }
        }
        if (ps == null) {
            String sql = getSQLStatement(opname, attr);
            if (sql == null) {
                throw new SQLException("No SQL defined for operation: " + opname);
            }
            if (psl == null && CACHE_PREPARED_STATEMENTS) {
                psl = new LinkedList<>();
                m_preparedStatements.put(aop, psl);
            }
            ps = doPrepareSQLStatement(sql);
        }
        if (CACHE_PREPARED_STATEMENTS) {
            m_cachedStmtInUse.put(ps, psl);
        }
        return ps;
    }

    /**
     * Run a group of sql statements - normally used for db formating and clean up.
     * All statements are executed even if one raises an error then the error is
     * reported at the end.
     *
     * Attribute version -- substitute the ${a} attribute macro
     * for the current attribute
     *
     * @param opname the opname
     * @param attr the attr
     * @throws SQLException the sQL exception
     */
    @Override
    public void runSQLGroup(String opname, String[] attr) throws SQLException {
        String op;
        Connection con = getThreadConnection();
        java.sql.Statement sql = con.createStatement();
        Iterator<String> ops = getSQLStatementGroup(opname).iterator();

        try {
            int attrCnt = attr == null ? 0 : attr.length;
            if (attrCnt > 6) {
                throw new RDFRDBException("Too many parameters");
            }
            while (ops.hasNext()) {
                op = ops.next();
                if (attrCnt > 0) {
                    op = substitute(op, "${a}", attr[0]);
                }
                if (attrCnt > 1) {
                    op = substitute(op, "${b}", attr[1]);
                }
                if (attrCnt > 2) {
                    op = substitute(op, "${c}", attr[2]);
                }
                if (attrCnt > 3) {
                    op = substitute(op, "${d}", attr[3]);
                }
                if (attrCnt > 4) {
                    op = substitute(op, "${e}", attr[4]);
                }
                if (attrCnt > 5) {
                    op = substitute(op, "${f}", attr[5]);
                }
                sql.execute(op);
            }
        } finally {
            try {
                sql.close();
                returnThreadConnection(con);
            } catch (SQLException e2) {
                log.error(e2);
            }
        }
    }

    /**
     * Return a prepared statement to the statement pool for reuse by
     * another caller. Any close problems logged rather than raising exception
     * so that iterator close() operations can be silent so that they can meet
     * the ClosableIterator signature.
     *
     * @param ps the ps
     */
    @Override
    public void returnPreparedSQLStatement(PreparedStatement ps) {
        try {
            Connection con = ps.getConnection();
            ps.close();
            if (con != getConnection()) {
                returnThreadConnection(con);
            }
        } catch (SQLException e) {
            log.warn("Problem discarded prepared statement", e);
        }
    }

    /**
     * Execute the given statement, return null if the statement appears to be
     * just an update or return an iterator for the result set if the statement appears
     * to be a query.
     *
     * @param ps the ps
     * @param opname the opname
     * @param iterator the iterator
     * @return the result set iterator
     * @throws SQLException the sQL exception
     */
    @Override
    protected ResultSetIterator executeSQL(PreparedStatement ps, String opname, ResultSetIterator iterator) throws SQLException {
        try {
            if (ps.execute()) {
                java.sql.ResultSet rs = ps.getResultSet();
                iterator.reset(rs, ps, this, opname);
                return iterator;
            } else {
                returnPreparedSQLStatement(ps);
                return null;
            }
        } catch (RuntimeException e) {
            returnPreparedSQLStatement(ps);
            throw e;
        }
    }

    public void notifyEnd() {
        closeThreadConnection();
    }
}
