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

package com.hp.hpl.jena.sdb.sql;

import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.SDBConstants;
import com.hp.hpl.jena.shared.Command;
import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

/**
 * An SDBConnection is the abstraction of the link between client application and the database.
 * There can be many Store's per connection.
 */
public class SDBConnection_SWB extends SDBConnection {

    /** The log. */
    private static Logger LOG = SWBUtils.getLogger(SDBConnection_SWB.class);

    /** The gen. */
    private static Generator gen = Gensym.create("connection-");

    /** The sql connection. */
    private Connection sqlConnection = null;

    /** The transaction handler. */
    TransactionHandler transactionHandler = null;

    /** The label. */
    String label = gen.next();

    /** The jdbc url. */
    String jdbcURL = "unset";

    // Defaults 
    /** The log sql exceptions. */
    public static boolean logSQLExceptions = true;

    /** The log sql statements. */
    public static boolean logSQLStatements = false;

    /** The log sql queries. */
    public static boolean logSQLQueries = false;

    /** The this log sql exceptions. */
    private boolean thisLogSQLExceptions = logSQLExceptions;

    /** The this log sql statements. */
    private boolean thisLogSQLStatements = logSQLStatements;

    /** The this log sql queries. */
    private boolean thisLogSQLQueries = logSQLQueries;

    /**
     * Instantiates a new sDB connection_swb.
     */
    public SDBConnection_SWB() {
        this(SWBUtils.DB.getDefaultConnection());
    }

    /**
     * Instantiates a new sDB connection_swb.
     *
     * @param poolname the poolname
     */
    public SDBConnection_SWB(String poolname) {
        this(SWBUtils.DB.getConnection(poolname));
    }

    /**
     * Instantiates a new sDB connection_swb.
     *
     * @param con the con
     */
    public SDBConnection_SWB(Connection con) {
        super(con);
        sqlConnection = con;
    }

    /**
     * None.
     *
     * @return the sDB connection
     */
    public static SDBConnection none() {
        return new SDBConnection(JDBC.jdbcNone, null, null);
    }

    /**
     * Checks for sql connection.
     *
     * @return true, if successful
     */
    @Override
    public boolean hasSQLConnection() {
        return true;
    }

    /**
     * Gets the transaction handler.
     *
     * @return the transaction handler
     */
    @Override
    public TransactionHandler getTransactionHandler() {
        return transactionHandler;
    }

    /**
     * Exec query.
     *
     * @param sqlString the sql string
     * @return the result set jdbc
     * @throws SQLException the sQL exception
     */
    @Override
    public ResultSetJDBC execQuery(String sqlString) throws SQLException {
        return execQuery(sqlString, SDBConstants.jdbcFetchSizeOff);
    }

    /**
     * Exec query.
     *
     * @param sqlString the sql string
     * @param fetchSize the fetch size
     * @return the result set jdbc
     * @throws SQLException the sQL exception
     */
    @Override
    public ResultSetJDBC execQuery(String sqlString, int fetchSize) throws SQLException {
        if (loggingSQLStatements() || loggingSQLQueries())
            writeLog("execQuery", sqlString);

        Connection conn = getPoolConnection();

        try (Statement s = conn.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)) {
            if (fetchSize != SDBConstants.jdbcFetchSizeOff) {
                s.setFetchSize(fetchSize);
            }
            return new ResultSetJDBC(s, s.executeQuery(sqlString));
        } catch (SQLException ex) {
            exception("execQuery", ex, sqlString);
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        }
    }

    /**
     * Execute in transaction.
     *
     * @param c the c
     * @return the object
     */
    @Override
    public Object executeInTransaction(Command c) {
        return getTransactionHandler().executeInTransaction(c);
    }

    /**
     * Execute sql.
     *
     * @param c the c
     * @return the object
     */
    @Override
    public Object executeSQL(final SQLCommand c) {
        Connection conn = getPoolConnection();
        try {
            return c.execute(conn);
        } catch (SQLException ex) {
            exception("SQL", ex);
            throw new SDBExceptionSQL(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOG.error(e);
                }
            }
        }
    }

    /**
     * Exec update.
     *
     * @param sqlString the sql string
     * @return the int
     * @throws SQLException the sQL exception
     */
    @Override
    public int execUpdate(String sqlString) throws SQLException {
        if (loggingSQLStatements()) {
            writeLog("execUpdate", sqlString);
        }

        Connection conn = getPoolConnection();
        try (Statement s = conn.createStatement()) {
            return s.executeUpdate(sqlString);
        } catch (SQLException ex) {
            exception("execUpdate", ex, sqlString);
            throw ex;
        }
    }

    /**
     * Execute a statement, return the result set if there was one, else null.
     *
     * @param sqlString the sql string
     * @return the result set jdbc
     * @throws SQLException the sQL exception
     */
    @Override
    public ResultSetJDBC exec(String sqlString) throws SQLException {
        if (loggingSQLStatements()) {
            writeLog("exec", sqlString);
        }

        Connection conn = getPoolConnection();
        try (Statement s = conn.createStatement()) {
            if (s.execute(sqlString)) {
                return new ResultSetJDBC(s, s.getResultSet());
            }
            return null;
        } catch (SQLException ex) {
            exception("exec", ex, sqlString);
            throw ex;
        }
    }

    /**
     * Execute a statement, return the result set if there was one, else null.
     *
     * @param sqlString the sql string
     * @return the result set jdbc
     */
    @Override
    public ResultSetJDBC execSilent(String sqlString) {
        if (loggingSQLStatements()) {
            writeLog("execSilent", sqlString);
        }

        Connection conn = getPoolConnection();
        try (Statement s = conn.createStatement()) {
            if (s.execute(sqlString)) {
                return new ResultSetJDBC(s, s.getResultSet());
            }
            return null;
        } catch (SQLException ex) {
            return null;
        }
    }

    /**
     * Prepare a statement *.
     *
     * @param sqlString the sql string
     * @return the prepared statement
     * @throws SQLException the sQL exception
     */
    @Override
    public PreparedStatement prepareStatement(String sqlString) throws SQLException {
        if (loggingSQLStatements()) {
            writeLog("prepareStatement", sqlString);
        }

        Connection conn = getPoolConnection();
        try {
            return conn.prepareStatement(sqlString);
        } catch (SQLException ex) {
            exception("prepareStatement", ex, sqlString);
            throw ex;
        }
    }

    /**
     * Close a prepared statement *.
     *
     * @param ps the ps
     * @throws SQLException the sQL exception
     */
    @Override
    public void closePreparedStatement(PreparedStatement ps) throws SQLException {
        if (loggingSQLStatements()) {
            writeLog("closePrepareStatement", ps.toString());
        }

        if (ps == null) {
            return;
        }

        try {
            ps.close();
        } catch (SQLException ex) {
            exception("closePrepareStatement", ex, ps.toString());
            throw ex;
        }
    }

    /**
     * Get the names of the application tables.
     *
     * @return the table names
     */
    @Override
    public List<String> getTableNames() {
        Connection conn = getPoolConnection();
        List<String> list = TableUtils.getTableNames(conn);
        try {
            conn.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return list;
    }

    /**
     * Gets the sql connection.
     *
     * @return the sql connection
     */
    @Override
    public Connection getSqlConnection() {
        // Potential pool point.
        return sqlConnection;
    }

    /**
     * Gets the pool connection.
     *
     * @return the pool connection
     */
    public Connection getPoolConnection() {
        return SWBUtils.DB.getDefaultConnection();
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        Connection connection = getSqlConnection();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            LOG.warn("Problems closing SQL connection", ex);
        }
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return getLabel();
    }

    /**
     * Logging sql exceptions.
     *
     * @return true, if successful
     */
    @Override
    public boolean loggingSQLExceptions() {
        return thisLogSQLExceptions;
    }

    /**
     * Sets the log sql exceptions.
     *
     * @param thisLogSQLExceptions the new log sql exceptions
     */
    @Override
    public void setLogSQLExceptions(boolean thisLogSQLExceptions) {
        this.thisLogSQLExceptions = thisLogSQLExceptions;
    }

    /**
     * Logging sql queries.
     *
     * @return true, if successful
     */
    @Override
    public boolean loggingSQLQueries() {
        return thisLogSQLQueries;
    }

    /**
     * Sets the log sql queries.
     *
     * @param thisLogSQLQueries the new log sql queries
     */
    @Override
    public void setLogSQLQueries(boolean thisLogSQLQueries) {
        this.thisLogSQLQueries = thisLogSQLQueries;
    }

    /**
     * Logging sql statements.
     *
     * @return true, if successful
     */
    @Override
    public boolean loggingSQLStatements() {
        return thisLogSQLStatements;
    }

    /**
     * Sets the log sql statements.
     *
     * @param thisLogSQLStatements the new log sql statements
     */
    @Override
    public void setLogSQLStatements(boolean thisLogSQLStatements) {
        this.thisLogSQLStatements = thisLogSQLStatements;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     *
     * @param label the new label
     */
    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the jdbc url.
     *
     * @return the jdbc url
     */
    @Override
    public String getJdbcURL() {
        return jdbcURL;
    }

    /**
     * Sets the jdbc url.
     *
     * @param jdbcURL the new jdbc url
     */
    @Override
    public void setJdbcURL(String jdbcURL) {
        this.jdbcURL = jdbcURL;
    }

    /**
     * Exception.
     *
     * @param who the who
     * @param ex the ex
     * @param sqlString the sql string
     */
    private void exception(String who, SQLException ex, String sqlString) {
        if (this.loggingSQLExceptions()) {
            LOG.warn(who + ": SQLException\n" + ex.getMessage() + "\n" + sqlString + "\n");
        }
    }

    /**
     * Exception.
     *
     * @param who the who
     * @param ex the ex
     */
    private void exception(String who, SQLException ex) {
        if (this.loggingSQLExceptions()) {
            LOG.warn(who + ": SQLException\n" + ex.getMessage());
        }
    }

    /**
     * Write log.
     *
     * @param who the who
     * @param sqlString the sql string
     */
    private void writeLog(String who, String sqlString) {
        LOG.info(who + "\n\n" + sqlString + "\n");
    }
}
