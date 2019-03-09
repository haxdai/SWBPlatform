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

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.base.util.URLEncoder;

/**
 * Class to encapsulate a property from an ontology model using jena {@link Property} as
 * internal representation.
 *
 * @author Jei
 */
public class SemanticProperty {
    private static final Logger LOG = SWBUtils.getLogger(SemanticProperty.class);

    /**
     * Internal representation of the Property.
     */
    private Property m_prop;

    /**
     * Inverse property related to this Property.
     */
    private SemanticProperty m_inverse;

    /**
     * Flag to set Property as ObjectProperty.
     */
    private Boolean objectProperty = null;

    /**
     * Flag to set Property as DataTypeProperty.
     */
    private Boolean dataTypeProperty = null;

    /**
     * Flag to set property as having inverse property.
     */
    private Boolean hasInverse = null;

    /**
     * Flag to set property as inverse property.
     */
    private boolean inverse = false;

    /**
     * Flag to set property as external.
     */
    private Boolean externalInvocation = null;

    /**
     * Flag to set property as inherit.
     */
    private Boolean inheritProperty = null;

    /**
     * Flag to set property as not observable.
     */
    private Boolean notObservable = null;

    /**
     * Flag to set property as not code generation.
     */
    private Boolean notCodeGeneration = null;

    /**
     * Flag to set property as remove dependency.
     */
    private Boolean removeDependency = null;

    /**
     * Flag to set property as clone dependency.
     */
    private Boolean cloneDependency = null;

    /**
     * Flag to set property as hierarchical relation.
     */
    private Boolean hierarchicalRelation = null;

    private Boolean hasHierarchicalFilterClass = null;
    private SemanticClass hierarchicalFilterClass = null;

    /**
     * The is required.
     */
    private Boolean required = null;

    /**
     * The is used as name.
     */
    private Boolean usedAsName = null;

    /**
     * The is localeable.
     */
    private Boolean localeable = null;

    /**
     * The m_property code name.
     */
    private String codeName = null;

    /**
     * The m_default value.
     */
    private String defaultValue = null;

    /**
     * The display property.
     */
    private SemanticObject displayProperty = null;

    /**
     * The disp property.
     */
    private boolean dispProperty = false;

    /**
     * The cardinality.
     */
    private int cardinality = 0;

    /**
     * The cardinality check.
     */
    private boolean cardinalityCheck = false;

    /**
     * The range check.
     */
    private boolean rangeCheck = false;

    /**
     * The range.
     */
    private Resource range = null;

    /**
     * The restrictions.
     */
    private HashMap<String, ArrayList<SemanticRestriction>> restrictions = null;

    /**
     * The allvalues.
     */
    private HashMap<String, SemanticRestriction> frestrictions = null;

    /**
     * SemanticObserver list for change notifications.
     */
    private List<SemanticObserver> observers;

    /**
     * Constructor. Creates a new instance of {@link SemanticProperty}.
     *
     * @param prop the base Jena {@link Property} object.
     */
    public SemanticProperty(Property prop) {
        this.m_prop = prop;
        if (m_prop instanceof OntProperty && hasInverse()) {
            m_inverse = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(((OntProperty) m_prop).getInverse());
            m_inverse.inverse = true;
            m_inverse.m_inverse = this;
        }
        observers = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Loads property restriction definitions.
     */
    private void loadRestrictions() {
        restrictions = new HashMap<>();
        frestrictions = new HashMap<>();
        Iterator<Restriction> it = ((OntProperty) m_prop).listReferringRestrictions();
        while (it.hasNext()) {
            Restriction restriction = it.next();

            Iterator<OntClass> it2 = restriction.listSubClasses();
            while (it2.hasNext()) {
                OntClass ontClass = it2.next();

                ArrayList<SemanticRestriction> list = restrictions.get(ontClass.getURI());
                if (list == null) {
                    list = new ArrayList<>();
                    restrictions.put(ontClass.getURI(), list);
                }
                list.add(new SemanticRestriction(restriction));
            }
        }
    }

    /**
     * Gets the {@link SemanticObject} related to this property.
     *
     * @return the semantic object
     */
    public SemanticObject getSemanticObject() {
        return SemanticObject.createSemanticObject(getURI());
    }

    /**
     * Gets the internal {@link Property} object.
     *
     * @return the internal Property object.
     */
    public Property getRDFProperty() {
        return m_prop;
    }

    /**
     * Gets the name of this property.
     *
     * @return the property name.
     * @see Property#getLocalName().
     */
    public String getName() {
        return m_prop.getLocalName();
    }

    /**
     * Gets the prefix of this property.
     *
     * @return the property prefix
     */
    public String getPrefix() {
        return m_prop.getModel().getNsURIPrefix(m_prop.getNameSpace());
    }

    /**
     * Gets the property ID (prefix + name).
     *
     * @return the property ID
     */
    public String getPropId() {
        return getPrefix() + ":" + getName();
    }

    /**
     * Gets the string label associated to this property ignoring language.
     *
     * @return the property label.
     */
    public String getLabel() {
        return getLabel(null);
    }

    /**
     * Gets the string label associated to this property matching the given <code>lang</code>.
     *
     * @param lang the language attribute for the desired label.
     * @return the property label.
     */
    public String getLabel(String lang) {
        if (m_prop instanceof OntProperty) {
            return ((OntProperty) m_prop).getLabel(lang);
        }
        return null;
    }

    /**
     * Gets the string comment associated to this property ignoring language.
     *
     * @return the property comment.
     */
    public String getComment() {
        return getComment(null);
    }

    /**
     * Gets the string comment associated to this property matching the given <code>lang</code>.
     *
     * @param lang the language attribute for the desired comment.
     * @return the property comment.
     */
    public String getComment(String lang) {
        if (m_prop instanceof OntProperty) {
            return ((OntProperty) m_prop).getComment(lang);
        }
        return null;
    }

    /**
     * Gets the Property code name defined in the SWBOntology.
     *
     * @return the Property code name.
     */
    public String getPropertyCodeName() {
        if (codeName == null) {
            //TODO: Check why try-catch is required here
            try {
                Property prop = SWBPlatform.getSemanticMgr().getVocabulary()
                        .getSemanticProperty(SemanticVocabulary.SWB_ANNOT_PROPERTYCODENAME).getRDFProperty();

                codeName = m_prop.getRequiredProperty(prop).getString();
                if (codeName == null) {
                    codeName = getName();
                }
            } catch (Exception pnf) {
                codeName = getName();
            }
        }
        return codeName;
    }

    /**
     * Gets the default value of this property defined in SWBOntology.
     *
     * @return the default value for the property.
     */
    public String getDefaultValue() {
        if (defaultValue == null) {
            Property prop = SWBPlatform.getSemanticMgr().getVocabulary()
                    .getSemanticProperty(SemanticVocabulary.SWB_PROP_DEFAULTVALUE).getRDFProperty();

            Statement st = m_prop.getProperty(prop);
            if (st != null) {
                defaultValue = st.getString();
            }
        }

        if (defaultValue != null && defaultValue.charAt(0) == '{') {
            if (defaultValue.equals("{invtime}")) return "" + (Long.MAX_VALUE - System.currentTimeMillis());
            if (defaultValue.equals("{time}")) return "" + (System.currentTimeMillis());
        }

        return defaultValue;
    }

    /**
     * Gets the URI for this property.
     *
     * @return the property URI.
     */
    public String getURI() {
        return m_prop.getURI();
    }

    /**
     * Gets the encoded property URI.
     *
     * @return encoded property URI.
     */
    public String getEncodedURI() {
        return URLEncoder.encode(getURI());
    }

    /**
     * Gets the value of a required property. A required property is considered any property relevant
     * to SemanticOntology for code-generation purposes.
     *
     * @param prop the {@link SemanticProperty} to get value from.
     * @return SemanticLiteral holding Property value.
     */
    public SemanticLiteral getRequiredProperty(SemanticProperty prop) {
        Statement st = m_prop.getProperty(prop.getRDFProperty());
        if (st != null) {
            return new SemanticLiteral(st);
        }
        return null;
    }

    /**
     * Checks if this property is internationalized using SWBPlatform facilities (locales)
     * as defined in SWBOntology.
     *
     * @return true if property internationalized.
     */
    public boolean isLocaleable() {
        if (localeable == null) {
            localeable = false;
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_LOCALEABLE));

            if (st != null) {
                localeable = st.getBoolean();
            }
        }
        return localeable;
    }

    /**
     * Checks whether this property can be used as display name for a SemanticObject instance,
     * as defined in SWBOntology.
     *
     * @return true, if can be used as display name.
     */
    public boolean isUsedAsName() {
        if (usedAsName == null) {
            usedAsName = false;
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr().getSchema()
                    .getRDFOntModel().getProperty(SemanticVocabulary.SWB_ANNOT_CANUSEDASNAME));

            if (st != null) {
                usedAsName = st.getBoolean();
            }
        }
        return usedAsName;
    }

    /**
     * Checks whether this property is required, as defined in SWBOntology.
     *
     * @return true, if is required.
     */
    public boolean isRequired() {
        if (required == null) {
            required = false;
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_REQUIRED));

            if (st != null) {
                required = st.getBoolean();
            }
        }
        return required;
    }

    /**
     * @return
     * @deprecated for naming conventions. Use {@link #isHierachicalRelation()}.
     * Si esta propiedad se utiliza para definir la relacio padre-hijo en el arbol de navegacion.
     */
    @Deprecated
    public boolean isHeraquicalRelation() {
        return isHierachicalRelation();
    }

    /**
     * Checks whether this property is used to define a parent-child relation for tree-like rendering
     * of objects.
     *
     * @return true if this property is a hierarchical relation property.
     */
    public boolean isHierachicalRelation() {
        if (hierarchicalRelation == null) {
            hierarchicalRelation = false;
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_HERARQUICALRELATION));

            if (st != null) {
                hierarchicalRelation = st.getBoolean();
            }
        }
        return hierarchicalRelation;
    }

    /**
     * @return
     * @deprecated for naming conventions. Use {@link #getHierarchicalRelationFilterClass()}.
     * Si esta propiedad se utiliza para definir la relacio padre-hijo en el arbol de navegacion.
     */
    @Deprecated
    public SemanticClass getHerarquicalRelationFilterClass() {
        return getHierarchicalRelationFilterClass();
    }

    /**
     * Gets {@link SemanticClass} to filter in a parent-child relation property.
     *
     * @return SemanticClass for parent-child filtering.
     */
    public SemanticClass getHierarchicalRelationFilterClass() {
        if (hasHierarchicalFilterClass == null) {
            hasHierarchicalFilterClass = false;
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_HERARQUICALRELATIONFILTERCLASS));

            if (st != null) {
                Resource res = st.getResource();
                if (res != null) {
                    hierarchicalFilterClass = SWBPlatform.getSemanticMgr()
                            .getVocabulary().getSemanticClass(res.getURI());
                }
                hasHierarchicalFilterClass = true;
            }
        }
        return hierarchicalFilterClass;
    }

    /**
     * Checks whether this property is described as a remove dependency property in SWBOntology.
     * Range values of this kind of properties are removed when domain objects are removed.
     *
     * @return true if property is described as remove dependency.
     */
    public boolean isRemoveDependency() {
        if (removeDependency == null) {
            removeDependency = false;
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_REMOVEDEPENDENCY));

            if (st != null) {
                removeDependency = st.getBoolean();
            }
        }
        return removeDependency;
    }

    /**
     * Checks whether this property is described as a clone dependency property in SWBOntology.
     * Range values of this kind of properties are cloned when domain objects are cloned.
     *
     * @return true if property is described as clone dependency.
     */
    public boolean isCloneDependency() {
        if (cloneDependency == null) {
            cloneDependency = false;
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_CLONEDEPENDENCY));

            if (st != null) {
                cloneDependency = st.getBoolean();
            }
        }
        return cloneDependency;
    }

    /**
     * Checks whether this property is not observable. Non-observable properties do not write changes to LOG.
     *
     * @return true if property is not observable.
     */
    public boolean isNotObservable() {
        //TODO: Discuss logic and name change from isNotObservable to isObservable to avoid confusion, setting default value to be observable
        if (notObservable == null) {
            notObservable = false;
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_NOTOBSERVABLE));

            if (st != null) {
                notObservable = st.getBoolean();
            }
        }
        return notObservable;
    }

    /**
     * Checks whether code-generation engine will produce code for this property, as described in SWBOntology.
     *
     * @return true if no code will be generated for this property.
     */
    public boolean isNotCodeGeneration() {
        //TODO: Discuss logic and name change from isNotCodeGeneration to isCodeGeneration to avoid confusion, setting default value to be code-generation
        if (notCodeGeneration == null) {
            notCodeGeneration = false;
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_NOTCODEGENERATION));

            if (st != null) {
                notCodeGeneration = st.getBoolean();
            }
        }
        return notCodeGeneration;
    }

    /**
     * Checks whether this property will be inherited to child objects, as described in SWBOntology.
     *
     * @return true if this property will be inherited to child objects.
     */
    public boolean isInheritProperty() {
        if (inheritProperty == null) {
            inheritProperty = false;
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_INHERITPROPERTY));

            if (st != null) {
                inheritProperty = st.getBoolean();
            }
        }
        return inheritProperty;
    }

    /**
     * @return
     * @deprecated for naming conventions. Use {@link #isInverseHierarchicalRelation()}.
     * Si esta propiedad se utiliza para definir la relacio hijo-padre en el arbol de navegacion.
     */
    @Deprecated
    public boolean isInverseHeraquicalRelation() {
        return isInverseHierarchicalRelation();
    }

    /**
     * Checks whether this inverse property is used to define a parent-child relation for tree-like rendering
     * of objects.
     *
     * @return true if this inverse property is a hierarchical relation property.
     */
    public boolean isInverseHierarchicalRelation() {
        SemanticProperty inv = getInverse();
        return inv != null && inv.isHierachicalRelation();
    }

    /**
     * Checks whether this property is managed using an external invocation (remote model).
     *
     * @return true if this property is managed using an external invocation.
     */
    public boolean isExternalInvocation() {
        if (externalInvocation == null) {
            externalInvocation = false;
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_EXTERNALINVOCATION));

            if (st != null) {
                externalInvocation = st.getBoolean();
            }
        }
        return externalInvocation;
    }

    /**
     * Gets the display property associated to this property.
     *
     * @return the display property.
     */
    public SemanticObject getDisplayProperty() {
        if (!dispProperty) {
            Statement st = m_prop.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_DISPLAYPROPERTY));

            if (st != null) {
                displayProperty = SemanticObject.createSemanticObject(st.getResource());
                dispProperty = true;
            }
        }
        return displayProperty;
    }

    /**
     * Gets the display name for this property, ignoring language.
     *
     * @return the display name of the property.
     */
    public String getDisplayName() {
        return getDisplayName(null);
    }

    /**
     * Gets the display name for this property matching <code>lang</code>. This method tries to get the name from the
     * associated DisplayProperty, falling back to returning property label or property name.
     * <p>
     * If <code>lang</code> is not contained as part of the literal value of the property, Spanish "es" is used as default.
     *
     * @return the display name of the property.
     */
    public String getDisplayName(String lang) {
        String defLang = null != lang ? lang : "es";
        String ret = null;

        SemanticObject obj = getDisplayProperty();
        if (obj != null) {
            ret = obj.getProperty(obj.getModel().getSemanticProperty(SemanticVocabulary.RDFS_LABEL), null, defLang);

            if (ret == null) {
                ret = obj.getProperty(obj.getModel().getSemanticProperty(SemanticVocabulary.RDFS_LABEL));
            }
        }

        if (ret == null) {
            ret = getLabel(lang);
        }

        if (ret == null) {
            ret = getLabel();
        }

        if (ret == null) {
            ret = getName();
        }

        return ret;
    }

    @Override
    public String toString() {
        return m_prop.toString();
    }

    @Override
    public int hashCode() {
        return m_prop.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return hashCode() == obj.hashCode();
    }

    /**
     * Gets the domain class for this property.
     *
     * @return the domain class
     */
    public SemanticClass getDomainClass() {
        if (hasInverse()) {
            return m_inverse.getRangeClass();
        }

        SemanticClass ret = null;
        Statement stm = m_prop.getProperty(m_prop.getModel().getProperty(SemanticVocabulary.RDFS_DOMAIN));
        if (stm != null) {
            String domclsid = stm.getResource().getURI();
            if (domclsid != null) {
                ret = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticClass(domclsid);
                //TODO: eliminar esto cuando se separe el vocabulario por ontologia
                if (ret == null) {
                    ret = new SemanticClass(((OntModel) stm.getResource().getModel()).getOntClass(domclsid));
                    SWBPlatform.getSemanticMgr().getVocabulary().registerClass(ret);
                }
            }
        }
        return ret;
    }

    /**
     * Gets class for the range of this property.
     *
     * @return the range class.
     */
    public SemanticClass getRangeClass() {
        SemanticClass ret = null;

        if (hasInverse()) {
            return m_inverse.getDomainClass();
        }

        Statement stm = m_prop.getProperty(m_prop.getModel().getProperty(SemanticVocabulary.RDFS_RANGE));
        if (stm != null) {
            ret = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticClass(stm.getResource().getURI());
        }
        return ret;
    }

    /**
     * Gets the range value of this property as a {@link Resource}.
     *
     * @return the range of this property.
     */
    public Resource getRange() {
        if (!rangeCheck) {
            Statement stm = m_prop.getProperty(m_prop.getModel().getProperty(SemanticVocabulary.RDFS_RANGE));
            if (stm != null) {
                range = stm.getResource();
            }
            rangeCheck = true;
        }
        return range;
    }

    /**
     * Gets the cardinality of the property. Cardinality is checked against properties
     * named with 'has' prefix in SWBOntology instead of checking OWL cardinality.
     *
     * @return the cardinality, 0 for one to manny and 1 for one to one.
     */
    public int getCardinality() {
        if (!cardinalityCheck) {
            String n = getPropertyCodeName();
            if (n == null) {
                n = getName();
            }

            if (n.startsWith("has")) {
                cardinality = 0;
            } else {
                cardinality = 1;
            }
            cardinalityCheck = true;
        }
        return cardinality;
    }

    /**
     * Checks if is this property is an object property. An object property uses an object class as range.
     *
     * @return true, if is an object property.
     */
    public boolean isObjectProperty() {
        if (objectProperty == null) {
            objectProperty = false;
            Statement stm = m_prop.getProperty(m_prop.getModel().getProperty(SemanticVocabulary.RDF_TYPE));

            if (stm != null) {
                objectProperty = SemanticVocabulary.OWL_OBJECTPROPERTY.equals(stm.getResource().getURI());
                if (!objectProperty) {
                    OntClass ontClassDataType = SWBPlatform.getSemanticMgr()
                            .getSchema().getRDFOntModel().createClass(SemanticVocabulary.OWL_OBJECTPROPERTY);

                    if (ontClassDataType.hasSubClass(stm.getResource())) {
                        objectProperty = Boolean.TRUE;
                    }
                }
            }
        }
        return objectProperty;
    }

    /**
     * Checks if is this property is a data type property. A data-type property uses a type literal as range.
     *
     * @return true, if is a data type property.
     */
    public boolean isDataTypeProperty() {
        if (dataTypeProperty == null) {
            dataTypeProperty = false;

            Statement stm = m_prop.getProperty(m_prop.getModel().getProperty(SemanticVocabulary.RDF_TYPE));
            if (stm != null) {
                dataTypeProperty = SemanticVocabulary.OWL_DATATYPEPROPERTY.equals(stm.getResource().getURI());
                if (!dataTypeProperty) {
                    OntClass ontClassDataType = SWBPlatform.getSemanticMgr()
                            .getSchema().getRDFOntModel().createClass(SemanticVocabulary.OWL_DATATYPEPROPERTY);

                    if (ontClassDataType.hasSubClass(stm.getResource())) {
                        dataTypeProperty = Boolean.TRUE;
                    }
                }
            }
        }
        return dataTypeProperty;
    }

    /**
     * Checks if is this property has an inverse property.
     *
     * @return true, if has at least one inverse property.
     * @see OntProperty#hasInverse()
     */
    public boolean hasInverse() {
        if (hasInverse == null) {
            hasInverse = false;
            if (m_prop instanceof OntProperty) {
                hasInverse = ((OntProperty) m_prop).hasInverse();
            }
        }
        return hasInverse;
    }

    /**
     * Checks whether this property is an inverse property.
     *
     * @return true if this property is an inverse property
     */
    public boolean isInverseOf() {
        return inverse;
    }

    /**
     * Gets the inverse property associated with this property.
     *
     * @return the inverse property.
     */
    public SemanticProperty getInverse() {
        return m_inverse;
    }

    /**
     * Checks whether this property is a boolean property (range is #XMLS_BOOLEAN).
     *
     * @return true, if this property is a boolean property.
     */
    public boolean isBoolean() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.XMLS_BOOLEAN));
    }

    /**
     * Checks whether this property is an integer property (range is #XMLS_INTEGER or #XMLS_INT).
     *
     * @return true, if this property is an integer property.
     */
    public boolean isInt() {
        Resource res = getRange();
        return res != null &&
                (res.getURI().equals(SemanticVocabulary.XMLS_INTEGER) || res.getURI().equals(SemanticVocabulary.XMLS_INT));
    }

    /**
     * Checks whether this property is a base64 encoded string (range is #XMLS_BASE64BINARY).
     *
     * @return true, if this property is a base64 encoded string.
     */
    public boolean isBinary() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.XMLS_BASE64BINARY));
    }

    /**
     * Checks whether this property is a long property (range is #XMLS_LONG).
     *
     * @return true, if this property is a long property.
     */
    public boolean isLong() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.XMLS_LONG));
    }

    /**
     * Checks whether this property is a date property (range is #XMLS_DATE).
     *
     * @return true, if this property is a date property.
     */
    public boolean isDate() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.XMLS_DATE));
    }

    /**
     * Checks whether this property is a datetime property (range is #XMLS_DATETIME).
     *
     * @return true, if this property is a datetime property.
     */
    public boolean isDateTime() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.XMLS_DATETIME));
    }

    /**
     * Checks whether this property is a string property (range is #XMLS_STRING).
     *
     * @return true, if this property is a String property.
     */
    public boolean isString() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.XMLS_STRING));
    }

    /**
     * Checks whether this property is a float property (range is #XMLS_FLOAT).
     *
     * @return true, if this property is a float property.
     */
    public boolean isFloat() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.XMLS_FLOAT));
    }

    /**
     * Checks whether this property is an XML String property (range is #RDF_XMLLITERAL).
     *
     * @return true, if this property is an XML String property.
     */
    public boolean isXML() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.RDF_XMLLITERAL));
    }

    /**
     * Checks whether this property is a double property (range is #XMLS_DOUBLE).
     *
     * @return true, if this property is a double property.
     */
    public boolean isDouble() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.XMLS_DOUBLE));
    }

    /**
     * Checks whether this property is a binary string property (range is #XMLS_BYTE).
     *
     * @return true, if this property is a binary string property.
     */
    public boolean isByte() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.XMLS_BYTE));
    }

    /**
     * Checks whether this property is a short property (range is #XMLS_SHORT).
     *
     * @return true, if this property is a short property.
     */
    public boolean isShort() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.XMLS_SHORT));
    }

    /**
     * Checks whether this property is a decimal property (range is #XMLS_DECIMAL).
     *
     * @return true, if this property is a decimal property.
     */
    public boolean isDecimal() {
        Resource res = getRange();
        return (res != null && res.getURI().equals(SemanticVocabulary.XMLS_DECIMAL));
    }

    /**
     * Checks whether this property is a numeric property (property is int, long, byte, double, float or short).
     *
     * @return true, if this property is a numeric property.
     */
    public boolean isNumeric() {
        return isInt() || isLong() || isByte() || isDouble() || isFloat() || isShort();
    }

    /**
     * Gets an iterator to the restrictions associated to this property on a specific SemanticClass.
     *
     * @param cls the {@link SemanticClass} to check property restrictions.
     * @return Iterator of SemanticRestrictions
     */
    public Iterator<SemanticRestriction> listRestrictions(SemanticClass cls) {
        if (restrictions == null) {
            loadRestrictions();
        }

        ArrayList<SemanticRestriction> list = restrictions.get(cls.getURI());
        if (list != null) {
            return list.iterator();
        }
        return new ArrayList<SemanticRestriction>().iterator();
    }

    /**
     * Gets a {@link SemanticRestriction} for the property on a specific {@link SemanticClass}.
     *
     * @param cls the {@link SemanticClass} to get property restriction.
     * @return SemanticRestriction of property on class or null if no restriction exists.
     */
    public SemanticRestriction getValuesFromRestriction(SemanticClass cls) {
        if (restrictions == null) {
            loadRestrictions();
        }

        int level = -1;
        SemanticRestriction rcls = frestrictions.get(cls.getURI());
        if (rcls == null && !frestrictions.containsKey(cls.getURI())) {
            ArrayList<SemanticRestriction> list = restrictions.get(cls.getURI());
            if (list != null) {
                for (SemanticRestriction restriction : list) {
                    if (restriction.isAllValuesFromRestriction() || restriction.isSomeValuesFromRestriction() ||
                            restriction.isHasValueRestriction()) {

                        int l = restriction.getSubClassLevel(cls);
                        if (level < 0 || level > l) {
                            rcls = restriction;
                            level = l;
                        }
                    }
                }
            }
            frestrictions.put(cls.getURI(), rcls);
        }
        return rcls;
    }

    /**
     * Registers a Property observer.
     *
     * @param observer the Observer
     */
    public void registerObserver(SemanticObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes a Property observer.
     *
     * @param observer the Observer
     */
    public void removeObserver(SemanticObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies a change in the Property to registered observers.
     *
     * @param obj    the changed {@link SemanticObject}
     * @param prop   the changed {@link SemanticProperty}
     * @param lang   the language
     * @param action the action that triggered the change
     */
    public void notifyChange(SemanticObject obj, Object prop, String lang, String action) {
        for (SemanticObserver observer : observers) {
            try {
                observer.notify(obj, prop, lang, action);
            } catch (Exception e) {
                LOG.error(e);
            }
        }
    }
}