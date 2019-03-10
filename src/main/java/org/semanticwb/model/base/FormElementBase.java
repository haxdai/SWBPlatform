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
package org.semanticwb.model.base;

import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.FormElement;
import org.semanticwb.model.FormElementURL;
import org.semanticwb.model.FormValidateException;
import org.semanticwb.model.GenericObject;
import org.semanticwb.platform.SemanticModel;
import org.semanticwb.platform.SemanticObject;
import org.semanticwb.platform.SemanticProperty;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

/**
 * Clase base para todos los elementos de forma utilizados en SemanticWebBuilder.
 *
 * @author Javier Solís
 */
public class FormElementBase extends GenericObjectBase implements FormElement, GenericObject {

    /**
     * The log.
     */
    private static final Logger LOG = SWBUtils.getLogger(FormElementBase.class);

    /**
     * The attributes.
     */
    protected HashMap<String, String> attributes;

    /**
     * The model.
     */
    private SemanticModel model = null;

    /**
     * The filter html tags.
     */
    private boolean filterHTMLTags = true;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private Object formMgr = null;

    /**
     * Instantiates a new form element base.
     *
     * @param obj the obj
     */
    public FormElementBase(SemanticObject obj) {
        super(obj);
        attributes = new HashMap<>();
    }

    @Override
    public void validate(HttpServletRequest request, SemanticObject obj, SemanticProperty prop, String propName) throws FormValidateException {

    }

    @Override
    public void process(HttpServletRequest request, SemanticObject obj, SemanticProperty prop, String propName) {
        boolean needDP = !(propName.indexOf('.') > 0);
        if (needDP && prop.getDisplayProperty() == null) {
            return;
        }

        if (prop.isDataTypeProperty()) {
            String value = request.getParameter(propName);
            String old = obj.getProperty(prop);
            if (prop.isBoolean()) {
                if (value != null && (value.equals("true") || value.equals("on")) && (old == null || old.equals("false"))) {
                    obj.setBooleanProperty(prop, true);
                } else if ((value == null || value.equals("false")) && old != null && old.equals("true")) {
                    obj.setBooleanProperty(prop, false);
                }
            } else if (prop.isDateTime()) {
                value = request.getParameter(propName + "_date");
                String tvalue = request.getParameter(propName + "_time");

                if (value != null && tvalue != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                    try {
                        Date dt = sdf.parse(value + tvalue);
                        Timestamp ts = new Timestamp(dt.getTime());
                        obj.setDateTimeProperty(prop, ts);
                    } catch (ParseException ex) {
                        LOG.error(ex);
                    }
                }
            } else {
                if (value != null) {
                    if (value.length() > 0 && !value.equals(old)) {
                        if (prop.isFloat()) {
                            obj.setFloatProperty(prop, Float.parseFloat(value));
                        }

                        if (prop.isDouble()) {
                            obj.setDoubleProperty(prop, Double.parseDouble(value));
                        }

                        if (prop.isInt() || prop.isShort() || prop.isByte()) {
                            obj.setIntProperty(prop, Integer.parseInt(value));
                        }

                        if (prop.isLong()) {
                            obj.setLongProperty(prop, Long.parseLong(value));
                        }

                        try {
                            if (prop.isDate()) {
                                obj.setDateProperty(prop, format.parse(value));
                            }
                        } catch (Exception e) {
                            LOG.error(e);
                        }

                        if (prop.isString()) {
                            if (isFilterHTMLTags()) {
                                obj.setProperty(prop, SWBUtils.XML.replaceXMLChars(value));
                            } else {
                                obj.setProperty(prop, value);
                            }
                        }
                    } else if (value.length() == 0 && old != null) {
                        obj.removeProperty(prop);
                    }
                }
            }
        } else if (prop.isObjectProperty()) {
            String uri = request.getParameter(propName);

            if (uri != null) {
                if (propName.startsWith("has")) {
                    //TODO:
                } else {
                    String ouri = "";
                    SemanticObject old = obj.getObjectProperty(prop);
                    if (old != null) {
                        ouri = old.getURI();
                    }

                    if (!uri.equals(ouri)) {
                        SemanticObject aux = null;
                        if (uri.length() > 0) {
                            aux = SWBPlatform.getSemanticMgr().getOntology().getSemanticObject(uri);
                        }
                        if (aux != null) {
                            obj.setObjectProperty(prop, aux);
                        } else {
                            obj.removeProperty(prop);
                        }
                    }
                }
            }
        }
    }

    public String renderElement(HttpServletRequest request, SemanticObject obj, SemanticProperty prop, String propName, String type, String mode, String lang) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAttribute(String name, String value) {
        if (value != null) {
            attributes.put(name, value);
        } else {
            attributes.remove(name);
        }
    }

    /**
     * Gets the attributes.
     *
     * @return the attributes
     */
    public String getAttributes() {
        StringBuilder ret = new StringBuilder();
        Iterator<Entry<String, String>> it = attributes.entrySet().iterator();

        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            ret.append(entry.getKey())
                    .append("=").append("\"")
                    .append(entry.getValue()).append("\"");

            if (it.hasNext()) {
                ret.append(" ");
            }
        }
        return ret.toString();
    }

    public FormElementURL getRenderURL(SemanticObject obj, SemanticProperty prop, String type, String mode, String lang) {
        return new FormElementURL(this, obj, prop, FormElementURL.URLTYPE_RENDER, type, mode, lang);
    }

    public FormElementURL getValidateURL(SemanticObject obj, SemanticProperty prop) {
        return new FormElementURL(this, obj, prop, FormElementURL.URLTYPE_VALIDATE, null, null, null);
    }

    public FormElementURL getProcessURL(SemanticObject obj, SemanticProperty prop) {
        return new FormElementURL(this, obj, prop, FormElementURL.URLTYPE_PROCESS, null, null, null);
    }

    public String getLocaleString(String key, String lang) {
        try {
            return SWBUtils.TEXT.getLocaleString(this.getClass().getName(), key,
                    new Locale(lang), this.getClass().getClassLoader());
        } catch (Exception e) {
            LOG.error(e);
        }
        return null;
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    public SemanticModel getModel() {
        return model;
    }

    /**
     * Sets the model.
     *
     * @param model the model to set
     */
    public void setModel(SemanticModel model) {
        this.model = model;
    }

    /**
     * Checks if is filter html tags.
     *
     * @return the filterHTMLTags
     */
    public boolean isFilterHTMLTags() {
        return filterHTMLTags;
    }

    /**
     * Sets the filter html tags.
     *
     * @param filterHTMLTags the filterHTMLTags to set
     */
    public void setFilterHTMLTags(boolean filterHTMLTags) {
        this.filterHTMLTags = filterHTMLTags;
    }

    public String renderLabel(HttpServletRequest request, SemanticObject obj, SemanticProperty prop, String propName, String type, String mode, String lang, String label) {
        String ret;
        boolean required = prop.isRequired();

        if (label == null) {
            label = prop.getDisplayName(lang);
        }

        String help = prop.getComment(lang);

        String reqtxt = "";
        if (!mode.equals("filter") && required) {
            reqtxt = "<em>*</em>";
        }

        ret = "<label for=\"" + propName + "\">" + label + reqtxt + "</label>";
        if (null != help && !help.isEmpty()) {
            ret += " <span class=\"fa fa-question-circle\" title=\"" + help + "\"></span>";
        }
        return ret;
    }

    public String renderLabel(HttpServletRequest request, SemanticObject obj, SemanticProperty prop, String propName, String type, String mode, String lang) {
        return renderLabel(request, obj, prop, prop.getName(), type, mode, lang, null);
    }

    public String renderLabel(HttpServletRequest request, SemanticObject obj, SemanticProperty prop, String type, String mode, String lang) {
        return renderLabel(request, obj, prop, prop.getName(), type, mode, lang);
    }

    public String renderElement(HttpServletRequest request, SemanticObject obj, SemanticProperty prop, String type, String mode, String lang) {
        return renderElement(request, obj, prop, prop.getName(), type, mode, lang);
    }

    public void validate(HttpServletRequest request, SemanticObject obj, SemanticProperty prop) throws FormValidateException {
        validate(request, obj, prop, prop.getName());
    }

    public void process(HttpServletRequest request, SemanticObject obj, SemanticProperty prop) {
        process(request, obj, prop, prop.getName());
    }

    @Override
    public Object getFormMgr() {
        return formMgr;
    }

    @Override
    public void setFormMgr(Object formMgr) {
        this.formMgr = formMgr;
    }
}
