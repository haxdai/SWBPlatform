/*
 * SemanticWebBuilder es una plataforma para el desarrollo de portales y aplicaciones de integración,
 * colaboración y conocimiento, que gracias al uso de tecnología semántica puede generar contextos de
 * información alrededor de algún tema de interés o bien integrar información y aplicaciones de diferentes
 * fuentes, donde a la información se le asigna un significado, de forma que pueda ser interpretada y
 * procesada por personas y/o sistemas, es una creación original del Fondo de Información y Documentación
 * para la Industria INFOTEC, cuyo registro se encuentra actualmente en trámite.
 *
 * INFOTEC pone a su disposición la herramienta SemanticWebBuilder a través de su licenciamiento abierto al público (‘open source’),
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
 *  http://www.semanticwebbuilder.org
 */
package org.semanticwb.platform;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import org.semanticwb.Logger;
import org.semanticwb.SWBException;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.base.util.URLEncoder;
import org.semanticwb.model.GenericIterator;
import org.semanticwb.model.GenericObject;

import java.lang.reflect.Constructor;
import java.util.*;

import static org.semanticwb.SWBUtils.TEXT.getPlural;
import static org.semanticwb.SWBUtils.TEXT.capitalize;

/**
 * Java Wrapper for an OWL Class.
 * @author Jei
 */
public class SemanticClass {

    /**
     * The log.
     */
    private static Logger log = SWBUtils.getLogger(SemanticClass.class);

    /**
     * The mClass.
     */
    private OntClass mClass;                           //clase ontologia schema

    /**
     * The mProps.
     */
    private HashMap<String, SemanticProperty> mProps;

    /**
     * The m_is swb class.
     */
    private Boolean isSWBClass = null;

    /**
     * No hay codigo generado de la clase
     */
    private Boolean isSWBVirtClass = null;

    /**
     * The m_is swb interface.
     */
    private Boolean isSWBInterface = null;

    /**
     * The m_is swb model.
     */
    private Boolean isSWBModel = null;

    /**
     * The m_is swb form element.
     */
    private Boolean isSWBFormElement = null;

    /**
     * The m_is swb semantic resource.
     */
    private Boolean isSWBSemanticResource = null;

    /**
     * The mClass code name.
     */
    private String classCodeName;

    /**
     * The mClass code package.
     */
    private String classCodePackage;

    /**
     * The m_autogen id.
     */
    private Boolean autogenId;

    /**
     * The m_autogen id.
     */
    private Boolean disableCache;

    /**
     * The m_autogen id.
     */
    private Boolean notClassCodeGeneration;

    /**
     * The mCls.
     */
    private Class mCls = null;

    /**
     * The mConstructor.
     */
    private Constructor mConstructor = null;

    /**
     * The display name property.
     */
    private SemanticProperty displayNameProperty;

    /**
     * The herarquical props.
     */
    List<SemanticProperty> hierarchicalProps;

    /**
     * The inverse herarquical props.
     */
    List<SemanticProperty> inverseHierarchicalProps;

    /**
     * The mClass group id.
     */
    private String classGroupId;

    /**
     * The m_is class group id check.
     */
    private boolean isClassGroupIdCheck = false;

    /**
     * The disp object.
     */
    private boolean dispObject = false;

    /**
     * The display object.
     */
    private SemanticObject displayObject = null;

    /**
     * The mClass name.
     */
    private String className = null;

    /**
     * The mClass name.
     */
    private String virtualClassName = null;

    /**
     * The mObservers.
     */
    private List<SemanticObserver> mObservers = null;

    /**
     * Instantiates a new semantic class.
     *
     * @param oclass the oclass
     */
    public SemanticClass(OntClass oclass) {
        this.mClass = oclass;
        init();
    }

    /**
     * Instantiates a new semantic class.
     *
     * @param classuri the classuri
     * @throws SWBException the sWB exception
     */
    public SemanticClass(String classuri) throws SWBException {
        this.mClass = SWBPlatform.getSemanticMgr().getSchema().getRDFOntModel().getOntClass(classuri);
        if (this.mClass == null) {
            throw new SWBException("OntClass Not Found");
        }
        init();
    }

    /**
     * Gets the semantic object.
     *
     * @return the semantic object
     */
    public SemanticObject getSemanticObject() {
        return SemanticObject.createSemanticObject(getURI());
    }

    /**
     * Inits the {@link SemanticClass} loading properties into memory.
     */
    private void init() {
        mProps = new HashMap<>();
        hierarchicalProps = new ArrayList<>();
        inverseHierarchicalProps = new ArrayList<>();
        mObservers = Collections.synchronizedList(new ArrayList<>());

        for (Iterator i = mClass.listDeclaredProperties(false); i.hasNext(); ) {
            Property prop = (Property) i.next();
            SemanticProperty p = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(prop);

            if (p.isUsedAsName()) {
                displayNameProperty = p;
            }
            if (p.isHeraquicalRelation()) {
                hierarchicalProps.add(p);
            }
            if (p.isInverseHeraquicalRelation()) {
                inverseHierarchicalProps.add(p);
            }
            mProps.put(p.getName(), p);
        }
        log.trace("SemanticClass:" + getName() + " " + getClassCodeName() + " " + mClass.getNameSpace() + " " + getPrefix());
    }

    /**
     * regresa nombre local de la clase.
     *
     * @return
     */
    public String getName() {
        return mClass.getLocalName();
    }

    /**
     * Regresa Prefijo de la clase en base al NS de la ontologia.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return mClass.getOntModel().getNsURIPrefix(mClass.getNameSpace());
    }

    /**
     * Regresa nombre de la clase con paquete, siempre y cuendo sea del tipo swb:Class.
     *
     * @return
     */
    public String getClassName() {
        if (className == null) {
            SemanticClass cls = this;
            while (cls != null) {
                if (cls.isSWB()) {
                    className = cls.getClassCodeName();
                    if (cls.getCodePackage() != null) {
                        className = cls.getCodePackage() + "." + className;
                    }
                    break;
                } else {
                    Iterator<SemanticClass> it = cls.listSuperClasses(true);
                    cls = null;
                    SemanticClass acls = null;
                    while (it.hasNext()) {
                        SemanticClass aux = it.next();
                        if (aux.isSWBClass()) {
                            cls = aux;
                        }
                        if (!aux.isSWB()) {
                            acls = aux;
                        }
                    }
                    if (cls == null) {
                        cls = acls;
                    }
                }
            }
        }
        return className;
    }

    /**
     * Regresa nombre de la clase con paquete, siempre y cuendo sea del tipo virtualClass.
     *
     * @return
     */
    public String getVirtualClassName() {
        if (virtualClassName == null) {
            SemanticClass cls = this;
            while (cls != null) {
                if (cls.isSWB() || cls.isSWBVirtualClass()) {
                    virtualClassName = cls.getClassCodeName();
                    if (cls.getCodePackage() != null) {
                        virtualClassName = cls.getCodePackage() + "." + virtualClassName;
                    }
                    break;
                } else {
                    Iterator<SemanticClass> it = cls.listSuperClasses(true);
                    cls = null;
                    SemanticClass acls = null;
                    while (it.hasNext()) {
                        SemanticClass aux = it.next();
                        if (aux.isSWBClass()) {
                            cls = aux;
                        }
                        if (!aux.isSWB()) {
                            acls = aux;
                        }
                    }
                    if (cls == null) {
                        cls = acls;
                    }
                }
            }
        }
        return virtualClassName;
    }

    /**
     * Regresa paquete de la clase generica java definido den la ontologia.
     *
     * @return
     */
    public String getCodePackage() {
        if (classCodePackage == null) {
            try {
                Property prop = SWBPlatform.getSemanticMgr().getVocabulary()
                        .getSemanticProperty(SemanticVocabulary.SWB_ANNOT_CLASSCODEPACKAGE).getRDFProperty();

                classCodePackage = mClass.getRequiredProperty(prop).getString();
            } catch (Exception pnf) {
                classCodePackage = SWBPlatform.getSemanticMgr().getCodePackage().getPackage(getPrefix());
            }
        }
        return classCodePackage;
    }

    /**
     * Gets the name in plural.
     *
     * @return the name in plural
     */
    public String getNameInPlural() {
        String name = getUpperClassName();
        return getPlural(name);
    }


    /**
     * Gets the canonical name.
     *
     * @return the canonical name
     */
    public String getCanonicalName() {
        String spackage = getCodePackage();
        if (spackage == null) {
            spackage = "";
        } else {
            spackage = spackage + ".";
        }
        if (getClassCodeName() != null) {
            spackage += capitalize(getClassCodeName());
        } else {
            spackage += getNameInPlural();
        }
        return spackage;
    }

    /**
     * Gets the upper class name.
     *
     * @return the upper class name
     */
    public String getUpperClassName() {
        if (getClassCodeName() != null) {
            return capitalize(getClassCodeName());
        } else {
            return capitalize(getName());
        }
    }


    /**
     * Regresa nombre de la clase definida por la ontologia
     * Nombre de la clase sin paquete.
     *
     * @return
     */
    public String getClassCodeName() {
        if (classCodeName == null) {
            try {
                Property prop = SWBPlatform.getSemanticMgr().getVocabulary()
                        .getSemanticProperty(SemanticVocabulary.SWB_ANNOT_CLASSCODENAME).getRDFProperty();

                classCodeName = mClass.getRequiredProperty(prop).getString();
            } catch (Exception pnf) {
                classCodeName = getName();
            }
            //TODO:corregir modelo para no hacer esto
            try {
                if (classCodeName != null) {
                    classCodeName = ("" + classCodeName.charAt(0)).toUpperCase() + classCodeName.substring(1);
                }
            } catch (IndexOutOfBoundsException iobe) {
                log.error("Class error definition " + this, iobe);
            }
        }
        return classCodeName;
    }

    /**
     * Checks if is autogen id.
     *
     * @return true, if is autogen id
     */
    public boolean isAutogenId() {
        if (autogenId == null) {
            Property prop = SWBPlatform.getSemanticMgr().getVocabulary()
                    .getSemanticProperty(SemanticVocabulary.SWB_ANNOT_AUTOGENID).getRDFProperty();

            try {
                autogenId = mClass.getRequiredProperty(prop).getBoolean();
            } catch (PropertyNotFoundException noe) {
                autogenId = false;
            }
        }
        return autogenId;
    }

    /**
     * Checks if disble cache.
     *
     * @return true, if is disable cache
     */
    public boolean isDisableCache() {
        if (disableCache == null) {
            Property prop = SWBPlatform.getSemanticMgr().getVocabulary()
                    .getSemanticProperty(SemanticVocabulary.SWB_PROP_DISABLECACHE).getRDFProperty();

            try {
                disableCache = mClass.getRequiredProperty(prop).getBoolean();
            } catch (PropertyNotFoundException noe) {
                disableCache = false;
            }
        }
        return disableCache;
    }

    /**
     * Checks if disble cache.
     *
     * @return true, if is disable cache
     */
    public boolean isNotClassCodeGeneration() {
        if (notClassCodeGeneration == null) {
            Property prop = SWBPlatform.getSemanticMgr().getVocabulary()
                    .getSemanticProperty(SemanticVocabulary.SWB_PROP_NOTCLASSCODEGENERATION).getRDFProperty();

            try {
                notClassCodeGeneration = mClass.getRequiredProperty(prop).getBoolean();
            } catch (PropertyNotFoundException noe) {
                notClassCodeGeneration = false;
            }
        }
        return notClassCodeGeneration;
    }

    /**
     * Regresa prefix:name.
     *
     * @return
     */
    public String getClassId() {
        return getPrefix() + ":" + getName();
    }

    /**
     * Usado para generar el identificador (uri) de las instancias de la clase,
     * asi como de sus subclases, tambien se usa para identificar al objeto en el URL (webpage:home).
     *
     * @return
     */
    public String getClassGroupId() {
        if (!isClassGroupIdCheck) {
            SemanticProperty prop = SWBPlatform.getSemanticMgr()
                    .getVocabulary().getSemanticProperty(SemanticVocabulary.SWB_PROP_CLASSGROUPID);

            SemanticClass cls = this;
            while (cls != null) {
                SemanticLiteral lit = cls.getRequiredProperty(prop);
                if (lit != null) {
                    classGroupId = lit.getString();
                }

                if (classGroupId != null) {
                    break;
                }

                Iterator<SemanticClass> it = cls.listSuperClasses(true);
                cls = null;
                while (it.hasNext()) {
                    SemanticClass aux = it.next();
                    if (aux.isSWBClass()) {
                        cls = aux;
                    }
                }
            }

            isClassGroupIdCheck = true;
            if (classGroupId == null) {
                classGroupId = getPrefix() + "_" + getName();
            }
        }
        return classGroupId;
    }

    /**
     * Gets the required property.
     *
     * @param prop the prop
     * @return the required property
     */
    public SemanticLiteral getRequiredProperty(SemanticProperty prop) {
        SemanticLiteral ret = null;
        Property iprop = prop.getRDFProperty();

        try {
            ret = new SemanticLiteral(mClass.getRequiredProperty(iprop).getLiteral());
        } catch (PropertyNotFoundException noe) {
        }
        return ret;
    }

    /**
     * List required properties.
     *
     * @param prop the prop
     * @return the iterator
     */
    public Iterator<SemanticLiteral> listRequiredProperties(SemanticProperty prop) {
        ArrayList<SemanticLiteral> literals = new ArrayList<>();
        Property iprop = prop.getRDFProperty();

        try {
            StmtIterator it = mClass.listProperties(iprop);
            while (it.hasNext()) {
                Statement statement = it.nextStatement();
                literals.add(new SemanticLiteral(statement));
            }
            it.close();
        } catch (PropertyNotFoundException noe) {
        }
        return literals.iterator();
    }

    /**
     * List object required properties.
     *
     * @param prop the prop
     * @return the iterator
     */
    public Iterator<SemanticObject> listObjectRequiredProperties(SemanticProperty prop) {
        ArrayList<SemanticObject> objects = new ArrayList<>();
        Property iprop = prop.getRDFProperty();

        try {
            StmtIterator it = mClass.listProperties(iprop);
            while (it.hasNext()) {
                Statement statement = it.nextStatement();
                Resource res = statement.getResource();
                SemanticObject object = SemanticObject.createSemanticObject(res);
                objects.add(object);
            }
            it.close();
        } catch (PropertyNotFoundException noe) {
        }
        return objects.iterator();
    }


    /**
     * Lista las clases relacionadas a esta clase del tipo modelo con la propiedad hasClass
     * Solo si isSWBModel = true.
     *
     * @return clases relacionadas a esta clase del tipo modelo con la propiedad hasClass
     */
    public Iterator<SemanticClass> listModelClasses() {
        Iterator ret = (new ArrayList()).iterator();
        if (isSWBModel()) {
            Property prop = SWBPlatform.getSemanticMgr().getVocabulary()
                    .getSemanticProperty(SemanticVocabulary.SWB_PROP_HASCLASS).getRDFProperty();

            ret = new SemanticClassIterator(mClass.listProperties(prop));
        }
        return ret;
    }

    /**
     * Lista los nodos a mostrar en el arbol de SWB.
     *
     * @return the iterator
     */
    public Iterator<SemanticObject> listHerarquicalNodes() {
        Iterator ret = (new ArrayList()).iterator();
        if (isSWBModel()) {
            Property prop = SWBPlatform.getSemanticMgr().getVocabulary()
                    .getSemanticProperty(SemanticVocabulary.SWB_PROP_HASHERARQUICALNODE).getRDFProperty();

            ret = new SemanticIterator(mClass.listProperties(prop));
        }
        return ret;
    }


    /**
     * List owner models.
     *
     * @return the iterator
     */
    public Iterator<SemanticClass> listOwnerModels() {
        List<SemanticClass> ret = new ArrayList<>();
        if (!isSWBModel()) {
            Property prop = SWBPlatform.getSemanticMgr().getVocabulary()
                    .getSemanticProperty(SemanticVocabulary.SWB_PROP_HASCLASS).getRDFProperty();


            StmtIterator it = mClass.getModel().listStatements(null, prop, mClass);
            while (it.hasNext()) {
                Statement stmt = it.nextStatement();
                ret.add(SWBPlatform.getSemanticMgr().getVocabulary().getSemanticClass(stmt.getSubject().getURI()));
            }
            it.close();
        }
        return ret.iterator();
    }


    /**
     * Gets the constructor.
     *
     * @return the constructor
     */
    public Constructor getConstructor() {
        if (mConstructor == null) {
            try {
                mConstructor = getObjectClass().getDeclaredConstructor(SemanticObject.class);
            } catch (NoSuchMethodException nsme) {
                throw new IllegalArgumentException(nsme); //MAPS74 faltaba el throw
            }
        }
        return mConstructor;

    }

    /**
     * New instance.
     *
     * @param uri the uri
     * @return the semantic object
     */
    public SemanticObject newInstance(String uri) {
        return SemanticObject.createSemanticObject(uri);
    }

    /**
     * New instance.
     *
     * @param res the res
     * @return the semantic object
     */
    public SemanticObject newInstance(Resource res) {
        return SemanticObject.createSemanticObject(res);
    }

    /**
     * New generic instance.
     *
     * @param res the res
     * @return the generic object
     */
    public GenericObject newGenericInstance(Resource res) {
        return SemanticObject.createSemanticObject(res).createGenericInstance();
    }

    /**
     * New generic instance.
     *
     * @param obj the obj
     * @return the generic object
     */
    public GenericObject newGenericInstance(SemanticObject obj) {
        return obj.createGenericInstance();
    }

    /**
     * Crea una nueva instancia del Objeto Generico (no cache).
     *
     * @param obj the obj
     * @return
     */
    GenericObject construcGenericInstance(SemanticObject obj) {
        try {
            return (GenericObject) getConstructor().newInstance(obj);
        } catch (Exception ie) {
            log.event("Error creating object " + obj, ie);
            Throwable th = ie.getCause();
            int cause = 0;
            while (null != th) {
                cause++;
                log.event("Cause " + cause + ":", th);
                th = th.getCause();
            }
            log.event("Aborting....... " + th.getMessage());
            throw new AssertionError(ie);
        }
    }


    /**
     * Gets the object class.
     *
     * @return the object class
     */
    public Class getObjectClass() {
        if (mCls == null) {
            if (isSWBVirtualClass()) {
                try {
                    mCls = Class.forName(getVirtualClassName(), false, SWBPlatform.getSemanticMgr().getClassLoader());
                } catch (Exception noe) {
                }
            }

            if (mCls == null) {
                try {
                    mCls = Class.forName(getClassName());
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        return mCls;
    }

    /**
     * Gets the uRI.
     *
     * @return the uRI
     */
    public String getURI() {
        return mClass.getURI();
    }

    /**
     * Regresa URI codificado para utilizar en ligas de html.
     *
     * @return URI Codificado
     */
    public String getEncodedURI() {
        return URLEncoder.encode(getURI());
    }

    /**
     * Regresa el valor de la propiedad rdf:label de la clase.
     *
     * @param lang the lang
     * @return the label
     */
    public String getLabel(String lang) {
        return mClass.getLabel(lang);
    }

    /**
     * Regresa el valor de la propiedad rdf:comment de la clase.
     *
     * @param lang Language of the comment
     * @return The comment in the ontology
     */

    public String getComment(String lang) {
        return mClass.getComment(lang);
    }

    /**
     * Regresa el valor de la propiedad rdf:comment de la clase.
     *
     * @return The comment in the ontology
     */

    public String getComment() {
        return mClass.getComment(null);
    }

    /**
     * Gets the display name.
     *
     * @param lang the lang
     * @return the display name
     */
    public String getDisplayName(String lang) {
        String ret = null;
        SemanticObject obj = getDisplayObject();

        if (obj != null) {
            if (lang != null) {
                ret = obj.getProperty(SWBPlatform.getSemanticMgr().getVocabulary()
                        .getSemanticProperty(SemanticVocabulary.RDFS_LABEL), null, lang);
            }
            if (ret == null) {
                ret = obj.getProperty(obj.getModel().getSemanticProperty(SemanticVocabulary.RDFS_LABEL));
            }
        }
        if (ret == null && lang != null) {
            ret = getLabel(lang);
        }
        if (ret == null) {
            ret = getLabel(null);
        }
        if (ret == null) {
            ret = getPrefix() + ":" + getName();
        }
        return ret;
    }

    /**
     * Gets the display object.
     *
     * @return the display object
     */
    public SemanticObject getDisplayObject() {
        if (!dispObject) {
            Statement st = mClass.getProperty(SWBPlatform.getSemanticMgr()
                    .getSchema().getRDFOntModel().getProperty(SemanticVocabulary.SWB_PROP_DISPLAYOBJECT));

            if (st != null) {
                displayObject = SemanticObject.createSemanticObject(st.getResource());
                dispObject = true;
            }
        }
        return displayObject;
    }

    /**
     * List instances.
     *
     * @return the iterator
     */
    public Iterator<SemanticObject> listInstances() {
        return listInstances(false);
    }

    /**
     * List instances.
     *
     * @param direct the direct
     * @return the iterator
     */
    public Iterator<SemanticObject> listInstances(boolean direct) {
        //TODO: concat iterators
        ArrayList<SemanticObject> arr = new ArrayList<>();
        //Iterar sobre modelos de datos
        Iterator<Map.Entry<String, SemanticModel>> it2 = SWBPlatform.getSemanticMgr().getModels().iterator();
        while (it2.hasNext()) {
            Map.Entry<String, SemanticModel> entry = it2.next();
            Iterator<SemanticObject> it = entry.getValue().listInstancesOfClass(this, !direct);
            while (it.hasNext()) {
                SemanticObject semanticObject = it.next();
                arr.add(semanticObject);
            }
        }
        //Iterar sobre schema
        Iterator<SemanticObject> it3 = new SemanticObjectIterator(mClass.listInstances(direct));
        while (it3.hasNext()) {
            SemanticObject semanticObject = it3.next();
            arr.add(semanticObject);
        }
        return arr.iterator();
    }

    /**
     * List schema instances.
     *
     * @return the iterator
     */
    public Iterator<SemanticObject> listSchemaInstances() {
        return listSchemaInstances(false);
    }

    /**
     * List schema instances.
     *
     * @param direct the direct
     * @return the iterator
     */
    public Iterator<SemanticObject> listSchemaInstances(boolean direct) {
        return new SemanticObjectIterator(mClass.listInstances(direct));
    }

    /**
     * List generic instances.
     *
     * @return the iterator
     */
    public Iterator listGenericInstances() {
        return listGenericInstances(false);
    }

    /**
     * List generic instances.
     *
     * @param direct the direct
     * @return the iterator
     */
    public Iterator listGenericInstances(boolean direct) {
        return new GenericIterator(listInstances(direct));
    }

    /**
     * List schema generic instances.
     *
     * @return the iterator
     */
    public Iterator listSchemaGenericInstances() {
        return listSchemaGenericInstances(false);
    }

    /**
     * List schema generic instances.
     *
     * @param direct the direct
     * @return the iterator
     */
    public Iterator listSchemaGenericInstances(boolean direct) {
        return new GenericIterator(listSchemaInstances(direct));
    }

    /**
     * Gets the property.
     *
     * @param name the name
     * @return the property
     */
    public SemanticProperty getProperty(String name) {
        return mProps.get(name);
    }

    /**
     * Checks for property.
     *
     * @param name the name
     * @return true, if successful
     */
    public boolean hasProperty(String name) {
        return mProps.containsKey(name);
    }

    /**
     * List properties.
     *
     * @return the iterator
     */
    public Iterator<SemanticProperty> listProperties() {
        return mProps.values().iterator();
    }

    /**
     * List sort properties.
     *
     * @return the iterator
     */
    public Iterator<SemanticProperty> listSortProperties() {
        final SemanticProperty swb_index = org.semanticwb.SWBPlatform.getSemanticMgr()
                .getVocabulary().getSemanticProperty("http://www.semanticwebbuilder.org/swb4/ontology#index");

        TreeSet<SemanticProperty> props = new TreeSet<>(new Comparator<SemanticProperty>() {
            @Override
            public int compare(SemanticProperty o1, SemanticProperty o2) {
                SemanticObject sobj1 = o1.getDisplayProperty();
                SemanticObject sobj2 = o2.getDisplayProperty();
                int v1 = 999999999;
                int v2 = 999999999;
                if (sobj1 != null) {
                    v1 = sobj1.getIntProperty(swb_index);
                }
                if (sobj2 != null) {
                    v2 = sobj2.getIntProperty(swb_index);
                }
                return v1 < v2 ? -1 : 1;
            }
        });

        Iterator<SemanticProperty> it = listProperties();
        while (it.hasNext()) {
            SemanticProperty prop = it.next();
            props.add(prop);
        }
        return props.iterator();
    }

    /**
     * Gets the ont class.
     *
     * @return the ont class
     */
    public OntClass getOntClass() {
        return mClass;
    }

    /**
     * Checks if is super class.
     *
     * @param cls the cls
     * @return true, if is super class
     */
    public boolean isSuperClass(SemanticClass cls) {
        return isSuperClass(cls, false);
    }

    /**
     * Checks if is super class.
     *
     * @param cls    the cls
     * @param direct the direct
     * @return true, if is super class
     */
    public boolean isSuperClass(SemanticClass cls, boolean direct) {
        return cls.isSubClass(this, direct);
    }

    /**
     * Checks if is sub class.
     *
     * @param cls the cls
     * @return true, if is sub class
     */
    public boolean isSubClass(SemanticClass cls) {
        return isSubClass(cls, false);
    }

    /**
     * Checks if is sub class.
     *
     * @param cls    the cls
     * @param direct the direct
     * @return true, if is sub class
     */
    public boolean isSubClass(SemanticClass cls, boolean direct) {
        Iterator it = mClass.listSuperClasses(direct);
        while (it.hasNext()) {
            OntClass cl = (OntClass) it.next();
            if (cl.equals(cls.getOntClass())) {
                return true;
            }
        }
        return false;
    }

    /**
     * List sub classes.
     *
     * @return the iterator
     */
    public Iterator<SemanticClass> listSubClasses() {
        return listSubClasses(false);
    }

    /**
     * List sub classes.
     *
     * @param direct the direct
     * @return the iterator
     */
    public Iterator<SemanticClass> listSubClasses(boolean direct) {
        return new SemanticClassIterator(mClass.listSubClasses(direct));
    }

    /**
     * List super classes.
     *
     * @return the iterator
     */
    public Iterator<SemanticClass> listSuperClasses() {
        return listSuperClasses(false);
    }

    /**
     * List super classes.
     *
     * @param direct the direct
     * @return the iterator
     */
    public Iterator<SemanticClass> listSuperClasses(boolean direct) {
        return new SemanticClassIterator(mClass.listSuperClasses(direct));
    }

    @Override
    public String toString() {
        return mClass.toString();
    }

    @Override
    public int hashCode() {
        return mClass.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            return (hashCode() == obj.hashCode());
        }
        return false;
    }

    /**
     * Check type.
     */
    private void checkType() {
        isSWBClass = false;
        isSWBVirtClass = false;
        isSWBInterface = false;
        isSWBModel = false;
        isSWBFormElement = false;
        isSWBSemanticResource = false;

        for (Iterator i = mClass.listRDFTypes(false); i.hasNext(); ) {
            Resource res = (Resource) i.next();
            String uri = res.getURI();

            if (uri.equals(SemanticVocabulary.SWB_MODEL)) {
                isSWBModel = true;
                break;
            } else if (uri.equals(SemanticVocabulary.SWB_CLASS)) {
                if (isNotClassCodeGeneration()) {
                    isSWBVirtClass = true;
                } else {
                    isSWBClass = true;
                }
                break;
            } else if (uri.equals(SemanticVocabulary.SWB_INTERFACE)) {
                isSWBInterface = true;
                break;
            } else if (uri.equals(SemanticVocabulary.SWB_FORMELEMENT)) {
                isSWBFormElement = true;
                break;
            } else if (uri.equals(SemanticVocabulary.SWB_SEMANTICRESOURCE)) {
                isSWBSemanticResource = true;
                break;
            }
        }
    }

    /**
     * Checks if is sWB class.
     *
     * @return true, if is sWB class
     */
    public boolean isSWBClass() {
        if (isSWBClass == null) {
            checkType();
        }
        return isSWBClass;
    }

    /**
     * Checks if is sWB class.
     *
     * @return true, if is sWB class
     */
    public boolean isSWBVirtualClass() {
        if (isSWBVirtClass == null) {
            checkType();
        }
        return isSWBVirtClass;
    }

    /**
     * Checks if is sWB interface.
     *
     * @return true, if is sWB interface
     */
    public boolean isSWBInterface() {
        if (isSWBInterface == null) {
            checkType();
        }
        return isSWBInterface;
    }

    /**
     * Checks if is sWB model.
     *
     * @return true, if is sWB model
     */
    public boolean isSWBModel() {
        if (isSWBModel == null) {
            checkType();
        }
        return isSWBModel;
    }

    /**
     * Checks if is sWB form element.
     *
     * @return true, if is sWB form element
     */
    public boolean isSWBFormElement() {
        if (isSWBFormElement == null) {
            checkType();
        }
        return isSWBFormElement;
    }

    /**
     * Checks if is sWB semantic resource.
     *
     * @return true, if is sWB semantic resource
     */
    public boolean isSWBSemanticResource() {
        if (isSWBSemanticResource == null) {
            checkType();
        }
        return isSWBSemanticResource;
    }

    /**
     * Gets the display name property.
     *
     * @return the display name property
     */
    public SemanticProperty getDisplayNameProperty() {
        return displayNameProperty;
    }

    /**
     * Checks for herarquical properties.
     *
     * @return true, if successful
     */
    public boolean hasHerarquicalProperties() {
        return !hierarchicalProps.isEmpty();
    }

    /**
     * List herarquical properties.
     *
     * @return the iterator
     */
    public Iterator<SemanticProperty> listHerarquicalProperties() {
        return hierarchicalProps.iterator();
    }

    /**
     * Checks for inverse herarquical properties.
     *
     * @return true, if successful
     */
    public boolean hasInverseHerarquicalProperties() {
        return !inverseHierarchicalProps.isEmpty();
    }

    /**
     * List inverse herarquical properties.
     *
     * @return the iterator
     */
    public Iterator<SemanticProperty> listInverseHerarquicalProperties() {
        return inverseHierarchicalProps.iterator();
    }

    /**
     * Adds the super class.
     *
     * @param cls the cls
     */
    public void addSuperClass(SemanticClass cls) {
        SemanticObject obj = SWBPlatform.getSemanticMgr().getSchema().getSemanticObject(getURI());
        Resource res = obj.getRDFResource();

        res.addProperty(SWBPlatform.getSemanticMgr().getVocabulary()
                .getSemanticProperty(SemanticVocabulary.RDFS_SUBCLASSOF).getRDFProperty(), cls.getOntClass());
    }

    /**
     * Identifica si el tipo de clase es del algun tipo de SWBClass, SWBModel,
     * SWBFormElement, SWBInterface, SWBSemanticResource
     * excepto SWBVirtualClass (notClassCodeGeneration)
     *
     * @return true, if is sWB
     */
    public boolean isSWB() {
        return isSWBClass() || isSWBModel() || isSWBFormElement() || isSWBInterface() || isSWBSemanticResource();
    }

    /**
     * Gets the root class.
     *
     * @return the root class
     */
    public SemanticClass getRootClass() {
        if (isSWBClass()) {
            SemanticClass swbcls = SWBPlatform.getSemanticMgr().getVocabulary()
                    .getSemanticClass(SemanticVocabulary.SWB_SWBCLASS);

            if (this == swbcls || this.isSubClass(swbcls, true)) {
                return this;
            } else {
                Iterator<SemanticClass> it = listSuperClasses();
                while (it.hasNext()) {
                    SemanticClass cls = it.next();
                    if (cls.isSubClass(swbcls, true)) {
                        return cls;
                    }
                }
            }
        }
        return this;
    }

    /**
     * Regresa nivel de subclase o -1 si no es subclase.
     *
     * @param cls the cls
     * @return
     */
    public int getSubClassLevel(SemanticClass cls) {
        return getSubClassLevel(cls, 0);
    }


    /**
     * Gets the sub class level.
     *
     * @param cls the cls
     * @param l   the l
     * @return the sub class level
     */
    private int getSubClassLevel(SemanticClass cls, int l) {
        int r = -1;
        if (this == cls) {
            return l;
        }

        Iterator<SemanticClass> it = listSubClasses(true);
        while (it.hasNext()) {
            SemanticClass c = it.next();
            r = c.getSubClassLevel(cls, l + 1);
            if (r > -1) {
                break;
            }
        }
        return r;
    }

    /**
     * Register observer.
     *
     * @param obs the obs
     */
    public void registerObserver(SemanticObserver obs) {
        mObservers.add(obs);
    }

    /**
     * Removes the observer.
     *
     * @param obs the obs
     */
    public void removeObserver(SemanticObserver obs) {
        mObservers.remove(obs);
    }

    /**
     * Notify change.
     *
     * @param obj    the obj
     * @param prop   the prop
     * @param lang   the lang
     * @param action the action
     */
    public void notifyChange(SemanticObject obj, Object prop, String lang, String action) {
        Iterator it = mObservers.iterator();
        while (it.hasNext()) {
            SemanticObserver obs = (SemanticObserver) it.next();

            try {
                obs.notify(obj, prop, lang, action);
            } catch (Exception e) {
                log.error(e);
            }
        }
    }
}