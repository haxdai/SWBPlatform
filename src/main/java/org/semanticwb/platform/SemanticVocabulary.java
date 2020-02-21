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
import com.hp.hpl.jena.rdf.model.Property;
import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class to manage ontology vocabulary definitions (classes and properties).
 * @author Jei
 */
public class SemanticVocabulary {
    /**
     * The Constant RDF_URI.
     */
    public static final String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /**
     * The Constant RDF_TYPE.
     */
    public static final String RDF_TYPE = RDF_URI + "type";

    /**
     * The Constant RDF_PROPERTY.
     */
    public static final String RDF_PROPERTY = RDF_URI + "Property";

    /**
     * The Constant RDF_XMLLITERAL.
     */
    public static final String RDF_XMLLITERAL = RDF_URI + "XMLLiteral";

    /**
     * The Constant RDFS_URI.
     */
    public static final String RDFS_URI = "http://www.w3.org/2000/01/rdf-schema#";

    /**
     * The Constant RDFS_RESOURCE.
     */
    public static final String RDFS_RESOURCE = RDFS_URI + "Resource";

    /**
     * The Constant RDFS_LABEL.
     */
    public static final String RDFS_LABEL = RDFS_URI + "label";

    /**
     * The Constant RDFS_COMMENT.
     */
    public static final String RDFS_COMMENT = RDFS_URI + "comment";

    /**
     * The Constant RDFS_DOMAIN.
     */
    public static final String RDFS_DOMAIN = RDFS_URI + "domain";

    /**
     * The Constant RDFS_RANGE.
     */
    public static final String RDFS_RANGE = RDFS_URI + "range";

    /**
     * The Constant RDFS_SUBPROPERTYOF.
     */
    public static final String RDFS_SUBPROPERTYOF = RDFS_URI + "subPropertyOf";

    /**
     * The Constant RDFS_SUBCLASSOF.
     */
    public static final String RDFS_SUBCLASSOF = RDFS_URI + "subClassOf";

    /**
     * The Constant XMLS_URI.
     */
    public static final String XMLS_URI = "http://www.w3.org/2001/XMLSchema#";

    /**
     * The Constant XMLS_DATETIME.
     */
    public static final String XMLS_DATETIME = XMLS_URI + "dateTime";

    /**
     * The Constant XMLS_DATE.
     */
    public static final String XMLS_DATE = XMLS_URI + "date";

    /**
     * The Constant XMLS_BOOLEAN.
     */
    public static final String XMLS_BOOLEAN = XMLS_URI + "boolean";

    /**
     * The Constant XMLS_STRING.
     */
    public static final String XMLS_STRING = XMLS_URI + "string";

    /**
     * The Constant XMLS_INT.
     */
    public static final String XMLS_INT = XMLS_URI + "int";

    /**
     * The Constant XMLS_INTEGERS.
     */
    public static final String XMLS_INTEGER = XMLS_URI + "integer";

    /**
     * The Constant XMLS_FLOAT.
     */
    public static final String XMLS_FLOAT = XMLS_URI + "float";

    /**
     * The Constant XMLS_DOUBLE.
     */
    public static final String XMLS_DOUBLE = XMLS_URI + "double";

    /**
     * The Constant XMLS_LONG.
     */
    public static final String XMLS_LONG = XMLS_URI + "long";

    /**
     * The Constant XMLS_SHORT.
     */
    public static final String XMLS_SHORT = XMLS_URI + "short";

    /**
     * The Constant XMLS_BYTE.
     */
    public static final String XMLS_BYTE = XMLS_URI + "byte";

    /**
     * The Constant XMLS_BYTE.
     */
    public static final String XMLS_DECIMAL = XMLS_URI + "decimal";

    /**
     * The Constant XMLS_BASE64BINARY.
     */
    public static final String XMLS_BASE64BINARY = XMLS_URI + "base64Binary";

    /**
     * The Constant OWL_URI.
     */
    public static final String OWL_URI = "http://www.w3.org/2002/07/owl#";

    /**
     * The Constant OWL_CLASS.
     */
    public static final String OWL_CLASS = OWL_URI + "Class";

    /**
     * The Constant OWL_DATATYPEPROPERTY.
     */
    public static final String OWL_DATATYPEPROPERTY = OWL_URI + "DatatypeProperty";

    /**
     * The Constant OWL_OBJECTPROPERTY.
     */
    public static final String OWL_OBJECTPROPERTY = OWL_URI + "ObjectProperty";

    /**
     * The Constant URI.
     */
    public static final String URI = "http://www.semanticwebbuilder.org/swb4/ontology#";

    /**
     * The Constant PROCESS_URI.
     */
    public static final String PROCESS_URI = "http://www.semanticwebbuilder.org/swb4/process#";

    /**
     * The Constant PROCESS_CLASS.
     */
    public static final String PROCESS_CLASS = PROCESS_URI + "ProcessClass";

    /**
     * The Constant SWBXF_URI.
     */
    public static final String SWBXF_URI = "http://www.semanticwebbuilder.org/swb4/xforms/ontology#";

    /**
     * The Constant SWB_SWBCLASS.
     */
    public static final String SWB_SWBCLASS = URI + "SWBClass";

    /**
     * The Constant SWB_CLASS.
     */
    public static final String SWB_CLASS = URI + "Class";

    /**
     * The Constant SWB_INTERFACE.
     */
    public static final String SWB_INTERFACE = URI + "Interface";

    /**
     * The Constant SWB_MODEL.
     */
    public static final String SWB_MODEL = URI + "Model";

    /**
     * The Constant SWB_FORMELEMENT.
     */
    public static final String SWB_FORMELEMENT = URI + "FormElement";

    /**
     * The Constant SWB_SWBFORMELEMENT.
     */
    public static final String SWB_SWBFORMELEMENT = URI + "SWBFormElement";

    /**
     * The Constant SWB_SEMANTICRESOURCE.
     */
    public static final String SWB_SEMANTICRESOURCE = URI + "SemanticResource";

    /**
     * The Constant SWB_PROP_VALUE.
     */
    public static final String SWB_PROP_VALUE = URI + "value";

    /**
     * The Constant SWB_PROP_HASCLASS.
     */
    public static final String SWB_PROP_HASCLASS = URI + "hasClass";

    /**
     * The Constant SWB_PROP_LOCALEABLE.
     */
    public static final String SWB_PROP_LOCALEABLE = URI + "localeable";

    /**
     * The Constant SWB_PROP_REQUIRED.
     */
    public static final String SWB_PROP_REQUIRED = URI + "required";

    /**
     * The Constant SWB_PROP_DEFAULTVALUE.
     */
    public static final String SWB_PROP_DEFAULTVALUE = URI + "defaultValue";

    /**
     * The Constant SWB_PROP_DISPLAYPROPERTY.
     */
    public static final String SWB_PROP_DISPLAYPROPERTY = URI + "displayProperty";

    /**
     * The Constant SWB_PROP_DISPLAYOBJECT.
     */
    public static final String SWB_PROP_DISPLAYOBJECT = URI + "displayObject";

    /**
     * The Constant SWB_PROP_EXTERNALINVOCATION.
     */
    public static final String SWB_PROP_EXTERNALINVOCATION = URI + "externalInvocation";

    /**
     * The Constant SWB_PROP_HERARQUICALRELATION.
     */
    public static final String SWB_PROP_HERARQUICALRELATION = URI + "herarquicalRelation";
    public static final String SWB_PROP_HERARQUICALRELATIONFILTERCLASS = URI + "herarquicalRelationFilterClass";

    /**
     * The Constant SWB_PROP_REMOVEDEPENDENCY.
     */
    public static final String SWB_PROP_REMOVEDEPENDENCY = URI + "removeDependency";

    /**
     * The Constant SWB_PROP_CLONEDEPENDENCY.
     */
    public static final String SWB_PROP_CLONEDEPENDENCY = URI + "cloneDependency";

    /**
     * The Constant SWB_PROP_HASHERARQUICALNODE.
     */
    public static final String SWB_PROP_HASHERARQUICALNODE = URI + "hasHerarquicalNode";

    /**
     * The Constant SWB_PROP_NOTOBSERVABLE.
     */
    public static final String SWB_PROP_NOTOBSERVABLE = URI + "notObservable";

    /**
     * The Constant SWB_PROP_NOTCODEGENERATION.
     */
    public static final String SWB_PROP_NOTCODEGENERATION = URI + "notCodeGeneration";

    /**
     * The Constant SWB_PROP_INHERITPROPERTY.
     */
    public static final String SWB_PROP_INHERITPROPERTY = URI + "inheritProperty";

    /**
     * The Constant SWB_ANNOT_PROPERTYCODENAME.
     */
    public static final String SWB_ANNOT_PROPERTYCODENAME = URI + "propertyCodeName";

    /**
     * The Constant SWB_ANNOT_CLASSCODENAME.
     */
    public static final String SWB_ANNOT_CLASSCODENAME = URI + "classCodeName";

    /**
     * The Constant SWB_ANNOT_CLASSCODEPACKAGE.
     */
    public static final String SWB_ANNOT_CLASSCODEPACKAGE = URI + "classCodePackage";

    /**
     * The Constant SWB_ANNOT_AUTOGENID.
     */
    public static final String SWB_ANNOT_AUTOGENID = URI + "autogenId";

    /**
     * The Constant SWB_ANNOT_CANUSEDASNAME.
     */
    public static final String SWB_ANNOT_CANUSEDASNAME = URI + "canUsedAsName";

    /**
     * The Constant SWB_PROP_CLASSGROUPID.
     */
    public static final String SWB_PROP_CLASSGROUPID = URI + "classGroupId";

    /**
     * The Constant SWB_PROP_DISABLECACHE.
     */
    public static final String SWB_PROP_DISABLECACHE = URI + "disableCache";

    /**
     * The Constant SWB_PROP_NOTCLASSCODEGENERATION.
     */
    public static final String SWB_PROP_NOTCLASSCODEGENERATION = URI + "notClassCodeGeneration";

    /**
     * The Constant SWB_PROP_PREFIX.
     */
    public static final String SWB_PROP_PREFIX = URI + "prefix";

    /**
     * The Constant SWB_PROP_PACKAGE.
     */
    public static final String SWB_PROP_PACKAGE = URI + "package";

    /**
     * The Constant SWB_ANNOT_FORMELEMENTRANGE.
     */
    public static final String SWB_ANNOT_FORMELEMENTRANGE = URI + "formElementRange";

    /**
     * The LOG.
     */
    private static final Logger LOG = SWBUtils.getLogger(SemanticVocabulary.class);

    /**
     * The classes.
     */
    public ConcurrentMap<String, SemanticClass> classes;

    /**
     * The properties.
     */
    public ConcurrentMap<String, SemanticProperty> properties;

    /**
     * The clsbyid.
     */
    private ConcurrentMap<String, SemanticClass> clsbyid;

    /**
     * The clsbyname.
     */
    private ConcurrentMap<String, SemanticClass> clsbyname;

    /**
     * The clsbyname.
     */
    private ConcurrentMap<String, SemanticClass> clsbyVirtualName;

    /**
     * Constructor. Creates a new {@link SemanticVocabulary}.
     */
    public SemanticVocabulary() {
        classes = new ConcurrentHashMap<>();
        clsbyid = new ConcurrentHashMap<>();
        clsbyname = new ConcurrentHashMap<>();
        clsbyVirtualName = new ConcurrentHashMap<>();
        properties = new ConcurrentHashMap<>();
    }

    /**
     * Inits the {@link SemanticVocabulary}.
     */
    public void init() {
    }

    /**
     * Removes properties from SemanticVocabulary that are not part of the model.
     */
    public void filterProperties() {
        Iterator<SemanticClass> tpcit = listSemanticClasses();
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            filterProperties(tpc);
        }
    }

    /**
     * Removes properties that are not part of a SemanticClass.
     * @param cls the SemanticClass
     */
    private void filterProperties(SemanticClass cls) {
        Iterator<SemanticProperty> tppit = cls.listProperties();
        while (tppit.hasNext()) {
            SemanticProperty tpp = tppit.next();
            if (tpp.getDomainClass() == null || (tpp.hasInverse() && !(cls.equals(tpp.getDomainClass()) ||
                    cls.isSubClass(tpp.getDomainClass())))) {

                tppit.remove();
                cls.hierarchicalProps.remove(tpp);
                cls.inverseHierarchicalProps.remove(tpp);
            }
        }
    }

    /**
     * Adds a SemanticClass to the vocabulary.
     * @param cls the SemanticClass to add.
     */
    private void addSemanticClass(SemanticClass cls) {
        if (null != cls) {
            //Add class to URI map
            classes.put(cls.getURI(), cls);

            //Add class to ID map
            String clsid = cls.getClassId();
            if (clsid != null) {
                clsbyid.put(clsid, cls);
            }

            //Add class to name map
            String clsname = cls.getClassName();
            if (clsname != null) {
                clsbyname.put(clsname, cls);
            }

            //Add class to virtualname map
            clsname = cls.getVirtualClassName();
            if (clsname != null) {
                clsbyVirtualName.put(cls.getVirtualClassName(), cls);
            }
        }
    }

    /**
     * Gets an iterator to the semantic classes in vocabulary.
     * @return Iterator of semantic classes.
     */
    public Iterator<SemanticClass> listSemanticClasses() {
        return new ArrayList<>(classes.values()).iterator();
    }

    /**
     * Gets an iterator to the semantic classes in vocabulary as {@link SemanticObject} instances.
     * @return Iterator of semantic objects.
     */
    public Iterator<SemanticObject> listSemanticClassesAsSemanticObjects() {
        List<SemanticObject> arr = new ArrayList<>();
        for (SemanticClass cls : classes.values()) {
            arr.add(cls.getSemanticObject());
        }
        return arr.iterator();
    }

    /**
     * Gets a {@link SemanticClass} with matching <code>uri</code>.
     *
     * @param uri the class URI.
     * @return the {@link SemanticClass} or null if URI does not exist in model
     */
    public SemanticClass getSemanticClass(String uri) {
        SemanticClass cls = classes.get(uri);
        if (cls == null && uri != null) {
            OntModel ont = SWBPlatform.getSemanticMgr().getSchema().getRDFOntModel();
            try {
                OntClass c = ont.getOntClass(uri);
                if (c != null) {
                    cls = new SemanticClass(c);
                    registerClass(cls);
                }
            } catch (Exception e) {
                LOG.warn(uri, e);
            }
        }

        return cls;
    }

    /**
     * Gets the semantic class by id.
     *
     * @param classId the class id
     * @return the semantic class by id
     */
    public SemanticClass getSemanticClassById(String classId) {
        if (classId == null) return null;
        return clsbyid.get(classId);
    }

    /**
     * Gets the semantic class by java name.
     *
     * @param className the class name
     * @return the semantic class by java name
     */
    public SemanticClass getSemanticClassByJavaName(String className) {
        if (className == null) return null;
        return clsbyname.get(className);
    }

    /**
     * Gets the semantic class by java name.
     *
     * @param className the class name
     * @return the semantic class by java name
     */
    public SemanticClass getSemanticClassByVirtualJavaName(String className) {
        if (className == null) return null;
        return clsbyVirtualName.get(className);
    }

    /**
     * Adds a {@link SemanticProperty} to the Vocabulary.
     * @param property the {@link SemanticProperty} to add
     */
    private void addSemanticProperty(SemanticProperty property) {
        if (!properties.containsKey(property.getURI())) {
            properties.put(property.getURI(), property);
        }
    }

    /**
     * Gets an iterator of {@link SemanticProperty} objects in the vocabulary.
     * @return the iterator of {@link SemanticProperty}
     */
    public Iterator<SemanticProperty> listSemanticProperties() {
        return properties.values().iterator();
    }

    /**
     * Gets a {@link SemanticProperty} from the vocabulary matching <code>propId</code>.
     * @param propId the Property ID.
     * @return the {@link SemanticProperty} or null if there is no property with matching <code>propId</code>
     */
    public SemanticProperty getSemanticPropertyById(String propId) {
        int i = propId.indexOf(':');
        if (i > 0) {
            String prefix = propId.substring(0, i);
            String name = propId.substring(i + 1);
            String uri = getNsPrefixMap().get(prefix) + name;
            return getSemanticProperty(uri);
        }
        return null;
    }

    /**
     * Gets a {@link SemanticProperty} from the vocabulary matching <code>uri</code>.
     * @param uri the {@link SemanticProperty} URI.
     * @return the {@link SemanticProperty} or null if URI does not exists in model.
     */
    public SemanticProperty getSemanticProperty(String uri) {
        SemanticProperty prop = properties.get(uri);
        if (prop == null) {
            OntModel ont = SWBPlatform.getSemanticMgr().getSchema().getRDFOntModel();

            try {
                Property p = ont.getProperty(uri);
                if (p != null) {
                    prop = new SemanticProperty(p);
                    addSemanticProperty(prop);
                }
            } catch (Exception e) {
                LOG.warn(uri, e);
            }
        }
        return prop;
    }

    /**
     * Gets the <code>property</code> {@link SemanticProperty}.
     * @param property the property to get.
     * @return the semantic property or null.
     */
    public SemanticProperty getSemanticProperty(Property property) {
        if (null != property) {
            SemanticProperty prop = properties.get(property.getURI());
            if (prop == null) {
                prop = new SemanticProperty(property);
                addSemanticProperty(prop);
            }
            return prop;
        }
        return null;
    }

    /**
     * Gets the namespaces prefix map.
     * @return the namespaces prefix map.
     */
    private Map<String, String> getNsPrefixMap() {
        HashMap<String, String> namespaces = new HashMap<>();
        for (Map.Entry<String, String> entry : SWBPlatform.getSemanticMgr().getSchema().getRDFOntModel().getNsPrefixMap().entrySet()) {
            namespaces.put(entry.getKey(), entry.getValue());
        }
        return namespaces;
    }

    /**
     * Registers a {@link SemanticClass} to be part of vocabulary, filtering its properties.
     * @param cls the {@link SemanticClass} to add.
     */
    public void registerClass(SemanticClass cls) {
        registerClass(cls, true);
    }

    /**
     * Registers a {@link SemanticClass} to be part of vocabulary.
     * @param cls the {@link SemanticClass} to add.
     * @param filterProps whether to filter class properties.
     */
    public void registerClass(SemanticClass cls, boolean filterProps) {
        if (null != cls && null != cls.getURI() && !classes.containsKey(cls.getURI())) {
            LOG.trace("Registering SemanticClass: " + cls + " --> " + cls.getClassName());
            addSemanticClass(cls);
            Iterator<SemanticProperty> propit = cls.listProperties();

            //Add properties to vocabulary
            while (propit.hasNext()) {
                SemanticProperty prop = propit.next();
                addSemanticProperty(prop);
            }

            //Filter class properties
            if (filterProps) {
                filterProperties(cls);
            }
        }
    }
}