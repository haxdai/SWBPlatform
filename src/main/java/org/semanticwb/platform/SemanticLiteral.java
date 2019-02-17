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
 *  http://www.semanticwebbuilder.org.mx
 */
package org.semanticwb.platform;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * The Class SemanticLiteral.
 *
 * @author Jei
 */
public class SemanticLiteral {

    /**
     * The LOG.
     */
    private static Logger log = SWBUtils.getLogger(SemanticLiteral.class);

    /**
     * The literal.
     */
    Literal literal;

    /**
     * The lang.
     */
    String lang = null;

    /**
     * The obj.
     */
    Object obj = null;

    /**
     * Instantiates a new semantic literal.
     *
     * @param stmt the stmt
     */
    public SemanticLiteral(Statement stmt) {
        literal = stmt.getLiteral();
    }

    /**
     * Instantiates a new semantic literal.
     *
     * @param literal the literal
     */
    public SemanticLiteral(Literal literal) {
        this.literal = literal;
    }

    /**
     * Instantiates a new semantic literal.
     *
     * @param value the value
     */
    public SemanticLiteral(Object value) {
        obj = value;
    }

    /**
     * Instantiates a new semantic literal.
     *
     * @param value the value
     * @param lang  the lang
     */
    public SemanticLiteral(Object value, String lang) {
        obj = value;
        if (lang != null && lang.isEmpty()) {
            this.lang = null;
        }
        this.lang = lang;
    }

    /**
     * Value of.
     *
     * @param prop  the prop
     * @param value the value
     * @return the semantic literal
     */
    public static SemanticLiteral valueOf(SemanticProperty prop, String value) {
        SemanticLiteral ret = null;
        if (value != null) {
            if (prop.isString()) {
                ret = new SemanticLiteral(value);
            } else if (prop.isBoolean()) {
                ret = new SemanticLiteral(Boolean.valueOf(value));
            } else if (prop.isDouble()) {
                ret = new SemanticLiteral(Double.valueOf(value));
            } else if (prop.isFloat()) {
                ret = new SemanticLiteral(Float.valueOf(value));
            } else if (prop.isInt()) {
                ret = new SemanticLiteral(Integer.valueOf(value));
            } else if (prop.isLong()) {
                ret = new SemanticLiteral(Long.valueOf(value));
            } else if (prop.isDecimal()) {
                ret = new SemanticLiteral(new BigDecimal(value));
            } else if (prop.isDate()) {
                try {
                    ret = new SemanticLiteral(SWBUtils.TEXT.iso8601DateParse(value));
                } catch (Exception e) {
                    log.error(e);
                }
            } else if (prop.isDateTime()) {
                try {
                    ret = new SemanticLiteral(new Timestamp(SWBUtils.TEXT.iso8601DateParse(value).getTime()));
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        return ret;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public Object getValue() {
        return literal != null ? literal.getValue() : obj;
    }

    /**
     * Gets the boolean.
     *
     * @return the boolean
     */
    public boolean getBoolean() {
        if (literal != null) {
            //Validar 
            if (literal.getDatatypeURI() != null) {
                if (literal.getDatatypeURI().endsWith("#boolean")) {
                    return literal.getBoolean();
                } else if (literal.getDatatypeURI().endsWith("#integer")) {
                    return literal.getInt() == 1;
                }
            }
        } else if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj) == 1;
        }
        return false;
    }

    /**
     * Gets the string.
     *
     * @return the string
     */
    public String getString() {
        if (literal != null) {
            return literal.getString();
        } else if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    /**
     * Gets the byte.
     *
     * @return the byte
     */
    public byte getByte() {
        if (literal != null) {
            String lf = literal.getLexicalForm();
            if (lf != null && lf.length() > 0) {
                return literal.getByte();
            }
        } else if (obj instanceof Byte) {
            return (Byte) obj;
        }

        return 0;
    }

    /**
     * Gets the char.
     *
     * @return the char
     */
    public char getChar() {
        if (literal != null) {
            return literal.getChar();
        } else if (obj instanceof Character) {
            return (Character) obj;
        }
        return 0;
    }

    /**
     * Gets the double.
     *
     * @return the double
     */
    public double getDouble() {
        if (literal != null) {
            try {
                String lf = literal.getLexicalForm();
                if (lf != null && lf.length() > 0) {
                    return literal.getDouble();
                }
            } catch (NumberFormatException e) {
                log.error("Error parsing double value...", e);
            }
        } else if (obj instanceof Double) {
            return (Double) obj;
        }

        return 0D;
    }

    /**
     * Gets the float.
     *
     * @return the float
     */
    public float getFloat() {
        if (literal != null) {
            try {
                String lf = literal.getLexicalForm();
                if (lf != null && lf.length() > 0) {
                    return literal.getFloat();
                }
            } catch (NumberFormatException e) {
                log.error("Error parsing float value...", e);
            }
        } else if (obj instanceof Float) {
            return (Float) obj;
        }

        return 0F;
    }

    /**
     * Gets the int.
     *
     * @return the int
     */
    public int getInt() {
        if (literal != null) {
            try {
                String lf = literal.getLexicalForm();
                if (lf != null && lf.length() > 0) {
                    return literal.getInt();
                }
            } catch (NumberFormatException e) {
                log.error("Error parsing int value...", e);
            }
        } else if (obj instanceof Long) {
            return ((Long) obj).intValue();
        } else if (obj instanceof Integer) {
            return (Integer) obj;
        }

        return 0;
    }

    /**
     * Gets the short.
     *
     * @return the short
     */
    public short getShort() {
        if (literal != null) {
            String lf = literal.getLexicalForm();
            if (lf != null && lf.length() > 0) {
                return literal.getShort();
            }
        } else if (obj instanceof Short) {
            return (Short) obj;
        }
        return 0;
    }

    /**
     * Gets the language.
     *
     * @return the language
     */
    public String getLanguage() {
        String ret;
        if (literal != null) {
            ret = literal.getLanguage();
            if (ret != null && ret.length() == 0) {
                ret = null;
            }
        } else {
            ret = lang;
        }
        return ret;
    }

    /**
     * Gets the long.
     *
     * @return the long
     */
    public long getLong() {
        if (literal != null) {
            String lf = literal.getLexicalForm();
            if (lf != null && lf.length() > 0) {
                return literal.getLong();
            }
        } else if (obj instanceof Long) {
            return (Long) obj;
        }

        return 0L;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate() {
        Object theobj = getValue();

        if (theobj instanceof Date) {
            return (Date) theobj;
        } else if (theobj != null) {
            try {
                String aux = theobj.toString();
                if (aux != null && aux.length() > 0) {
                    return new Date(SWBUtils.TEXT.iso8601DateParse(aux).getTime());
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
        return null;
    }

    /**
     * Gets the date time.
     *
     * @return the date time
     */
    public Timestamp getDateTime() {
        Object theobj = getValue();

        if (theobj instanceof Timestamp) {
            return (Timestamp) theobj;
        } else {
            try {
                if (theobj instanceof String) {
                    String aux = (String) theobj;
                    if (aux.length() > 0) {
                        return new Timestamp(SWBUtils.TEXT.iso8601DateParse(aux).getTime());
                    }
                } else if (theobj instanceof BaseDatatype.TypedValue) {
                    BaseDatatype.TypedValue tv = (BaseDatatype.TypedValue) theobj;
                    String aux = tv.lexicalValue;
                    if (aux != null && aux.length() > 0) {
                        return new Timestamp(SWBUtils.TEXT.iso8601DateParse(aux).getTime());
                    }
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
        return null;
    }
}
