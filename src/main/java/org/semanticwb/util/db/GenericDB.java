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
 * dirección electrónica:
 *  http://www.semanticwebbuilder.org.mx.mx
 */
package org.semanticwb.util.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.resolver.DialectFactory;
import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The Class GenericDB.
 * 
 * @author Juan Fernández {juan.fernandez}
 */
public class GenericDB {

    /** The log. */
    static Logger log=SWBUtils.getLogger(GenericDB.class);
    
    /** The Constant DB_MYSQL. */
    static final String DB_MYSQL = "MYSQL";
    
    /** The Constant DB_ORACLE. */
    static final String DB_ORACLE = "ORACLE";
    
    /** The Constant DB_INFORMIX. */
    static final String DB_INFORMIX = "INFORMIX";
    
    /** The Constant DB_SQLSERVER. */
    static final String DB_SQLSERVER = "SQLSERVER";
    
    /** The Constant DB_POSTGRESSQL. */
    static final String DB_POSTGRESSQL = "POSTGRESSQL";
    
    /** The Constant DB_HSSQL. */
    static final String DB_HSSQL = "HSQL";
    
    /** The Constant DB_POINTBASE. */
    static final String DB_POINTBASE = "POINTBASE";
    
    /** The Constant DB_SYBASE. */
    static final String DB_SYBASE = "SYBASE"; //
    
    /** The Constant DB_DB2. */
    static final String DB_DB2 = "DB2";
    
    static final String DB_DERBY = "APACHE DERBY"; //Apache Derby
    
    static final String DB_VIRTUOSO = "OPENLINK VIRTUOSO VDBMS";
    
    /** The Constant SQL_CHAR. */
    static final String SQL_CHAR = "CHAR";
    
    /** The Constant SQL_VARCHAR. */
    static final String SQL_VARCHAR = "VARCHAR";

    /** The Constant SQL_NUMERIC. */
    static final String SQL_NUMERIC = "NUMERIC";

    /** The Constant SQL_BIT. */
    static final String SQL_BIT = "BIT";
    
    /** The Constant SQL_BLOB. */
    static final String SQL_BLOB = "BLOB";

    /** The Constant SQL_SMALLINT. */
    static final String SQL_SMALLINT = "SMALLINT";
    
    /** The Constant SQL_INTEGER. */
    static final String SQL_INTEGER = "INTEGER";
    
    /** The Constant SQL_BIGINT. */
    static final String SQL_BIGINT = "BIGINT";

    /** The Constant SQL_FLOAT. */
    static final String SQL_FLOAT = "FLOAT";
    
    /** The Constant SQL_DOUBLE. */
    static final String SQL_DOUBLE = "DOUBLE";

    /** The Constant SQL_DATE. */
    static final String SQL_DATE = "DATE";
    
    /** The Constant SQL_TIME. */
    static final String SQL_TIME = "TIME";
    
    /** The Constant SQL_TIMESTAMP. */
    static final String SQL_TIMESTAMP = "TIMESTAMP";

    /** The Constant SQL_CLOB. */
    static final String SQL_CLOB = "CLOB";
    
    /** The Constant PK. */
    private static final String PK = "PRIMARYKEY_INI";
    
    /** The Constant COLUMN. */
    private static final String COLUMN = "COLUMN";
    
    /** The Constant INDTYPE. */
    private static final String INDTYPE = "INDEX_TYPE";
    
    /** The Constant INDORDER. */
    private static final String INDORDER = "INDEX_ORDER";
    
    /** The Constant PRIMARYKEY. */
    private static final String PRIMARYKEY = "#COLUMN_NAME#";
    
    /** The Constant FK. */
    private static final String FK = "ALTER TABLE #TABLE_NAME# ADD CONSTRAINT #CNAME# ";
    
    /** The Constant FOREIGNKEY. */
    private static final String FOREIGNKEY = "FOREIGN KEY ( ";
    
    /** The Constant FK_COLUMN. */
    private static final String FK_COLUMN = "#COLUMN_NAME#";
    
    /** The Constant FK_REFERENCE. */
    private static final String FK_REFERENCE = "REFERENCES #TABLE_NAME# ( ";
    
    /** The Constant INDEX_INI. */
    private static final String INDEX_INI = "CREATE #INDEX_TYPE# INDEX #INDEX_NAME# ON #TABLE_NAME# ( ";
    
    /** The Constant INDEX. */
    private static final String INDEX = "#COLUMN_NAME# #ORDER#";
    
    /** The hm dialect. */
    private HashMap<String, String> hmDialect=null;
    
    /** The hm sql type. */
    private HashMap<String, String> hmSQLType=null;
    
    /** The hm syntax. */
    private HashMap<String, HashMap<String, String>> hmSyntax=null;

    /**
     * Instantiates a new generic db.
     */
    public GenericDB() {
        if (null == hmDialect) {
            loadDialects();
        }
        if (null == hmSQLType) {
            loadSQLTypes();
        }
        if (null == hmSyntax) {
            loadSyntax();
        }
    }

    /**
     * Gets the sQL script.
     * 
     * @param XML the xML
     * @param dbname the dbname
     * @return the sQL script
     * @throws SQLException the sQL exception
     */
    public String getSQLScript(String XML, String dbname) throws SQLException {
        String retSQL = null;
        Document dom = null;

        if (null != XML) {
            dom = SWBUtils.XML.xmlToDom(XML);
        }
        dbname = dbname.toLowerCase();
        if (dbname.lastIndexOf("informix") > -1) {
            dbname = DB_INFORMIX;
        } 
        else if (dbname.lastIndexOf("microsoft sql server") > -1) {
            dbname = DB_SQLSERVER;
        }
        else if (dbname.lastIndexOf("mysql") > -1) {
            dbname = DB_MYSQL;
        }
        else if (dbname.lastIndexOf("adaptive server enterprise") > -1) {
            dbname = DB_SYBASE;
        }
        else if (dbname.lastIndexOf("postgresql") > -1) {
            dbname = DB_POSTGRESSQL;
        }
        else if (dbname.lastIndexOf("oracle") > -1) {
            dbname = DB_ORACLE;
        }
        else if (dbname.lastIndexOf("hsql") > -1) {
            dbname = DB_HSSQL;
        }
        else if (dbname.lastIndexOf("pointbase") > -1) {
            dbname = DB_POINTBASE;
        }
        else if (dbname.lastIndexOf("db2") > -1) {
            dbname = DB_DB2;
        }
        else if (dbname.lastIndexOf("apache derby") > -1) {
            dbname = DB_DERBY;
        }
        if (null != dom) {
            try 
            {
                retSQL = getSchema(XML, dbname);       
            } 
            catch (Exception e) {
                log.error("Error al generar el SCRIPT SQL. GenericDB.getSQLScript()",e);
            }
        }
        return retSQL;
    }

    /**
     * Execute sql script.
     * 
     * @param XML the xML
     * @param dbname the dbname
     * @param poolname the poolname
     * @return true, if successful
     * @throws SQLException the sQL exception
     */
    public boolean executeSQLScript(String XML, String dbname, String poolname) throws SQLException {

        Connection conn = null;
        Statement st = null;
        StringTokenizer sto = null;
        String sqlScript = null;
        String query = null;
        try
            {
                sqlScript = getSQLScript(XML, dbname); 
                if(poolname==null)
                {
                    conn = SWBUtils.DB.getDefaultConnection("GenericDB.executeSQLScript()");
                }
                else
                {
                    conn = SWBUtils.DB.getConnection(poolname,"GenericDB.executeSQLScript()");
                }
                
                st = conn.createStatement();
                if(sqlScript!=null)
                {
                    sto=new StringTokenizer(sqlScript,";");
                    while(sto.hasMoreTokens())
                    {
                        query = sto.nextToken();
                        try
                        {
                            if(query.trim().length()>0)
                                st.executeUpdate(query);
                        }
                        catch(Exception e)
                        {
                            log.error("Error on method executeSQLScript() of GenericDB trying to execute next code : "+ query,e);
                            throw e;
                        }
                    }
                }
                if(st != null) st.close();
                if(conn != null) conn.close();
                
            }
            catch(Exception e)
            {
                log.error("Error on method executeSQLScript() on GenericDB util.",e);
            }
            finally
            {
                try{if(st != null) st.close();st = null;}catch(Exception e1){}
                try{if(conn != null) conn.close();conn = null;}catch(Exception e2){}
            }
        
        return true;
    }
    
    /**
     * Gets the column syntax.
     * 
     * @param dbtype the dbtype
     * @param attr the attr
     * @return the column syntax
     */
    public String getColumnSyntax(String dbtype, String attr) 
    {
        if(null==hmSyntax)
        {
            loadSyntax();
        }
        return hmSyntax.get(dbtype.toUpperCase()).get(attr);
    }

    /**
     * Gets the sQL type.
     * 
     * @param coltype the coltype
     * @return the sQL type
     */
    public int getSQLType(String coltype) {
        if(null==hmSQLType)
        {
            loadSQLTypes();
        }
        return hmSQLType.get(coltype) != null ? Integer.parseInt((String) hmSQLType.get(coltype)) : -1;
    }

    /**
     * Gets the dB dialect.
     * 
     * @param DBName the dB name
     * @return the dB dialect
     */
    public String getDBDialect(String DBName) {
        if(null==hmDialect)
        {
            loadDialects();
        }
        return (String) hmDialect.get(DBName.toUpperCase());
    }
    
    /**
     * Validate xml.
     * 
     * @param xml the xml
     * @return true, if successful
     */
    private boolean validateXML(String xml)
    {
        boolean bOk=false;
        String schema=null;
        try {  
            schema = SWBUtils.IO.getFileFromPath(SWBUtils.getApplicationPath()+"/WEB-INF/xsds/GenericDB.xsd");
        } 
        catch(Exception e) { return bOk; }
        if (schema != null && xml !=null) bOk=SWBUtils.XML.xmlVerifier(schema, xml);
        return bOk;
    }
    
    /**
     * Gets the schema.
     * 
     * @param strXML the str xml
     * @param DBName the dB name
     * @return the schema
     * @throws Exception the exception
     */
    public String getSchema(String strXML, String DBName) throws Exception {
        StringBuilder strBuff = new StringBuilder();
        String LFCR = " ";
        
        if (DBName == null) {
            return null;
        }
        final String eleFin = "); ";
        final String eleFinTblMysql = ") ENGINE="+SWBPlatform.getEnv("swb/mysqlJenaEngine", "INNODB") +" DEFAULT CHARSET=utf8 ;";
        Document dom = SWBUtils.XML.xmlToDom(strXML);
        
        if (dom != null) {
            Dialect dialect = DialectFactory.constructDialect(getDBDialect(DBName));
            Element root = dom.getDocumentElement();
            NodeList tbEle = root.getElementsByTagName("table");
            if (tbEle != null && tbEle.getLength() > 0) {
                for (int i = 0; i < tbEle.getLength(); i++) {
                    Element ele = (Element) tbEle.item(i);
                    if (ele.getNodeName().equals("table")) {
                        strBuff.append(dialect.getCreateTableString() + " " + ele.getAttribute("name") + " ( "+LFCR);
                        NodeList colEle = ele.getElementsByTagName("column");
                        if (colEle != null && colEle.getLength() > 0) {
                            String colSyntax = getColumnSyntax(DBName, COLUMN);
                            String tmpCol = "";
                            String comilla = "";
                            boolean haveCLOB = false;
                            for (int j = 0; j < colEle.getLength(); j++) {
                                Element col = (Element) colEle.item(j);
                                tmpCol = colSyntax;
                                comilla = "";
                                String tmpVal = col.getAttribute("id");

                                if (tmpVal != null) {
                                    tmpCol = tmpCol.replaceAll("#COLUMN_NAME#", tmpVal);
                                }

                                if (col.getAttribute("signed") != null && col.getAttribute("signed").toLowerCase().equals("false")) {
                                    tmpCol = tmpCol.replaceAll("#SIGNED#", "UNSIGNED");
                                } else {
                                    tmpCol = tmpCol.replaceAll("#SIGNED#", "");
                                }

                                tmpVal = col.getAttribute("type").trim().toUpperCase();

                                String tmpSize = col.getAttribute("size");  // para el tipo de datos correspondiente dependiendo el tamaño.
                                if (tmpVal != null && tmpSize != null && tmpSize.trim().length() > 0)
                                {
                                    int codigo = 0;
                                    String tipocol = "";
                                    if(DBName.equals(DB_INFORMIX))
                                    {
                                         if(tmpVal.equals(SQL_CLOB)&&!haveCLOB)
                                        {
                                            tipocol = "clob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);

                                        } else if(tmpVal.equals(SQL_CLOB)&&haveCLOB)
                                        {
                                            tipocol = "text in table";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);

                                        } else if(tmpVal.equals(SQL_BLOB))
                                        {
                                            tipocol = "blob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);

                                        }
                                        else 
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(tmpVal), Integer.parseInt(col.getAttribute("size")), 0, 0));

                                        }
                                    }
                                    else if(DBName.equals(DB_SYBASE))
                                    {
                                        if(tmpVal.equals(SQL_CLOB)&&haveCLOB)
                                        {
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(SQL_VARCHAR), Integer.parseInt(col.getAttribute("size")), 0, 0));
                                        }
                                        else
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(tmpVal), Integer.parseInt(col.getAttribute("size")), 0, 0));
                                        }
                                    }
                                    else if(DBName.equals(DB_POINTBASE))
                                    {
                                         if(tmpVal.equals(SQL_CLOB)&&!haveCLOB)
                                        {
                                            tipocol = "clob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        } else if(tmpVal.equals(SQL_CLOB)&&haveCLOB)
                                        {
                                            codigo = getSQLType(SQL_VARCHAR);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(SQL_VARCHAR), 65535, 0, 0));
                                        } else if(tmpVal.equals(SQL_BLOB))
                                        {
                                            tipocol = "blob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }
                                        else
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(tmpVal), Integer.parseInt(col.getAttribute("size")), 0, 0));
                                        }
                                    }else if(DBName.equals(DB_DERBY))
                                    {
                                         if(tmpVal.equals(SQL_CLOB)&&!haveCLOB)
                                        {
                                            tipocol = "clob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        } else if(tmpVal.equals(SQL_CLOB)&&haveCLOB)
                                        {
                                            codigo = getSQLType(SQL_VARCHAR);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(SQL_VARCHAR), 32672, 0, 0));
                                        } else if(tmpVal.equals(SQL_BLOB))
                                        {
                                            tipocol = "blob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }
                                        else
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(tmpVal), Integer.parseInt(col.getAttribute("size")), 0, 0));
                                        }
                                    }
                                    else if(DBName.equals(DB_ORACLE))
                                    {
                                        if(tmpVal.equals(SQL_CLOB)&&haveCLOB)
                                        {
                                            codigo = getSQLType(SQL_VARCHAR);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(SQL_VARCHAR), 4000, 0, 0));
                                        } 
                                        else
                                        {
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(tmpVal), Integer.parseInt(col.getAttribute("size")), 0, 0));
                                        }
                                    }
                                    else
                                    {
                                        tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(tmpVal), Integer.parseInt(col.getAttribute("size")), 0, 0));
                                    }
                                    if (tmpVal.equals(SQL_VARCHAR) || tmpVal.equals(SQL_CHAR)) {
                                        comilla = "'";
                                    }
                                } else if (tmpVal != null && ((tmpSize != null && tmpSize.trim().length() == 0) || (col.getAttribute("size") == null))) {                                    
                                    int codigo = 0;
                                    String tipocol = "";
                                    if(DBName.equals(DB_INFORMIX))
                                    {
                                        if(tmpVal.equals(SQL_CHAR))
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(tmpVal), 1, 0, 0));
                                        } 
                                        else if(tmpVal.equals(SQL_CLOB)&&!haveCLOB)
                                        {
                                            tipocol = "clob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        } else if(tmpVal.equals(SQL_CLOB)&&haveCLOB)
                                        {
                                            tipocol = "text in table";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }else if(tmpVal.equals(SQL_BLOB))
                                        {
                                            tipocol = "blob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }
                                        else
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }
                                    }
                                    else if(DBName.equals(DB_SYBASE))
                                    {
                                        if(tmpVal.equals(SQL_CLOB)&&haveCLOB)
                                        {
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(SQL_VARCHAR), 4000, 0, 0));
                                        }
                                        else
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }
                                    }
                                    else if(DBName.equals(DB_DB2))
                                    {
                                        if(tmpVal.equals(SQL_VARCHAR))
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(tmpVal), 4000, 0, 0));
                                        }
                                        else if(tmpVal.equals(SQL_CLOB))
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(tmpVal), 65535, 0, 0));
                                        } else if(tmpVal.equals(SQL_BLOB))
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType(tmpVal), 65535, 0, 0));
                                        }
                                        else
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }
                                    }
                                    else if(DBName.equals(DB_POINTBASE))
                                    {
                                         if(tmpVal.equals(SQL_CLOB)&&!haveCLOB)
                                        {
                                            tipocol = "clob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        } else if(tmpVal.equals(SQL_CLOB)&&haveCLOB)
                                        {
                                            codigo = getSQLType(SQL_VARCHAR);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType("VARCHAR"), 65535, 0, 0));
                                        } else if(tmpVal.equals(SQL_BLOB))
                                        {
                                            tipocol = "blob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }
                                        else
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }
                                    }
                                    else if(DBName.equals(DB_DERBY))
                                    {
                                         if(tmpVal.equals(SQL_CLOB)&&!haveCLOB)
                                        {
                                            tipocol = "clob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        } else if(tmpVal.equals(SQL_CLOB)&&haveCLOB)
                                        {
                                            codigo = getSQLType(SQL_VARCHAR);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType("VARCHAR"), 32672, 0, 0));
                                        } else if(tmpVal.equals(SQL_BLOB))
                                        {
                                            tipocol = "blob";
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }
                                        else
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }
                                    }
                                    else if(DBName.equals(DB_ORACLE))
                                    {
                                        if(tmpVal.equals(SQL_CLOB)&&haveCLOB)
                                        {
                                            codigo = getSQLType(SQL_VARCHAR);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", dialect.getTypeName(getSQLType("VARCHAR"), 4000, 0, 0));
                                        } 
                                        else
                                        {
                                            codigo = getSQLType(tmpVal);
                                            tipocol = dialect.getTypeName(codigo);
                                            tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                        }
                                    }
                                    else
                                    {
                                        codigo = getSQLType(tmpVal);
                                        tipocol = dialect.getTypeName(codigo);
                                        tmpCol = tmpCol.replaceAll("#TYPE#", tipocol);
                                    }
                                        
                                    if (tmpVal.equals(SQL_VARCHAR) || tmpVal.equals(SQL_CHAR)) {
                                        comilla = "'";
                                    }
                                }
                                if (col.getAttribute("acceptNulls") != null && col.getAttribute("acceptNulls").toLowerCase().equals("false")) {
                                    tmpCol = tmpCol.replaceAll("#NULL#", "NOT NULL");
                                } else {
                                    tmpCol = tmpCol.replaceAll("#NULL#", "");
                                }

                                if (col.getAttribute("default") != null && col.getAttribute("default").length()>0 ) {
                                    tmpCol = tmpCol.replaceAll("#DEFAULT#", "DEFAULT " + comilla + col.getAttribute("default") + comilla);
                                } else {
                                    tmpCol = tmpCol.replaceAll("#DEFAULT#", "");
                                }

                                strBuff.append(tmpCol);

                                if ((j+1) < colEle.getLength()) {
                                    strBuff.append(","+LFCR);
                                }
                                if(tmpVal.equals(SQL_CLOB)&&(DBName.equals(DB_ORACLE)||DBName.equals(DB_SYBASE)||DBName.equals(DB_INFORMIX)||DBName.equals(DB_POINTBASE)))
                                {
                                    haveCLOB=true;
                                }
                            } 
                        }
                        // Cerrando tabla
                        if (DBName.equals(DB_MYSQL)){
                            strBuff.append(eleFinTblMysql + LFCR+LFCR);
                        } else {
                            strBuff.append(eleFin + LFCR+LFCR);
                        }

                    }
                }
            }

            // PRIMARY KEY
            NodeList tbPK = root.getElementsByTagName("primarykey");
            if (tbPK != null && tbPK.getLength() > 0) {
                for (int i = 0; i < tbPK.getLength(); i++) {
                    Element ele = (Element) tbPK.item(i);
                    if (ele.getNodeName().equals("primarykey")) {
                        String tbname = ele.getAttribute("table");
                        String colINISyntax = getColumnSyntax(DBName, PK);
                        if (null != tbname) {
                            colINISyntax = colINISyntax.replaceAll("#TABLE_NAME#", tbname);
                            if (colINISyntax.indexOf("#CNAME#") > -1) {
                                colINISyntax = colINISyntax.replaceAll("#CNAME#", tbname + "_pk" + i);
                            }
                            strBuff.append(colINISyntax+LFCR);

                            NodeList colEle = ele.getElementsByTagName("colpk");
                            if (colEle != null && colEle.getLength() > 0) {
                                String colSyntax = PRIMARYKEY;
                                String tmpCol = "";

                                for (int j = 0; j < colEle.getLength(); j++) {
                                    Element col = (Element) colEle.item(j);
                                    tmpCol = colSyntax;
                                    String tmpVal = col.getAttribute("id");

                                    if (tmpVal != null) {
                                        tmpCol = tmpCol.replaceAll("#COLUMN_NAME#", tmpVal);
                                    }
                                    
                                    strBuff.append(tmpCol);
                                    if ((j+1) < colEle.getLength()) {
                                        strBuff.append(","+LFCR);
                                    }
                                }
                            }
                        }
                        // Cerrando alter primary key
                        strBuff.append(eleFin+LFCR+LFCR);
                    }
                }
            }
            
            // INDICES
            NodeList tbIND = root.getElementsByTagName("index");
            if (tbIND != null && tbIND.getLength() > 0) {
                for (int i = 0; i < tbIND.getLength(); i++) {
                    Element ele = (Element) tbIND.item(i);
                    if (ele.getNodeName().equals("index")) {
                        String tbname = ele.getAttribute("table");
                        String indType = ele.getAttribute("type");
                        String indName = ele.getAttribute("name");
                        String colINISyntax = INDEX_INI;
                        if (null != tbname && indType!=null) {
                            colINISyntax = colINISyntax.replaceAll("#TABLE_NAME#", tbname);
                            colINISyntax = colINISyntax.replaceAll("#INDEX_NAME#", indName);
                            String tmp = getColumnSyntax(DBName, INDTYPE);
                            if (indType!=null && indType.trim().length()>0&&tmp.indexOf(indType)>-1) {
                                
                                if(null!=tmp && tmp.trim().length()>0)
                                {
                                    colINISyntax = colINISyntax.replaceAll("#INDEX_TYPE#", indType);
                                }
                                else
                                {
                                    colINISyntax = colINISyntax.replaceAll("#INDEX_TYPE#", "");
                                }
                            }
                            else
                            {
                                colINISyntax = colINISyntax.replaceAll("#INDEX_TYPE#", "");
                            }
                            strBuff.append(colINISyntax);
                            
                            // revisando las columnas
                            NodeList colEle = ele.getElementsByTagName("colindex");
                            if (colEle != null && colEle.getLength() > 0) {
                                String colSyntax = INDEX;
                                String tmpCol = "";

                                for (int j = 0; j < colEle.getLength(); j++) {
                                    Element col = (Element) colEle.item(j);
                                    tmpCol = colSyntax;
                                    String tmpVal = col.getAttribute("id");

                                    if (tmpVal != null && tmpVal.trim().length()>0) {
                                        tmpCol = tmpCol.replaceAll("#COLUMN_NAME#", tmpVal);
                                    }
                                    
                                    tmpVal = col.getAttribute("order");
                                    tmp = getColumnSyntax(DBName, INDORDER);  // REVISANDO ORDENAMIENTO VALIDO
                                    if (tmpVal != null && tmpVal.trim().length()>0 && tmp.indexOf(tmpVal.trim().toUpperCase())>0) {
                                        
                                        if(null!=tmp && tmp.trim().length()>0 )
                                        {
                                            tmpCol = tmpCol.replaceAll("#ORDER#", tmpVal);
                                        }
                                        else
                                        {
                                            tmpCol = tmpCol.replaceAll("#ORDER#", "");
                                        }
                                    }
                                    else
                                        {
                                            tmpCol = tmpCol.replaceAll("#ORDER#", "");
                                        }
                                    
                                    strBuff.append(tmpCol);
                                    if ((j+1) < colEle.getLength()) {
                                        strBuff.append(","+LFCR);
                                    }
                                }
                            }
                        }
                        // Cerrando indice
                        strBuff.append(eleFin+LFCR+LFCR);
                    }
                }
            }
            // FOREIGN KEY  ---- FK = "ALTER TABLE #TABLE_NAME# ADD CONSTRAINT #CNAME# ";
            NodeList tbFK = root.getElementsByTagName("foreignkey");
            if (tbFK != null && tbFK.getLength() > 0) {
                for (int i = 0; i < tbFK.getLength(); i++) {
                    Element ele = (Element) tbFK.item(i);
                    if (ele.getNodeName().equals("foreignkey")) {
                        String tbname = ele.getAttribute("table");
                        String colINISyntax = FK;
                        if (null != tbname) {
                            colINISyntax = colINISyntax.replaceAll("#TABLE_NAME#", tbname);
                            colINISyntax = colINISyntax.replaceAll("#CNAME#", tbname + "_fk" + i);
                            strBuff.append(colINISyntax+LFCR);

                            NodeList columnsEle = ele.getElementsByTagName("columns");
                            if(columnsEle !=null && columnsEle.getLength() > 0)
                            {
                                strBuff.append(FOREIGNKEY+LFCR);
                                Element eleColumns = (Element) columnsEle.item(0);
                                NodeList colEle = eleColumns.getElementsByTagName("colpk");
                                if (colEle != null && colEle.getLength() > 0) {
                                    String colSyntax = FK_COLUMN;
                                    String tmpCol = "";
                                    for (int j = 0; j < colEle.getLength(); j++) {
                                        Element col = (Element) colEle.item(j);
                                        tmpCol = colSyntax;
                                        String tmpVal = col.getAttribute("id");

                                        if (tmpVal != null) {
                                            tmpCol = tmpCol.replaceAll("#COLUMN_NAME#", tmpVal);
                                        }
                                        else
                                        {
                                            tmpCol = tmpCol.replaceAll("#COLUMN_NAME#", "");
                                        }

                                        strBuff.append(tmpCol);
                                        if ((j+1) < colEle.getLength()) {
                                            strBuff.append(","+LFCR);
                                        }
                                    }
                                    strBuff.append(") ");
                                }
                            }
                            
                            NodeList refEle = ele.getElementsByTagName("reference");
                            if(refEle !=null && refEle.getLength() > 0)
                            {
                                for (int j = 0; j < refEle.getLength(); j++) 
                                {
                                    Element eleRef = (Element) refEle.item(i);
                                    tbname = eleRef.getAttribute("table");
                                    String refINISyntax = FK_REFERENCE;
                                    if (null != tbname) 
                                    {
                                        refINISyntax = refINISyntax.replaceAll("#TABLE_NAME#", tbname);
                                        strBuff.append(refINISyntax+LFCR);
                                        NodeList colsEle = eleRef.getElementsByTagName("columns");
                                        if(colsEle !=null && colsEle.getLength() > 0)
                                        {
                                                Element eleCols = (Element) colsEle.item(0);
                                                NodeList colEle = eleCols.getElementsByTagName("colpk");
                                                if (colEle != null && colEle.getLength() > 0) 
                                                {
                                                    String colSyntax = FK_COLUMN;
                                                    String tmpCol = "";
                                                    for (int k = 0; k < colEle.getLength(); k++) {
                                                        Element col = (Element) colEle.item(k);
                                                        tmpCol = colSyntax;
                                                        String tmpVal = col.getAttribute("id");
                                                        if (tmpVal != null) 
                                                        {
                                                            tmpCol = tmpCol.replaceAll("#COLUMN_NAME#", tmpVal);
                                                        }
                                                        else
                                                        {
                                                            tmpCol = tmpCol.replaceAll("#COLUMN_NAME#", "");
                                                        }
                                                        strBuff.append(tmpCol);
                                                        if ((k+1) < colEle.getLength()) 
                                                        {
                                                            strBuff.append(","+LFCR);
                                                        }
                                                    }
                                                }
                                           strBuff.append(")");
                                        }
                                    }
                                }
                            }                        
                            // Cerrando alter add foreign key
                            strBuff.append(";"+LFCR+LFCR);
                        }
                    }
                }
            }
        }
        return strBuff.toString();
    }

    /**
     * Gets the dialects.
     * 
     * @return the dialects
     */
    public HashMap<String, String> getDialects()
    {
        if(hmDialect==null)
        {
            loadDialects();
        }
        return hmDialect;
    }
    
    /**
     * Load dialects.
     */
    private void loadDialects()
    {
        hmDialect = new HashMap<>();

        hmDialect.put(DB_MYSQL, "org.hibernate.dialect.MySQLDialect");
        hmDialect.put(DB_ORACLE, "org.hibernate.dialect.Oracle9iDialect");
        hmDialect.put(DB_INFORMIX, "org.hibernate.dialect.InformixDialect");
        hmDialect.put(DB_SQLSERVER, "org.hibernate.dialect.SQLServerDialect");
        hmDialect.put(DB_HSSQL, "org.hibernate.dialect.HSQLDialect");
        hmDialect.put(DB_POINTBASE, "org.hibernate.dialect.PointbaseDialect");
        hmDialect.put(DB_POSTGRESSQL, "org.hibernate.dialect.PostgreSQLDialect");
        hmDialect.put(DB_SYBASE, "org.hibernate.dialect.SybaseDialect");
        hmDialect.put(DB_DB2, "org.hibernate.dialect.DB2Dialect");
        hmDialect.put(DB_DERBY, "org.hibernate.dialect.DerbyDialect");
        hmDialect.put(DB_VIRTUOSO, "org.hibernate.dialect.DerbyDialect");
        
    }
    
    /**
     * Load sql types.
     */
    private void loadSQLTypes()
    {
        hmSQLType = new HashMap<>();

        hmSQLType.put(SQL_BIGINT, Integer.toString(java.sql.Types.BIGINT));
        hmSQLType.put(SQL_BIT, Integer.toString(java.sql.Types.BIT));
        hmSQLType.put(SQL_BLOB, Integer.toString(java.sql.Types.BLOB));
        hmSQLType.put(SQL_CHAR, Integer.toString(java.sql.Types.CHAR));
        hmSQLType.put(SQL_CLOB, Integer.toString(java.sql.Types.CLOB));
        hmSQLType.put(SQL_DATE, Integer.toString(java.sql.Types.DATE));
        hmSQLType.put(SQL_DOUBLE, Integer.toString(java.sql.Types.DOUBLE));
        hmSQLType.put(SQL_FLOAT, Integer.toString(java.sql.Types.FLOAT));
        hmSQLType.put(SQL_INTEGER, Integer.toString(java.sql.Types.INTEGER));
        hmSQLType.put(SQL_NUMERIC, Integer.toString(java.sql.Types.NUMERIC));
        hmSQLType.put(SQL_SMALLINT, Integer.toString(java.sql.Types.SMALLINT));
        hmSQLType.put(SQL_TIME, Integer.toString(java.sql.Types.TIME));
        hmSQLType.put(SQL_TIMESTAMP, Integer.toString(java.sql.Types.TIMESTAMP));
        hmSQLType.put(SQL_VARCHAR, Integer.toString(java.sql.Types.VARCHAR));
    }
    
    /**
     * Load syntax.
     */
    private void loadSyntax() {
        hmSyntax = new HashMap<>();

        HashMap<String, String> hmMySQL = new HashMap<>();
        hmMySQL.put(COLUMN, "#COLUMN_NAME# #TYPE# #SIGNED# #NULL# #DEFAULT#");
        hmMySQL.put(PK, "ALTER TABLE #TABLE_NAME# ADD PRIMARY KEY ( ");
        hmMySQL.put(INDTYPE, "UNIQUE|FULLTEXT|SPATIAL");
        hmMySQL.put(INDORDER, "ASC|DESC");

        HashMap<String, String> hmOracle = new HashMap<>();
        hmOracle.put(COLUMN, "#COLUMN_NAME# #TYPE# #DEFAULT# #NULL#");
        hmOracle.put(PK, "ALTER TABLE #TABLE_NAME# ADD PRIMARY KEY ( ");
        hmOracle.put(INDTYPE, "UNIQUE|BITMAP");
        hmOracle.put(INDORDER, "ASC|DESC");

        HashMap<String, String> hmInformix = new HashMap<>();
        hmInformix.put(COLUMN, "#COLUMN_NAME# #TYPE# #DEFAULT# #NULL#");
        hmInformix.put(PK, "ALTER TABLE #TABLE_NAME# ADD PRIMARY KEY ( ");
        hmInformix.put(INDTYPE, "UNIQUE|CLUSTER");
        hmInformix.put(INDORDER, "ASC|DESC");

        HashMap<String, String> hmSQLSERVER = new HashMap<>();
        hmSQLSERVER.put(COLUMN, "#COLUMN_NAME# #TYPE# #DEFAULT# #NULL#");
        hmSQLSERVER.put(PK, "ALTER TABLE #TABLE_NAME# ADD PRIMARY KEY ( ");
        hmSQLSERVER.put(INDTYPE, "UNIQUE");
        hmSQLSERVER.put(INDORDER, "ASC|DESC");

        HashMap<String, String> hmHSQL = new HashMap<>();
        hmHSQL.put(COLUMN, "#COLUMN_NAME# #TYPE# #DEFAULT# #NULL#");
        hmHSQL.put(PK, "ALTER TABLE #TABLE_NAME# ADD CONSTRAINT #CNAME# PRIMARY KEY ( ");
        hmHSQL.put(INDTYPE, "UNIQUE");
        hmHSQL.put(INDORDER, "DESC");

        HashMap<String, String> hmPOINTBASE = new HashMap<>();
        hmPOINTBASE.put(COLUMN, "#COLUMN_NAME# #TYPE# #DEFAULT# #NULL#");
        hmPOINTBASE.put(PK, "ALTER TABLE #TABLE_NAME# ADD CONSTRAINT #CNAME# PRIMARY KEY ( ");
        hmPOINTBASE.put(INDTYPE, "UNIQUE");
        hmPOINTBASE.put(INDORDER, "ASC|DESC");

        HashMap<String, String> hmPOSTGRESSQL = new HashMap<>();
        hmPOSTGRESSQL.put(COLUMN, "#COLUMN_NAME# #TYPE# #DEFAULT# #NULL#");
        hmPOSTGRESSQL.put(PK, "ALTER TABLE #TABLE_NAME# ADD PRIMARY KEY ( ");
        hmPOSTGRESSQL.put(INDTYPE, "UNIQUE");
        hmPOSTGRESSQL.put(INDORDER, "ASC|DESC");

        HashMap<String, String> hmDB2 = new HashMap<>();
        hmDB2.put(COLUMN, "#COLUMN_NAME# #TYPE# #NULL# #DEFAULT#");
        hmDB2.put(PK, "ALTER TABLE #TABLE_NAME# ADD PRIMARY KEY ( ");
        hmDB2.put(INDTYPE, "UNIQUE");
        hmDB2.put(INDORDER, "ASC|DESC");

        HashMap<String, String> hmSYBASE = new HashMap<>();
        hmSYBASE.put(COLUMN, "#COLUMN_NAME# #TYPE# #NULL# #DEFAULT#");
        hmSYBASE.put(PK, "ALTER TABLE #TABLE_NAME# ADD CONSTRAINT #CNAME# PRIMARY KEY ( ");
        hmSYBASE.put(INDTYPE, "UNIQUE");
        hmSYBASE.put(INDORDER, "ASC|DESC");
        
        HashMap<String, String> hmDERBY = new HashMap<>();
        hmDERBY.put(COLUMN, "#COLUMN_NAME# #TYPE# #DEFAULT# #NULL#");
        hmDERBY.put(PK, "ALTER TABLE #TABLE_NAME# ADD CONSTRAINT #CNAME# PRIMARY KEY ( ");
        hmDERBY.put(INDTYPE, "UNIQUE");
        hmDERBY.put(INDORDER, "ASC|DESC");

        hmSyntax.put("MYSQL", hmMySQL);
        hmSyntax.put("ORACLE", hmOracle);
        hmSyntax.put("INFORMIX", hmInformix);
        hmSyntax.put("SQLSERVER", hmSQLSERVER);
        hmSyntax.put("HSQL", hmHSQL);
        hmSyntax.put("POINTBASE", hmPOINTBASE);
        hmSyntax.put("POSTGRESSQL", hmPOSTGRESSQL);
        hmSyntax.put("SYBASE", hmSYBASE);
        hmSyntax.put(DB_DB2, hmDB2);
        hmSyntax.put(DB_DERBY, hmDERBY); //Apache Derby
        hmSyntax.put(DB_VIRTUOSO, hmDERBY); 

    }
}
