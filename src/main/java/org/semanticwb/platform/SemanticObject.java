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
 * dirección electrónica http://www.semanticwebbuilder.org.mx
 */
package org.semanticwb.platform;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import org.semanticwb.Logger;
import org.semanticwb.SWBException;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.base.util.HashMapCache;
import org.semanticwb.base.util.URLEncoder;
import org.semanticwb.model.GenericObject;
import org.w3c.dom.Document;

import javax.swing.plaf.nimbus.State;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * High level wrapper for a set of RDF statements. This class is the base class of all SWB Framework.
 * @author javier.solis.g
 */
//TODO: Validate notifications to observers on methods to ensure notification is sent only when action is performed
public class SemanticObject {
    private static final Logger LOG = SWBUtils.getLogger(SemanticObject.class);

    //TODO: Refactor action constants to use an enumeration to avoid errors.
    public static final String ACT_CREATE = "CREATE";
    public static final String ACT_REMOVE = "REMOVE";
    public static final String ACT_ADD = "ADD";
    public static final String ACT_SET = "SET";
    public static final String ACT_CLONE = "CLONE";

    /**
     * The cachedObjects.
     */
    private static Map<String, SemanticObject> cachedObjects = new ConcurrentHashMap<>();

    /**
     * The m_uris.
     */
    private static ConcurrentHashMap<String, String> m_uris = new ConcurrentHashMap<>();

    /**
     * Cached map containing objects not identified in model.
     */
    private static Map<String, Object> notFoundObjects = new HashMapCache<>(1000);

    /**
     * The has cache flag.
     */
    //TODO: Flag is not changed in code
    private static boolean hasCache = true;

    //No cambian
    /**
     * The ext get methods.
     */
    private static HashMap<String, Method> extGetMethods = new HashMap<>();

    /**
     * The ext set methods.
     */
    private static HashMap<String, Method> extSetMethods = new HashMap<>();

    /**
     * The wrapper to primitive.
     */
    private static HashMap<Class, Class> wrapperToPrimitive = new HashMap<>();

    static {
        wrapperToPrimitive.put(Boolean.class, Boolean.TYPE);
        wrapperToPrimitive.put(Byte.class, Byte.TYPE);
        wrapperToPrimitive.put(Short.class, Short.TYPE);
        wrapperToPrimitive.put(Character.class, Character.TYPE);
        wrapperToPrimitive.put(Integer.class, Integer.TYPE);
        wrapperToPrimitive.put(Long.class, Long.TYPE);
        wrapperToPrimitive.put(Float.class, Float.TYPE);
        wrapperToPrimitive.put(Double.class, Double.TYPE);
    }

    /**
     * The genericObject.
     */
    private GenericObject genericObject = null;

    /**
     * The Resource of this SemanticObject.
     */
    private Resource objectResource = null;

    /**
     * The SemanticModel of this SemanticObject.
     */
    private SemanticModel objectModel = null;

    /**
     * The SemanticClass of this SemanticObject.
     */
    private SemanticClass objectClass = null;

    /**
     * The virtual flag.
     */
    private boolean virtual = false;

    private List<Statement> properties = new CopyOnWriteArrayList<>();
    private List<Statement> inverseProperties = null;

    /**
     * Properties cache as map for Javascript engine transformations.
     */
    private HashMap<String, Object> propertyMap = null;

    /**
     * The lastaccess.
     */
    private long lastaccess = System.currentTimeMillis();

    /**
     * The m_cachepropsrel.
     */
    private Map<String, Object> m_cachepropsrel = new ConcurrentHashMap<>();                    //Cache de objetos relacionados a la propiedad


    /**
     * Constructor. Creates a new Instance of a {@link SemanticObject} tagging it as virtual.
     * This method must be used when code generation is not required on SWB OWL classes or properties.
     */
    public SemanticObject() {
        this.virtual = true;
    }

    /**
     * Constructor. Creates a new {@link SemanticObject} tagging it as virtual.
     * This method must be used when code generation is not required on SWB OWL classes or properties.
     *
     * @param smodel {@link SemanticModel} where the new {@link SemanticObject} should be created.
     * @param scls type of the new {@link SemanticObject}
     */
    public SemanticObject(SemanticModel smodel, SemanticClass scls) {
        this.objectModel = smodel;
        this.objectClass = scls;
        this.virtual = true;
        this.objectResource = smodel.getRDFModel().createResource();
        this.properties.add(smodel.getRDFModel().createStatement(objectResource, RDF.type, scls.getOntClass()));
    }

    /**
     * Constructor. Creates a new {@link SemanticObject}.
     * @param smodel {@link SemanticModel} where the new {@link SemanticObject} should be created
     * @param res Resource to create SemanticObject from
     * @param scls type of the new {@link SemanticObject}
     */
    public SemanticObject(SemanticModel smodel, Resource res, SemanticClass scls) {
        this.objectModel = smodel;
        this.objectResource = res;
        this.objectClass = scls;
        this.properties.add(smodel.getRDFModel().createStatement(res, RDF.type, scls.getOntClass()));

        if (res != null) {
            notFoundObjects.remove(res.getURI());
        }
    }

    /**
     * Constructor. Creates a new {@link SemanticObject}.
     * @param smodel {@link SemanticModel} where the new {@link SemanticObject} should be created.
     * @param res Resource to create SemanticObject from
     * @param statements Iterator with statements to create the SemanticObject
     */
    public SemanticObject(SemanticModel smodel, Resource res, Iterator<Statement> statements) {
        this.objectModel = smodel;
        this.objectResource = res;
        init(statements);
    }

    /**
     * Loads statements and initializes SemanticObject information.
     * @param stit statements iterator
     */
    private void init(Iterator<Statement> stit) {
        while (stit.hasNext()) {
            Statement st = stit.next();
            properties.add(0, st);

            //Load object class from rdf:type value
            if (st.getPredicate().equals(RDF.type) && (objectClass == null || !objectClass.isSWBClass())) {
                objectClass = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticClass(st.getResource().getURI());
            }
        }

        if (stit instanceof StmtIterator) {
            ((StmtIterator) stit).close();
        }
        propertyMap = null;
    }

    /**
     * Loads inverse properties for the SemanticObject
     * @param statements statement iterator.
     */
    private void initInverse(StmtIterator statements) {
        while (statements.hasNext()) {
            Statement st = statements.next();
            inverseProperties.add(st);
        }
        statements.close();
    }

    /**
     * @deprecated for naming conventions. Use {@link #reloadProperties()}
     * Reloads SemanticObject properties.
     */
    @Deprecated
    public void reloadProps() {
        reloadProperties();
    }

    /**
     * Reloads SemanticObject properties.
     */
    public void reloadProperties() {
        if (objectResource != null) {
            properties.clear();
            m_cachepropsrel.clear();
            init(objectResource.listProperties());
        }
    }

    /**
     * @deprecated for naming conventions. Use {@link #reloadInverseProperties()}
     * Reloads inverse properties of SemanticObject.
     */
    @Deprecated
    public void reloadInvProps() {
        reloadInverseProperties();
    }

    /**
     * Reloads inverse properties of SemanticObject.
     */
    public void reloadInverseProperties() {
        if (inverseProperties == null) {
            inverseProperties = Collections.synchronizedList(new ArrayList<>());
        } else {
            inverseProperties.clear();
        }

        if (objectResource != null) {
            initInverse(objectModel.getRDFModel().listStatements(null, null, objectResource));
            //TODO: Revisar inversas de otros modelos
        }
    }

    /**
     * @deprecated for namming conventions. Use {@link #clearInverseProperties()}
     * Clears inverse properties of SemanticObject.
     */
    @Deprecated
    public void clearInvProps() {
        inverseProperties = null;
    }

    /**
     * Clears inverse properties of SemanticObject.
     */
    public void clearInverseProperties() {
        inverseProperties = null;
    }

    /**
     * @deprecated for naming conventions. Use {@link #getProperties()}
     * @return properties.
     */
    @Deprecated
    private List<Statement> getProps() {
        return properties;
    }

    /**
     * Gets a list of the SemanticObject's properties.
     * @return SemanticObject's properties
     */
    private List<Statement> getProperties() {
        return properties;
    }

    /**
     * @deprecated for naming conventions. Use {@link #getInverseProperties()}
     * @return properties.
     */

    @Deprecated
    private List<Statement> getPropsInv() {
        return getInverseProperties();
    }

    /**
     * Gets a list of the SemanticObject's inverse properties.
     * @return SemanticObject's properties
     */
    private List<Statement> getInverseProperties() {
        if (inverseProperties == null) {
            synchronized (this) {
                if (inverseProperties == null) {
                    reloadInverseProperties();
                }
            }
        }
        return inverseProperties;
    }

    /**
     * Adds a statement to a property map
     * @param statement statement
     * @param map map
     */
    private void addStatementToPropertyMap(Statement statement, Map map) {
        if (map != null) {
            Property rdfprop = statement.getPredicate();
            SemanticProperty prop = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(rdfprop);

            String name = prop.getPropertyCodeName();
            if (prop.getCardinality() == 1) {
                if (prop.isDataTypeProperty()) {
                    Literal lit = statement.getLiteral();
                    if (lit != null) {
                        map.put(name, lit.getValue());
                    }
                } else {
                    String value = statement.getObject().toString();
                    if (value != null) {
                        map.put(name, value);
                    }
                }
            } else {
                ArrayList arr = (ArrayList) map.get(name);
                if (arr == null) {
                    arr = new ArrayList();
                    map.put(name, arr);
                }

                if (prop.isDataTypeProperty()) {
                    Literal lit = statement.getLiteral();
                    if (lit != null) {
                        arr.add(lit.getValue());
                    }
                } else {
                    String value = statement.getObject().toString();
                    if (value != null) {
                        arr.add(value);
                    }
                }
            }
        }
    }

    /**
     * @deprecated for naming conventions. Use {@link #removeStatementFromPropertyMap(Statement, Map)}
     * @param statement
     * @param map
     */
    @Deprecated
    private void removeStatementToPropertyMap(Statement statement, Map map) {
        removeStatementFromPropertyMap(statement, map);
    }

    /**
     * Removes a statement from a property map
     * @param statement statement
     * @param map map
     */
    private void removeStatementFromPropertyMap(Statement statement, Map map) {
        if (map != null) {
            Property rdfprop = statement.getPredicate();
            SemanticProperty prop = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(rdfprop);

            String name = prop.getName();
            if (prop.getCardinality() == 1) {
                map.remove(name);
            } else {
                ArrayList arr = (ArrayList) map.get(name);
                if (arr != null) {
                    if (prop.isDataTypeProperty()) {
                        Literal lit = statement.getLiteral();
                        if (lit != null) {
                            arr.remove(lit.getValue());
                        }
                    } else {
                        String value = statement.getObject().toString();
                        if (value != null) {
                            arr.remove(value);
                        }
                    }
                }
            }
        }
    }

    /***
     * Gets SemanticObject properties as a map
     * @return property map
     */
    public Map asPropertyMap() {
        if (propertyMap == null) {
            synchronized (this) {
                if (propertyMap == null) {
                    propertyMap = new HashMap<>();
                    for (Statement statement: properties) {
                        addStatementToPropertyMap(statement, propertyMap);
                    }
                }
            }
        }
        return propertyMap;
    }

    /*********************************************** statics ****************************************************************/

    /**
     * Removes an URI from the list of not identified objects.
     * @param uri the URI.
     */
    public static void clearNotFoundURI(String uri) {
        notFoundObjects.remove(uri);
    }

    /**
     * Loads all statements of a model from the Triple Store to memory.
     */
    public static void loadFullCache(SemanticModel model) {
        //TODO: Refactor method to avoid setting of private fields of SemanticObject to prevent a bad practice
        HashMap<String, SemanticObject> map = new HashMap<>();
        StmtIterator it = model.getRDFModel().listStatements();

        while (it.hasNext()) {
            Statement st = it.next();
            Resource subj = st.getSubject();
            Property prop = st.getPredicate();
            RDFNode obj = st.getObject();

            String uri = subj.getURI();
            if (uri != null) {
                SemanticObject sobj = map.get(uri);
                if (sobj == null) {
                    sobj = new SemanticObject();
                    sobj.objectResource = subj;
                    sobj.virtual = false;
                    sobj.objectModel = model;
                    map.put(uri, sobj);
                }
                sobj.properties.add(0, st);

                if (prop.equals(RDF.type) && (sobj.objectClass == null || !sobj.objectClass.isSWBClass())) {
                    sobj.objectClass = SWBPlatform.getSemanticMgr()
                            .getVocabulary().getSemanticClass(obj.asResource().getURI());
                }
            }

            //Inversa
            if (obj.isResource()) {
                uri = obj.asResource().getURI();
                if (uri != null) {
                    SemanticObject sobj = map.get(uri);
                    if (sobj == null) {
                        sobj = new SemanticObject();
                        sobj.objectResource = obj.asResource();
                        sobj.virtual = false;
                        sobj.objectModel = model;
                        map.put(uri, sobj);
                    }
                    if (sobj.inverseProperties == null) {
                        sobj.inverseProperties = Collections.synchronizedList(new ArrayList<>());
                    }
                    sobj.inverseProperties.add(st);
                }
            }
        }

        for (SemanticObject semanticObject : map.values()) {
            if (!semanticObject.properties.isEmpty()) {
                addSemanticObjectToCache(semanticObject);
                if (semanticObject.inverseProperties == null) {
                    semanticObject.inverseProperties = Collections.synchronizedList(new ArrayList<>());
                }
                semanticObject.propertyMap = null;
            }
        }
    }


    /**
     * Gets a {@link SemanticObject} from cached SemanticObject map.
     * @param uri URI of {@link SemanticObject}
     * @return SemanticObject
     */
    public static SemanticObject getSemanticObjectFromCache(String uri) {
        if (hasCache && null != uri) {
            return cachedObjects.get(uri);
        }
        return null;
    }

    /**
     * Gets a {@link SemanticObject} with the specified <code>uri</code>.
     * @param uri the URI
     * @return SemanticObject or null if no object exists with <code>uri</code>
     */
    public static SemanticObject getSemanticObject(String uri) {
        return createSemanticObject(uri, null);
    }


    /**
     * Creates a {@link SemanticObject} with the specified <code>uri</code>. The model where
     * SemanticObject is created is extracted from <code>uri</code>.
     *
     * @param uri the URI
     * @return SemanticObject or null if object is not created.
     */
    public static SemanticObject createSemanticObject(String uri) {
        return createSemanticObject(uri, null);
    }

    /**
     * Creates a {@link SemanticObject} using the specified <code>resource</code>. The model where
     * SemanticObject is created is extracted from <code>resource</code> statements.
     *
     * @param resource the {@link Resource}
     * @return SemanticObject or null if object is not created.
     */
    public static SemanticObject createSemanticObject(Resource resource) {
        if (resource != null) {
            String uri = resource.getURI();
            if (uri != null) {
                return createSemanticObject(uri);
            } else {
                //Anonimous nodes
                SemanticModel smodel = SWBPlatform.getSemanticMgr().getModel(resource.getModel());
                StmtIterator stit = resource.listProperties();
                if (stit.hasNext()) {
                    return new SemanticObject(smodel, resource, stit);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Creates a {@link SemanticObject} with the specified <code>uri</code> on the specified <code>smodel</code>.
     *
     * @param uri the URI of the new SemanticObject
     * @param smodel {@link SemanticModel} where the new SemanticObject is to be created.
     * @return SemanticObject or null if object is not created.
     */
    public static SemanticObject createSemanticObject(String uri, SemanticModel smodel) {
        if (null == uri) {
            return null;
        }

        SemanticObject ret = getSemanticObjectFromCache(uri);
        if (ret == null && !notFoundObjects.containsKey(uri)) {
            String i_uri = m_uris.putIfAbsent(uri, uri);
            if (i_uri == null) i_uri = uri;

            synchronized (i_uri) {
                ret = getSemanticObjectFromCache(uri);
                if (ret == null && !notFoundObjects.containsKey(uri)) {

                    if (smodel != null) {
                        Resource res = smodel.getRDFModel().getResource(uri);
                        StmtIterator stit = res.listProperties();
                        if (stit.hasNext()) {
                            ret = new SemanticObject(smodel, res, stit);
                        } else {
                            ret = null;
                        }
                    } else {
                        int i = uri.indexOf('#');
                        if (i == -1) {
                            i = uri.lastIndexOf('/');
                        }
                        if (i > 0) {
                            String base = uri.substring(0, i + 1);
                            LOG.trace("getResource in Model(1):" + uri + " " + base);
                            SemanticModel model = SWBPlatform.getSemanticMgr().getModelByNS(base);
                            if (model != null) {
                                Resource res = model.getRDFModel().getResource(uri);
                                StmtIterator stit = res.listProperties();
                                if (stit.hasNext()) {
                                    ret = new SemanticObject(model, res, stit);
                                } else {
                                    notFoundObjects.put(uri, uri);
                                    return null;
                                }
                            }
                        }

                        if (ret == null) {
                            LOG.trace("getResource in Schema(2):");
                            Model bmodel = SWBPlatform.getSemanticMgr().getSchema().getRDFOntModel();
                            Resource res = bmodel.getResource(uri);
                            SemanticModel model = SWBPlatform.getSemanticMgr().getModel(res.getModel());
                            StmtIterator stit = res.listProperties();
                            if (stit.hasNext()) {
                                ret = new SemanticObject(model, res, stit);
                            }
                        }

                        if (ret == null) {
                            LOG.trace("getResource in All Model(3):");

                            for (Map.Entry<String, SemanticModel> ent : SWBPlatform.getSemanticMgr().getModels()) {
                                SemanticModel model = ent.getValue();
                                Resource res = model.getRDFModel().getResource(uri);
                                StmtIterator stit = res.listProperties();
                                if (stit.hasNext()) {
                                    ret = new SemanticObject(model, res, stit);
                                    break;
                                }
                            }
                        }
                    }
                    addSemanticObjectToCache(ret);
                }
                if (ret == null) {
                    notFoundObjects.put(uri, uri);
                }
            }
        }
        return ret;
    }

    /**
     * @deprecated for naming conventions. Use {@link #addSemanticObjectToCache(SemanticObject)}
     * Adds a {@link SemanticObject} to the cached list.
     * @param obj SemanticObject
     */
    @Deprecated
    protected static void cacheSemanticObject(SemanticObject obj) {
        addSemanticObjectToCache(obj);
    }

    /**
     * Adds a {@link SemanticObject} to the cached list.
     * @param obj SemanticObject
     */
    protected static void addSemanticObjectToCache(SemanticObject obj) {
        //TODO: Validar si puede agregarse a cache
        if (hasCache && obj != null && obj.getURI() != null) {
            cachedObjects.put(obj.getURI(), obj);
        }
    }

    /**
     * @deprecated for naming conventions. Use {@link #removeSemanticObjectFromCache(String)}
     * Removes a SemanticObject with URI equals to <code>uri</code> from object cache.
     * @param uri SemanticObject URI.
     */
    @Deprecated
    public static void removeCache(String uri) {
        removeSemanticObjectFromCache(uri);
    }

    /**
     * Removes a SemanticObject with URI equals to <code>uri</code> from object cache.
     * @param uri SemanticObject URI.
     */
    public static void removeSemanticObjectFromCache(String uri) {
        cachedObjects.remove(uri);
    }


    /**
     * Clears SemanticObject cache.
     */
    public static void clearCache() {
        cachedObjects.clear();
        notFoundObjects.clear();
    }

    /**
     * Gets the number of cached objects.
     * @return number of cached objects.
     */
    public static int getCacheSize() {
        return cachedObjects.size();
    }

    /**
     * @deprecated Use {@link SemanticMgr#shortToFullURI(String).
     */
    @Deprecated
    public static String shortToFullURI(String shorturi) {
        return SemanticMgr.shortToFullURI(shorturi);
    }

    /*********************************************** instance ****************************************************************/

    /**
     * Gets the SemanticObject's URI.
     * @return the URI
     */
    public String getURI() {
        if (objectResource != null) {
            return objectResource.getURI();
        }
        return null;
    }

    /**
     * Gets SemanticObject's short URI
     * @return short URI
     */
    public String getShortURI() {
        if (getURI() == null) {
            throw new IllegalArgumentException();
        }
        String suri = getModel().getModelObject().getId();
        int pos = getURI().indexOf('#');

        if (pos != -1) {
            suri = suri + ":" + getURI().substring(pos + 1);
        } else {
            throw new IllegalArgumentException();
        }
        return suri;
    }

    /**
     * @deprecated for naming conventions. Use {@link #getResourceId()}.
     *
     * @return String with the format prefix:localname
     */
    @Deprecated
    public String getResId() {
        return getResourceId();
    }

    /**
     * Gets the resource ID of the SemanticObject
     * @return Resource ID with format prefix:localname
     */
    public String getResourceId() {
        if (objectResource == null) {
            return null;
        }

        String ret;
        String pref = null;
        try {
            pref = getPrefix();
        } catch (Exception e) {
            LOG.error("Error getting prefix of:" + this.getURI());
        }

        if (pref != null) {
            ret = pref + ":" + objectResource.getLocalName();
        } else {
            ret = objectResource.getLocalName();
        }

        if (ret == null || ret.length() == 0) {
            ret = getId();
        }

        if (ret == null) {
            ret = objectResource.toString();
        }
        return ret;
    }

    /**
     * Gets the SemanticObject ID.
     * @return the ID
     */
    public String getId() {
        String id = getURI();
        if (id != null) {
            int x = id.indexOf('#');
            if (x > -1) {
                id = id.substring(x + 1);
                x = id.indexOf(':');
                if (x > -1) {
                    id = id.substring(x + 1);
                }
            }
        }
        return id;
    }

    /**
     * Gets the encoded URI of the SemanticObject.
     * @return Encoded URI.
     */
    public String getEncodedURI() {
        return URLEncoder.encode(getURI());
    }

    /**
     * Gets the RDF name of this SemanticObject.
     * @return the RDF name
     */
    public String getRDFName() {
        if (objectResource != null) {
            return objectResource.getLocalName();
        }
        return null;
    }

    /**
     * Gets the SemanticObject's prefix.
     * @return the prefix
     */
    public String getPrefix() {
        if (objectResource != null && objectResource.getNameSpace() != null) {
            return objectResource.getModel().getNsURIPrefix(objectResource.getNameSpace());
        }
        return null;
    }

    /**
     * Gets the SemanticObject's {@link SemanticModel}.
     * @return SemanticModel
     */
    public SemanticModel getModel() {
        return objectModel;
    }

    /**
     * Gets the RDF resource of this SemanticObject.
     * @return Resource.
     */
    public Resource getRDFResource() {
        return objectResource;
    }

    /**
     * Gets the {@link SemanticClass} of this {@link SemanticObject}.
     * @return addSemanticClass
     */
    public SemanticClass getSemanticClass() {
        if (objectClass == null) {
            List<Statement> stmts = getProperties();
            for(Statement st : stmts) {
                if (st.getPredicate().equals(RDF.type) && (objectClass == null || !objectClass.isSWBClass())) {
                    objectClass = SWBPlatform.getSemanticMgr()
                            .getVocabulary().getSemanticClass(st.getResource().getURI());
                }
            }
        }
        return objectClass;
    }

    /**
     * Gets an iterator of {@link SemanticClass}es set as value of rdf:type property of this SemanticObject.
     * @return Iterator of {@link SemanticClass}
     */
    public Iterator<SemanticClass> listSemanticClasses() {
        return new SemanticClassIterator<SemanticClass>(listProperties(RDF.type));
    }

    /**
     * Adds a resource to the {@link SemanticObject} to set a rdf:type property with <code>cls</code> as value.
     * @param cls {@link SemanticClass}
     */
    public void addSemanticClass(SemanticClass cls) {
        SWBPlatform.getSemanticMgr().notifyChange(this, cls, null, ACT_ADD);
        if (objectResource != null) {
            addResource(RDF.type, cls.getOntClass());
            reloadProperties();
        }
    }

    /**
     * Removes a resource to the {@link SemanticObject} to remove a rdf:type property with <code>cls</code> as value.
     * @param cls {@link SemanticClass}
     * @return the SemanticObject
     */
    public SemanticObject removeSemanticClass(SemanticClass cls) {
        SWBPlatform.getSemanticMgr().notifyChange(this, cls, null, ACT_REMOVE);
        if (objectResource != null) {
            Iterator<Statement> stit = listProperties(RDF.type);
            while (stit.hasNext()) {
                Statement staux = stit.next();
                if (staux.getResource().getURI().equals(cls.getURI())) {
                    removeStatement(staux);
                }
            }
            reloadProperties();
        }
        return this;
    }

    /**
     * Sets the RDF resource for the SemanticObject.
     * @param res the new RDF resource
     */
    public void setRDFResource(Resource res) {
        objectResource = res;
        objectModel = SWBPlatform.getSemanticMgr().getModel(res.getModel());
        reloadProperties();
        clearInverseProperties();
    }

    /**
     * Gets the virtual property.
     * @return virtual property.
     */
    public boolean isVirtual() {
        return virtual;
    }

    /**
     * Gets last access to this SemanticObject.
     * @return last acces time in milliseconds.
     */
    public long getLastAccess() {
        return lastaccess;
    }

    /**
     * Gets cached {@link GenericObject} of this SemanticObject.
     * @return GenericObject.
     */
    public GenericObject getGenericInstanceFromCache() {
        lastaccess = System.currentTimeMillis();
        return genericObject;
    }

    /**
     * Gets or creates the {@link GenericObject} of this SemanticObject.
     * @return GenericObject.
     */
    public GenericObject getGenericInstance() {
        return createGenericInstance();
    }

    /**
     * Sets the {@link GenericObject} of this SemanticObject.
     * @param gen GenericObject
     */
    private void setGenericInstance(GenericObject gen) {
        genericObject = gen;
    }

    /**
     * Creates the {@link GenericObject} of this SemanticObject.
     * @return GenericObject.
     */
    public GenericObject createGenericInstance() {
        GenericObject gen = getGenericInstanceFromCache();
        if (gen == null) {
            synchronized (this) {
                gen = getGenericInstanceFromCache();
                if (gen == null) {
                    gen = createNewGenericIntance();
                    if (gen != null) {
                        setGenericInstance(gen);
                    }
                }
            }
        }
        return gen;
    }

    /**
     * @deprecated for namming conventions. Use {@link #createNewGenericInstance()}
     * Crea una nueva instancia del GenericObject asociado
     *
     * @return
     */
    @Deprecated
    public GenericObject createNewGenericIntance() {
        return createNewGenericInstance();
    }

    /**
     * Creates a new {@link GenericObject} from this SemanticObject.
     * @return GenericObject.
     */
    public GenericObject createNewGenericInstance() {
        GenericObject gen = null;

        SemanticClass cls = getSemanticClass();
        if (cls == null) {
            printStatements();
            LOG.error("SemanticObject(" + this + ") without SemanticClass...", new Exception());
        } else {
            if (cls.isSWBInterface()) {
                Iterator<SemanticClass> classes = listSemanticClasses();
                while (classes.hasNext()) {
                    SemanticClass tempCls = classes.next();
                    if (tempCls.isSWBClass()) {
                        cls = tempCls;
                        break;
                    }
                }
            }
            gen = cls.construcGenericInstance(this);
        }
        return gen;
    }

    /************************************************************** RDF BASE ***********************************************************/

    /**
     * Gets an iterator of {@link Statement}s where statement predicate is <code>prop</code>.
     * @param prop Property to find.
     * @return Iterator of statements.
     */
    public Iterator<Statement> listProperties(Property prop) {
        return listProperties(prop, false);
    }

    /**
     * Gets an iterator of {@link Statement}s where statement predicate is <code>prop</code>.
     * @param prop Property to find.
     * @param inverse whether to get inverse properties.
     * @return Iterator of statements.
     */
    private Iterator<Statement> listProperties(Property prop, boolean inverse) {
        ArrayList<Statement> ret = new ArrayList<>();
        List<Statement> stmts;

        if (inverse) {
            stmts = getInverseProperties();
        } else {
            stmts = getProperties();
        }

        for (Statement statement : stmts) {
            if (statement.getPredicate().equals(prop)) {
                ret.add(statement);
            }
        }

        return ret.iterator();
    }

    /**
     * Logs {@link SemanticObject} statements as warning.
     */
    public void printStatements() {
        StringBuilder ret = new StringBuilder("---PrintStatements---\n");
        for (Statement statement : getProperties()) {
            ret.append(statement).append("\n");
        }

        for (Statement statement : getInverseProperties()) {
            ret.append(statement).append("\n");
        }
        ret.append("\n");
        LOG.warn(ret.toString());
    }

    /**
     * Gets first statement involving <code>prop</code>.
     * @param prop property to find.
     * @return Statement
     */
    private Statement getProperty(Property prop) {
        Iterator<Statement> it = listProperties(prop);
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    /**
     * Removes all statements related to this SemanticObject.
     * @param external whether SemanticObject belongs to a remote model.
     */
    protected void remove(boolean external) {
        if (!virtual && !external) {
            getModel().getRDFModel().removeAll(getRDFResource(), null, null);
            SWBPlatform.getSemanticMgr().notifyTSChange(this, null, ACT_REMOVE);
        }
        removeSemanticObjectFromCache(getRDFResource().getURI());
    }

    /**
     * Removes all statements related to the Property <code>prop</code> in this SemanticObject.
     * @param prop the SemanticProperty
     */
    private void removeProperty(Property prop) {
        Object [] stmts = getProperties().toArray();
        for (int x = 0; x < stmts.length; x++) {
            Statement statement = (Statement) stmts[x];
            if (statement.getPredicate().equals(prop)) {
                removeStatement(statement);
                removeStatementFromPropertyMap(statement, propertyMap);
            }
        }
    }

    /**
     * Removes a statement from the SemanticObject's model.
     * @param stmt {@link Statement}
     * @return true if statement was removed.
     */
    private boolean removeStatement(Statement stmt) {
        return removeStatement(stmt, false);
    }

    /**
     * @deprecated for namming conventions. Use {@link #removeStatement(Statement, boolean)}
     * @param stmt
     * @param external
     * @return
     */
    @Deprecated
    protected boolean remove(Statement stmt, boolean external) {
        return remove(stmt, external);
    }

    /**
     * Removes a statement from the SemanticObject's model.
     * @param stmt {@link Statement}
     * @param external whether the statement belongs to a remote model.
     * @return true if statement was removed.
     */
    protected boolean removeStatement(Statement stmt, boolean external) {
        boolean ret;
        if (external) {
            Object [] stmts = getProperties().toArray();
            for (int x = 0; x < stmts.length; x++) {
                Statement statement = (Statement) stmts[x];
                if (statement.getPredicate().equals(stmt.getPredicate()) &&
                        statement.getObject().equals(stmt.getObject())) {
                    stmt = statement;
                    break;
                }
            }
        }

        ret = getProperties().remove(stmt);
        removeStatementFromPropertyMap(stmt, propertyMap);

        if (ret) {
            if (!virtual && !external) {
                objectModel.getRDFModel().remove(stmt);
                //Notify external.
                SWBPlatform.getSemanticMgr().notifyTSChange(this, stmt, ACT_REMOVE);
            }

            //Eliminar cache inversos
            RDFNode node = stmt.getObject();
            if (node != null && node.isResource()) {
                Resource res = node.asResource();
                if (res != null && res.getURI() != null) {
                    Property iprop = stmt.getPredicate();
                    SemanticProperty prop = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(iprop);

                    if (prop.isInverseOf()) {
                        SemanticObject sobj = getSemanticObjectFromCache(res.getURI());
                        if (sobj != null) sobj.removeInverseProperty(stmt);
                    }
                }
            }

            m_cachepropsrel.remove(stmt.getPredicate().getURI());
        }
        return ret;
    }

    /**
     * Adds a {@link Statement} to the SemanticObject setting property value to <code>prop</code>
     * and object value to <code>obj</code>.
     *
     * @param prop Property of the resource.
     * @param obj Object of the resource.
     * @return Statement.
     */
    private Statement addResource(Property prop, Resource obj) {
        Model m = objectModel.getRDFModel();
        Statement stmt = m.createStatement(objectResource, prop, obj);
        addStatement(stmt);
        return stmt;
    }

    /**
     * Adds a {@link Statement} to the SemanticObject setting property value to <code>prop</code>
     * and object value to a typed Literal based on <code>obj</code>.
     *
     * @param prop Property of the resource.
     * @param obj Object of the resource.
     * @return Statement.
     */
    private Statement addLiteral(Property prop, Object obj) {
        Model m = objectModel.getRDFModel();
        Statement stmt = m.createStatement(objectResource, prop, m.createTypedLiteral(obj));
        addStatement(stmt);
        return stmt;
    }

    /**
     * Adds a {@link Statement} to the SemanticObject setting property value to <code>prop</code>
     * and value of <code>obj</code> to an untyped literal tagged with @<code>lang</code>.
     *
     * @param prop Property of the resource.
     * @param obj Object of the resource.
     * @param lang Language of typed literal.
     * @return Statement.
     */
    private Statement addLiteral(Property prop, String obj, String lang) {
        Model m = objectModel.getRDFModel();
        Statement stmt = m.createStatement(objectResource, prop, m.createLiteral(obj, lang));
        addStatement(stmt);
        return stmt;
    }

    /**
     * Adds a {@link Statement} to the SemanticObject setting property value to <code>prop</code>
     * and value of <code>obj</code> to an untyped literal.
     *
     * @param prop Property of the resource.
     * @param obj Object of the resource.
     * @return Statement.
     */
    private Statement addLiteral(Property prop, String obj) {
        Model m = objectModel.getRDFModel();
        Statement stmt = m.createStatement(objectResource, prop, m.createLiteral(obj));
        addStatement(stmt);
        return stmt;
    }

    /**
     * Adds a {@link Statement} to the SemanticObject's model.
     * @param stmt Statement.
     */
    private void addStatement(Statement stmt) {
        addStatement(stmt, false);
    }

    /**
     * Adds a {@link Statement} to the SemanticObject's model.
     * @param stmt Statement.
     * @param external whether this SemanticObject belongs to a remote model.
     */
    protected void addStatement(Statement stmt, boolean external) {
        boolean contains = false;
        if (external) {
            Object [] stmts = getProperties().toArray();
            for (int x = 0; x < stmts.length; x++) {
                Statement statement = (Statement) stmts[x];
                if (statement.getPredicate().equals(stmt.getPredicate()) &&
                        statement.getObject().equals(stmt.getObject())) {
                    stmt = statement;
                    contains = true;
                }
            }
        }

        if (!virtual && !external) {
            Model m = objectModel.getRDFModel();
            m.add(stmt);
            SWBPlatform.getSemanticMgr().notifyTSChange(this, stmt, ACT_ADD);
        }

        if (!contains) {
            getProperties().add(stmt);
            addStatementToPropertyMap(stmt, propertyMap);
        }

        //Agrega inversas
        RDFNode node = stmt.getObject();
        if (node != null && node.isResource()) {
            Resource res = node.asResource();
            if (res != null && res.getURI() != null) {
                Property iprop = stmt.getPredicate();
                SemanticProperty prop = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(iprop);

                if (prop.isInverseOf()) {
                    SemanticObject sobj = getSemanticObjectFromCache(res.getURI());
                    if (sobj != null && !sobj.getInverseProperties().contains(stmt)) {
                        sobj.addInverseProperty(stmt);
                    }
                }
            }
        }

        m_cachepropsrel.remove(stmt.getPredicate().getURI());
    }

/************************************************************** RDF Inv ***********************************************************/

    /**
     * @deprecated for naming conventions. Use {@link #listInverseProperties(Property)}
     * @param prop
     * @return
     */
    @Deprecated
    public Iterator<Statement> listInvProperties(Property prop) {
        return listInverseProperties(prop);
    }

    /**
     * Gets an iterator of {@link Statement}s containing <code>prop</code> as predicate.
     * @param prop Property of the resource.
     * @return Iterator of Statements
     */
    public Iterator<Statement> listInverseProperties(Property prop) {
        ArrayList<Statement> ret = new ArrayList<>();
        List<Statement> stmts = getInverseProperties();

        for (Statement statement : stmts) {
            if (statement.getPredicate().equals(prop)) {
                ret.add(statement);
            }
        }
        return ret.iterator();
    }


    /**
     * @deprecated for naming conventions. Use {@link #getInverseProperty(Property)}
     * @param prop
     * @return
     */
    @Deprecated
    private Statement getInvProperty(Property prop) {
        return getInverseProperty(prop);
    }

    /**
     * Gets the specified inverse property.
     * @param prop Property
     * @return Property or null if it does not exists.
     */
    private Statement getInverseProperty(Property prop) {
        Iterator<Statement> it = listInverseProperties(prop);
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    /**
     * @deprecated for naming conventions. Use {@link #removeInverseProperty(Statement)}
     * Elimina el Statement del cache de inversas
     *
     * @param stmt
     * @return
     */
    @Deprecated
    protected boolean removeInv(Statement stmt) {
        return removeInverseProperty(stmt);
    }

    /**
     * Removes a statement from the inverse properties of the SemanticObject.
     * @param stmt Statement.
     * @return true if statement was removed.
     */
    protected boolean removeInverseProperty(Statement stmt) {
        return getInverseProperties().remove(stmt);
    }

    /**
     * @deprecated for namming conventions. Use {@link #removeInverseProperty(Property)}
     * @param prop Property.
     */
    @Deprecated
    private void removeInv(Property prop) {
        removeInverseProperty(prop);
    }

    /**
     * Removes a {@link Property} from the inverse properties of the SemanticObject.
     * @param prop Property to remove.
     */
    private void removeInverseProperty(Property prop) {
        Object [] stmts = getInverseProperties().toArray();
        for (int x = 0; x < stmts.length; x++) {
            Statement statement = (Statement) stmts[x];
            if (statement.getPredicate().equals(prop)) {
                removeInverseProperty(statement);
            }
        }
    }

    /**
     * Adds an inverse property statement.
     * @param stmt Statement.
     */
    private void addInverseProperty(Statement stmt) {
        getInverseProperties().add(stmt);
    }


/************************************************************** RDF End ***********************************************************/

    @Override
    public String toString() {
        if (objectResource == null) {
            return super.toString();
        }
        return objectResource.toString();
    }

    @Override
    public int hashCode() {
        if (objectResource == null) {
            return super.hashCode();
        }
        return objectResource.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }

    /**
     * Checks whether the SemanticObject is an instance of <code>cls</code>.
     * @param cls the {@link SemanticClass}
     * @return true if SemanticObject is an instance of <code>cls</code>.
     */
    public boolean instanceOf(SemanticClass cls) {
        SemanticClass cl = getSemanticClass();
        return cl != null && (cl.equals(cls) || cl.isSubClass(cls));
    }

/***********************************************************************************************************************/

    /**
     * Gets the <code>prop</code> property as a DOM Document parsing its value.
     * This method expects value of <code>prop</code> to be a well formed XML String.
     * @param prop the property
     * @return Document representation of property value.
     */
    public Document getDomProperty(SemanticProperty prop) {
        //TODO: Add checkings in case this method is used on non XML properties
        Document dom = (Document) m_cachepropsrel.get(prop.getURI());
        if (dom == null) {
            String xml = getProperty(prop);
            if (xml != null) {
                dom = SWBUtils.XML.xmlToDom(xml);
            }
            if (dom == null) {
                dom = SWBUtils.XML.getNewDocument();
            }
            m_cachepropsrel.put(prop.getURI(), dom);
        }
        return dom;
    }


    /**
     * Gets the value of <code>prop</code> as a Literal.
     * @param prop the property
     * @return the literal value of <code>prop</code>
     */
    public SemanticLiteral getLiteralProperty(SemanticProperty prop) {
        if (objectResource != null) {
            Statement stm = getProperty(prop.getRDFProperty());
            if (stm != null) {
                return new SemanticLiteral(stm);
            }
        }
        return null;
    }

    /**
     * Gets the value of <code>prop</code> as a Literal in the given language.
     * @param prop the property
     * @param lang the literal language
     * @return the literal value of <code>prop</code>
     */
    public SemanticLiteral getLiteralProperty(SemanticProperty prop, String lang) {
        if (objectResource != null) {
            Statement stm = getLocaleStatement(prop, lang);
            if (stm != null) {
                return new SemanticLiteral(stm);
            }
        }
        return null;
    }

    /**
     * Adds a literal value to a <code>prop</code>.
     * @param prop property to set value to.
     * @param literal {@link SemanticLiteral} of value.
     */
    public void addLiteralProperty(SemanticProperty prop, SemanticLiteral literal) {
        setLiteralProperty(prop, literal, false);
    }

    /**
     * Sets the literal value of a <code>prop</code>.
     * @param prop property to set value to.
     * @param literal {@link SemanticLiteral} of value.
     */
    public void setLiteralProperty(SemanticProperty prop, SemanticLiteral literal) {
        setLiteralProperty(prop, literal, true);
    }

    /**
     * Sets or adds a literal value of a <code>prop</code>.
     * @param prop property to set value to.
     * @param literal {@link SemanticLiteral} of value.
     * @param replace whether to replace literal value of <code>prop</code>. When set to false,
     *                new statements are added.
     */
    protected void setLiteralProperty(SemanticProperty prop, SemanticLiteral literal, boolean replace) {
        String action = ACT_ADD;

        if (objectResource != null) {
            Object obj = literal.getValue();
            String lang = literal.getLanguage();

            if (replace) {
                action = ACT_SET;

                if (!prop.isLocaleable()) {
                    removeProperty(prop.getRDFProperty());
                } else {
                    Iterator<Statement> stit = listProperties(prop.getRDFProperty());
                    while (stit.hasNext()) {
                        Statement staux = stit.next();
                        String lg = staux.getLanguage();
                        if (lg != null && lg.isEmpty()) {
                            lg = null;
                        }

                        if ((lang == null && lg == null) || (lg != null && lg.equals(lang))) {
                            removeStatement(staux);
                        }
                    }
                }
            }

            if (obj != null) {
                if (obj instanceof String) {
                    if (lang != null) {
                        addLiteral(prop.getRDFProperty(), (String) obj, literal.getLanguage());
                    } else {
                        addLiteral(prop.getRDFProperty(), (String) obj);
                    }
                } else if (obj instanceof java.util.Date) {
                    addLiteral(prop.getRDFProperty(), SWBUtils.TEXT.iso8601DateFormat((java.util.Date) obj));
                } else {
                    addLiteral(prop.getRDFProperty(), obj);
                }
            }
        }
        SWBPlatform.getSemanticMgr().notifyChange(this, prop, literal.getLanguage(), action);
    }

    /**
     * Removes the <code>prop</code> from the SemanticObject.
     * @param prop {@link SemanticProperty} to remove.
     * @return updated SemanticObject.
     */
    public SemanticObject removeProperty(SemanticProperty prop) {
        SWBPlatform.getSemanticMgr().notifyChange(this, prop, null, ACT_REMOVE);
        if (objectResource != null) {
            try {
                if (prop.isBinary()) {
                    String value = getProperty(prop);
                    if (value != null) {
                        String workPath = this.getWorkPath();
                        if (!(workPath.endsWith("\\") || workPath.equals("/"))) {
                            workPath += "/" + value;
                        }
                        SWBPlatform.createInstance().removeFileFromPlatformWorkPath(workPath);
                    }
                }
            } catch (Exception e) {
                LOG.error(e);
            }

            Property iprop = prop.getRDFProperty();
            removeProperty(iprop);
        }
        return this;
    }

    /**
     * Removes the <code>prop</code> from the SemanticObject with matching <code>lang</code>.
     * @param prop {@link SemanticProperty} to remove.
     * @param lang value language
     * @return updated SemanticObject.
     */
    public SemanticObject removeProperty(SemanticProperty prop, String lang) {
        SWBPlatform.getSemanticMgr().notifyChange(this, prop, lang, ACT_REMOVE);

        if (objectResource != null) {
            Iterator<Statement> stit = listProperties(prop.getRDFProperty());
            while (stit.hasNext()) {
                Statement staux = stit.next();
                String lg = staux.getLanguage();
                if (lg != null && lg.equals(lang)) {
                    removeStatement(staux);
                }
            }
        }
        return this;
    }

    /**
     * Sets the value of <code>object</code> to <code>property</code>.
     * @param prop      property to set value to.
     * @param object    {@link SemanticObject} to set.
     * @return updated SemanticObject.
     */
    public SemanticObject setObjectProperty(SemanticProperty prop, SemanticObject object) {
        if (objectResource != null) {
            Property iprop = prop.getRDFProperty();

            Statement stm = getProperty(iprop);
            if (stm != null) {
                removeStatement(stm);
            }

            if (object != null) {
                addResource(iprop, object.getRDFResource());
            }
            SWBPlatform.getSemanticMgr().notifyChange(this, prop, null, ACT_SET);
        }
        return this;
    }


    /**
     * Adds the value of <code>object</code> to <code>property</code>.
     * @param prop      property to set value to.
     * @param object    {@link SemanticObject} to set.
     * @return updated SemanticObject.
     */
    public SemanticObject addObjectProperty(SemanticProperty prop, SemanticObject object) {
        return addObjectProperty(prop, object, true);
    }


    /**
     * Adds the value of <code>object</code> to <code>property</code>.
     * @param prop      property to set value to.
     * @param object    {@link SemanticObject} to set.
     * @param notify    whether to notify change.
     * @return updated SemanticObject.
     */
    public SemanticObject addObjectProperty(SemanticProperty prop, SemanticObject object, boolean notify) {
        if (objectResource != null) {
            Property iprop = prop.getRDFProperty();
            if (object != null) {
                addResource(iprop, object.getRDFResource());
            }

            if (notify) {
                //TODO: Check why notification language is harcoded to "list"
                SWBPlatform.getSemanticMgr().notifyChange(this, prop, "list", ACT_ADD);
            }
        }
        return this;
    }


    /**
     * Removes the value of <code>object</code> from <code>property</code>.
     * @param prop      property to remove value from.
     * @param object    {@link SemanticObject} to remove.
     * @return updated SemanticObject.
     */
    public SemanticObject removeObjectProperty(SemanticProperty prop, SemanticObject object) {
        return removeObjectProperty(prop, object, true);
    }

    /**
     * Removes the value of <code>object</code> from <code>property</code>.
     * @param prop      property to remove value from.
     * @param object    {@link SemanticObject} to remove.
     * @param notify    whether to notify change.
     * @return updated SemanticObject.
     */
    public SemanticObject removeObjectProperty(SemanticProperty prop, SemanticObject object, boolean notify) {
        if (objectResource != null && object != null) {
            Iterator<Statement> it = listProperties(prop.getRDFProperty());
            while (it.hasNext()) {
                Statement stmt = it.next();
                if (object.getRDFResource().equals(stmt.getResource())) {
                    removeStatement(stmt);
                }
            }

            //TODO: Check why notification language is harcoded to "list"
            if (notify) {
                SWBPlatform.getSemanticMgr().notifyChange(this, prop, "list", ACT_REMOVE);
            }
        }
        return this;
    }


    /**
     * Gets an iterator of the SemanticObject's <code>prop</code> literal values.
     * @param prop the SemanticProperty
     * @return the iterator of SemanticLiteral
     */
    public Iterator<SemanticLiteral> listLiteralProperties(SemanticProperty prop) {
        if (objectResource != null) {
            return new SemanticLiteralIterator(listProperties(prop.getRDFProperty()));
        }
        return new ArrayList<SemanticLiteral>().iterator();
    }

    /**
     * Removes the literal value of <code>lit</code> from <code>property</code>.
     * @param prop  property to remove value from.
     * @param lit   {@link SemanticLiteral} to remove.
     * @return updated SemanticObject.
     */
    public SemanticObject removeLiteralProperty(SemanticProperty prop, SemanticLiteral lit) {
        if (objectResource != null) {
            Iterator<Statement> it = listProperties(prop.getRDFProperty());
            while (it.hasNext()) {
                Statement stmt = it.next();
                if (lit.getValue().equals(stmt.getLiteral().getValue())) {
                    removeStatement(stmt);
                }
            }

            SWBPlatform.getSemanticMgr().notifyChange(this, prop, lit.getLanguage(), ACT_REMOVE);
        }
        return this;
    }

    /**
     * Gets an iterator of {@link SemanticProperty} objects in this SemanticObject.
     * @return Iterator of {@link SemanticProperty}
     */
    public Iterator<SemanticProperty> listProperties() {
        HashSet<SemanticProperty> props = new HashSet<>();
        for (Statement stmt : getProperties()) {
            Property prop = stmt.getPredicate();
            props.add(getModel().getSemanticProperty(prop.getURI()));
        }
        return props.iterator();
    }

    /**
     * Gets an iterator of {@link SemanticObject} with <code>prop</code> values.
     * @param prop the SemanticProperty to find values from.
     * @return the iterator of {@link SemanticObject}
     */
    public Iterator<SemanticObject> listObjectProperties(SemanticProperty prop) {
        if (objectResource != null) {
            if (!prop.hasInverse()) {
                return new SemanticIterator(listProperties(prop.getRDFProperty()));
            } else {
                return new SemanticIterator(listInverseProperties(prop.getInverse().getRDFProperty()), true);
            }
        } else {
            return new ArrayList<SemanticObject>().iterator();
        }
    }

    /**
     * Filters valid objects from an iterator.
     * @param it Iterator
     * @return Iterator with valid objects only.
     */
    private Iterator<SemanticObject> filterValidObjects(Iterator<SemanticObject> it) {
        SemanticClass cls = null;
        SemanticProperty valid = null;

        List<SemanticObject> list = new ArrayList<>();
        while (it.hasNext()) {
            SemanticObject obj = it.next();
            boolean add = true;

            //TODO: Check this condition. Assumes all objects are of the same class
            if (cls == null) {
                cls = obj.getSemanticClass();
                valid = cls.getProperty("valid");
            }


            //TODO: Check this condition. adds objects without a "valid" property set
            if (valid != null && !obj.getBooleanProperty(valid)) {
                add = false;
            }

            if (add) {
                list.add(obj);
            }
        }
        return list.iterator();
    }

    /**
     * Regresa lista de objetos activos y no borrados relacionados por la propiedad
     * Si no encuentra en el objeto busca en los padres.
     *
     * @param prop the prop
     * @return
     */
    public Iterator<SemanticObject> listInheritProperties(SemanticProperty prop) {
        return listInheritProperties(prop, new ArrayList());
    }

    /**
     * Regresa lista de objetos activos y no borrados relacionados por la propiedad
     * Si no encuentra en el objeto busca en los padres.
     *
     * @param prop the prop
     * @return
     */
    private Iterator<SemanticObject> listInheritProperties(SemanticProperty prop, ArrayList arr) {
        if (arr.contains(this)) {
            LOG.error("Error: circular reference:" + prop + " obj:" + this + " " + arr);
            return listObjectProperties(prop);
        } else {
            arr.add(this);
        }

        Iterator it = listObjectProperties(prop);
        if (prop.isInheritProperty()) {
            it = filterValidObjects(it);
            if (!it.hasNext()) {
                //TODO: revisar como definir propirdad de no herencia
                SemanticProperty noinherit = getSemanticClass()
                        .getProperty("notInherit" + prop.getName().substring(3));

                if (noinherit == null || !getBooleanProperty(noinherit)) {
                    SemanticObject parent = getHerarquicalParent();
                    if (parent != null) {
                        it = parent.listInheritProperties(prop, arr);
                    }
                }
            }
        }
        return it;
    }

    /**
     * Regresa lista de objetos activos y no borrados relacionados por la propiedad.
     *
     * @param prop the prop
     * @return
     */
    public Iterator<SemanticObject> listValidObjectProperties(SemanticProperty prop) {
        return filterValidObjects(listObjectProperties(prop));
    }

    /**
     * Check whether this SemanticObject contains the object property <code>prop</code> with
     * value set to <code>obj</code>
     * @param prop {@link SemanticProperty} to check
     * @param obj object value
     * @return true if SemanticObject has property <code>prop</code> with value <code>obj</code>
     */
    public boolean hasObjectProperty(SemanticProperty prop, SemanticObject obj) {
        Iterator<SemanticObject> it = listObjectProperties(prop);
        while (it.hasNext()) {
            SemanticObject so = it.next();
            if (so != null && so.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether this SemanticObject contains the object property <code>prop</code>.
     * @param prop {@link SemanticProperty} to check
     * @return true if SemanticObject has property <code>prop</code>
     */
    public boolean hasObjectProperty(SemanticProperty prop) {
        return getObjectProperty(prop) != null;
    }


    /**
     * Gets value of the <code>prop</code> object property in the SemanticObject.
     * @param prop property to find.
     * @return Value of <code>prop</code>
     */
    public SemanticObject getObjectProperty(SemanticProperty prop) {
        Iterator<SemanticObject> it = listObjectProperties(prop);
        if (it.hasNext()) return it.next();
        return null;
    }

    /**
     * Gets value of the <code>prop</code> object property in the SemanticObject. If property is not set
     * <code>default</code> is returned.
     *
     * @param prop        property to find.
     * @param defValue    default value.
     * @return Value of <code>prop</code> or default value
     */
    public SemanticObject getObjectProperty(SemanticProperty prop, SemanticObject defValue) {
        SemanticObject obj = getObjectProperty(prop);
        if (obj == null) {
            return defValue;
        }
        return obj;
    }


    /**
     * Gets a localized statement.
     *
     * @param prop the prop
     * @param lang the lang
     * @return the locale statement
     */
    private Statement getLocaleStatement(SemanticProperty prop, String lang) {
        Iterator<Statement> stit = listProperties(prop.getRDFProperty());
        Statement st = null;
        while (stit.hasNext()) {
            Statement staux = stit.next();
            try {
                String lg = staux.getLanguage();
                if (lg != null && lg.length() == 0) {
                    lg = null;
                }
                if ((lang == null && lg == null) || (lg != null && lg.equals(lang))) {
                    st = staux;
                    break;
                }
            } catch (Exception e) {
                LOG.error("Error in statement:" + staux, e);
            }
        }
        return st;
    }

    /**
     * External invoker get.
     *
     * @param prop the prop
     * @return the object
     */
    private Object externalInvokerGet(SemanticProperty prop) {
        Object ret = null;
        if (!virtual) {
            GenericObject obj = createGenericInstance();
            Class cls = obj.getClass();
            Method method = extGetMethods.get(cls.getName() + "-" + prop.getURI());
            if (method == null) {
                String pre = "get";
                if (prop.isBoolean()) {
                    pre = "is";
                }


                String name = prop.getPropertyCodeName();
                if (name == null) {
                    name = prop.getName();
                }

                name = pre + name.substring(0, 1).toUpperCase() + name.substring(1);
                try {
                    method = cls.getMethod(name);
                    extGetMethods.put(cls.getName() + "-" + prop.getURI(), method);
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
            try {
                ret = method.invoke(obj);
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        return ret;
    }

    /**
     * External invoker set.
     *
     * @param prop   the prop
     * @param values the values
     * @return the object
     */
    private Object externalInvokerSet(SemanticProperty prop, Object... values) {
        Object ret = null;

        if (!virtual) {
            Object [] vals;
            GenericObject obj = this.createGenericInstance();
            Class cls = obj.getClass();

            Method method = extSetMethods.get(cls.getName() + "-" + prop.getURI() + "-" + values.length);

            if (method == null) {
                String name = prop.getPropertyCodeName();
                if (name == null) {
                    name = prop.getName();
                }

                name = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);

                try {
                    Class [] types;
                    if (prop.isLocaleable()) {
                        types = new Class[values.length];
                    } else {
                        types = new Class[1];
                    }

                    Object o = values[0];
                    if (o != null) {
                        types[0] = o.getClass();
                    } else {
                        if (prop.isString()) {
                            types[0] = String.class;
                        } else if (prop.isDate()) {
                            types[0] = Date.class;
                        } else if (prop.isObjectProperty()) {
                            types[0] = prop.getDomainClass().getObjectClass();
                        }
                    }

                    Class pri = wrapperToPrimitive.get(types[0]);
                    if (pri != null) {
                        types[0] = pri;
                    }

                    if (prop.isLocaleable() && values.length > 1) {
                        o = values[1];
                        if (o == null) {
                            types[1] = String.class;
                        } else {
                            types[1] = o.getClass();
                        }
                    }
                    method = cls.getMethod(name, types);
                    extSetMethods.put(cls.getName() + "-" + prop.getURI() + "-" + values.length, method);
                } catch (Exception e) {
                    LOG.error(e);
                }
            }  // Movi la generación de "vals" a esta posición
            // por que si esta en caché se pasaba un null al invoke - MAPS
            if (prop.isLocaleable()) {
                vals = values;
            } else {
                vals = new Object[1];
                vals[0] = values[0];
            }

            try {
                ret = method.invoke(obj, vals);
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        return ret;
    }

    /**
     * Removes the dependencies.
     *
     * @param stack the stack
     */
    public void removeDependencies(ArrayList<SemanticObject> stack) {
        SemanticVocabulary v = SWBPlatform.getSemanticMgr().getVocabulary();
        Object [] stmts = getProperties().toArray();
        for (int x = 0; x < stmts.length; x++) {
            Statement st = (Statement) stmts[x];
            SemanticProperty prop = v.getSemanticProperty(st.getPredicate());

            if (prop.isObjectProperty()) {
                if (prop.isRemoveDependency()) {
                    Resource res = st.getResource();
                    if (res != null) {
                        SemanticObject obj = SemanticObject.createSemanticObject(res);
                        if (obj != null && stack != null && !stack.contains(obj)) {
                            obj.remove(stack);
                        }
                    }
                } else if (prop.isInverseOf()) {
                    removeStatement(st);
                }
            }
        }

        //Eliminar Inversas
        stmts = getInverseProperties().toArray();
        for (int x = 0; x < stmts.length; x++) {
            Statement st = (Statement) stmts[x];
            SemanticProperty prop = v.getSemanticProperty(st.getPredicate());
            Resource res = st.getSubject();
            if (res != null) {
                SemanticObject obj = SemanticObject.createSemanticObject(res);

                if (prop.isInverseOf() && prop.getInverse().isRemoveDependency()) {
                    if (obj != null && stack != null && !stack.contains(obj)) {
                        obj.remove(stack);
                    }
                } else {
                    if (obj != null) {
                        obj.removeStatement(st);
                    }
                }
            }
        }
    }

    /**
     * Removes the {@link SemanticObject} statements.
     */
    public void remove() {
        remove(new ArrayList<>());
    }

    /**
     * Removes the.
     *
     * @param stack the stack
     */
    public void remove(ArrayList<SemanticObject> stack) {
        remove(stack, true);
    }

    /**
     * Removes the.
     *
     * @param stack the stack
     */
    public void remove(ArrayList<SemanticObject> stack, boolean removeDep) {
        stack.add(this);
        if (getModel().getModelObject().equals(this)) { //es un modelo
            removeDependencies(stack);
            SWBPlatform.getSemanticMgr().removeModel(getId());
            SWBPlatform.getSemanticMgr().notifyChange(this, null, null, ACT_REMOVE);
            removeSemanticObjectFromCache(getURI());
        } else { //es un objeto
            SWBPlatform.getSemanticMgr().notifyChange(this, null, null, ACT_REMOVE);

            if (this.getSemanticClass() == null) {
                printStatements();
            }

            try {
                //TODO: revisar esto de vic
                Iterator<SemanticProperty> propit = this.getSemanticClass().listProperties();
                while (propit.hasNext()) {
                    SemanticProperty prop = propit.next();
                    if (prop.isBinary()) {
                        // removida manualmente por ser binaria
                        removeProperty(prop);
                    }
                }
            } catch (Exception e) {
                LOG.error(e);
            }

            //Eliminar dependencias
            if (removeDep) {
                removeDependencies(stack);
            }

            //Borrar objeto
            Resource res = getRDFResource();
            if (res != null) {
                remove(false);
            }
        }
    }

    /**
     * Removes the properties.
     */
    public void removeProperties() {
        objectResource.removeProperties();
        removeSemanticObjectFromCache(this.getURI());
    }

    /**
     * Asigna la propiedad con el valor especificado
     *
     * @param prop  Propiedad a modificar
     * @param value Valor a asignar
     * @return SemanticObject para cascada
     */
    public SemanticObject setProperty(SemanticProperty prop, String value) {
        return setProperty(prop, value, null);
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop        Propiedad a modificar
     * @param value       Valor a asignar
     * @param evalExtInvo the eval ext invo
     * @return SemanticObject para cascada
     */
    public SemanticObject setProperty(SemanticProperty prop, String value, boolean evalExtInvo) {
        return setProperty(prop, value, null, evalExtInvo);
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop  Propiedad a modificar
     * @param value Valor a asignar
     * @param lang  the lang
     * @return SemanticObject para cascada
     */
    public SemanticObject setProperty(SemanticProperty prop, String value, String lang) {
        return setProperty(prop, value, lang, true);
    }


    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop        Propiedad a modificar
     * @param value       Valor a asignar
     * @param lang        the lang
     * @param evalExtInvo the eval ext invo
     * @return SemanticObject para cascada
     */
    public SemanticObject setProperty(SemanticProperty prop, String value, String lang, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            externalInvokerSet(prop, value, lang);
        } else {
            setLiteralProperty(prop, new SemanticLiteral(value, lang));
        }
        return this;
    }

    /**
     * Regresa valor de la Propiedad especificada.
     *
     * @param prop the prop
     * @return valor de la propiedad, si no existe la propiedad regresa null
     */
    public String getProperty(SemanticProperty prop) {
        return getProperty(prop, true);
    }

    /**
     * Regresa valor de la Propiedad especificada.
     *
     * @param prop        the prop
     * @param evalExtInvo the eval ext invo
     * @return valor de la propiedad, si no existe la propiedad regresa null
     */
    public String getProperty(SemanticProperty prop, boolean evalExtInvo) {
        return getProperty(prop, null, evalExtInvo);
    }

    /**
     * Gets the property.
     *
     * @param prop     the prop
     * @param defValue the def value
     * @return the property
     */
    public String getProperty(SemanticProperty prop, String defValue) {
        return getProperty(prop, defValue, true);
    }

    /**
     * Gets the property.
     *
     * @param prop        the prop
     * @param defValue    the def value
     * @param evalExtInvo the eval ext invo
     * @return the property
     */
    public String getProperty(SemanticProperty prop, String defValue, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            Object aux = externalInvokerGet(prop);
            if (aux != null) {
                return "" + aux;
            }
        } else {
            SemanticLiteral lit = getLiteralProperty(prop, null);
            if (lit != null) {
                return lit.getString();
            }
        }
        return defValue;
    }

    /**
     * Gets the property.
     *
     * @param prop     the prop
     * @param defValue the def value
     * @param lang     the lang
     * @return the property
     */
    public String getProperty(SemanticProperty prop, String defValue, String lang) {
        SemanticLiteral lit = getLiteralProperty(prop, lang);
        if (lit != null) {
            return lit.getString();
        }
        return defValue;
    }

    /**
     * Gets the locale property.
     *
     * @param prop the prop
     * @param lang the lang
     * @return the locale property
     */
    public String getLocaleProperty(SemanticProperty prop, String lang) {
        String ret;

        if (lang == null) {
            ret = getProperty(prop);
        } else {
            ret = getProperty(prop, null, lang);
            if (ret == null) {
                ret = getProperty(prop);
            }
        }
        return ret;
    }

    /**
     * Gets the int property.
     *
     * @param prop the prop
     * @return the int property
     */
    public int getIntProperty(SemanticProperty prop) {
        return getIntProperty(prop, true);
    }

    /**
     * Gets the int property.
     *
     * @param prop        the prop
     * @param evalExtInvo the eval ext invo
     * @return the int property
     */
    public int getIntProperty(SemanticProperty prop, boolean evalExtInvo) {
        return getIntProperty(prop, 0, evalExtInvo);
    }

    /**
     * Gets the int property.
     *
     * @param prop     the prop
     * @param defValue the def value
     * @return the int property
     */
    public int getIntProperty(SemanticProperty prop, int defValue) {
        return getIntProperty(prop, defValue, true);
    }

    /**
     * Gets the int property.
     *
     * @param prop        the prop
     * @param defValue    the def value
     * @param evalExtInvo the eval ext invo
     * @return the int property
     */
    public int getIntProperty(SemanticProperty prop, int defValue, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            return (Integer) externalInvokerGet(prop);
        } else {
            SemanticLiteral lit = getLiteralProperty(prop);
            if (lit != null) {
                return lit.getInt();
            }
        }
        return defValue;
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop  Propiedad a modificar
     * @param value Valor a asignar
     * @return SemanticObject para cascada
     */
    public SemanticObject setIntProperty(SemanticProperty prop, int value) {
        return setIntProperty(prop, value, true);
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop        Propiedad a modificar
     * @param value       Valor a asignar
     * @param evalExtInvo the eval ext invo
     * @return SemanticObject para cascada
     */
    public SemanticObject setIntProperty(SemanticProperty prop, int value, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            externalInvokerSet(prop, value);
        } else {
            setLiteralProperty(prop, new SemanticLiteral(new Integer(value)));
        }
        return this;
    }

    /**
     * Gets the long property.
     *
     * @param prop the prop
     * @return the long property
     */
    public long getLongProperty(SemanticProperty prop) {
        return getLongProperty(prop, true);
    }

    /**
     * Gets the long property.
     *
     * @param prop        the prop
     * @param evalExtInvo the eval ext invo
     * @return the long property
     */
    public long getLongProperty(SemanticProperty prop, boolean evalExtInvo) {
        return getLongProperty(prop, 0L, evalExtInvo);
    }

    /**
     * Gets the long property.
     *
     * @param prop     the prop
     * @param defValue the def value
     * @return the long property
     */
    public long getLongProperty(SemanticProperty prop, long defValue) {
        return getLongProperty(prop, defValue, true);
    }

    /**
     * Gets the long property.
     *
     * @param prop        the prop
     * @param defValue    the def value
     * @param evalExtInvo the eval ext invo
     * @return the long property
     */
    public long getLongProperty(SemanticProperty prop, long defValue, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            return (Long) externalInvokerGet(prop);
        } else {
            SemanticLiteral lit = getLiteralProperty(prop);
            if (lit != null) {
                return lit.getLong();
            }
        }
        return defValue;
    }

    /**
     * Sets the input stream property.
     *
     * @param prop  the prop
     * @param value the value
     * @param name  the name
     * @return the semantic object
     * @throws SWBException the sWB exception
     */
    public SemanticObject setInputStreamProperty(SemanticProperty prop, InputStream value, String name) throws SWBException {
        String workPath = this.getWorkPath();
        if (!(workPath.endsWith("\\") || workPath.equals("/"))) {
            workPath += "/" + name;
        }
        setProperty(prop, name);
        SWBPlatform.createInstance().writeFileToPlatformWorkPath(workPath, value);
        return this;
    }

    /**
     * Gets the input stream property.
     *
     * @param prop the prop
     * @return the input stream property
     * @throws SWBException the sWB exception
     */
    public InputStream getInputStreamProperty(SemanticProperty prop) throws SWBException {
        String value = getProperty(prop);
        String workPath = this.getWorkPath();
        if (!(workPath.endsWith("\\") || workPath.equals("/"))) {
            workPath += "/" + value;
        }
        return SWBPlatform.createInstance().getFileFromPlatformWorkPath(workPath);
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop  Propiedad a modificar
     * @param value Valor a asignar
     * @return SemanticObject para cascada
     */
    public SemanticObject setLongProperty(SemanticProperty prop, long value) {
        return setLongProperty(prop, value, true);
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop        Propiedad a modificar
     * @param value       Valor a asignar
     * @param evalExtInvo the eval ext invo
     * @return SemanticObject para cascada
     */
    public SemanticObject setLongProperty(SemanticProperty prop, long value, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            externalInvokerSet(prop, value);
        } else {
            setLiteralProperty(prop, new SemanticLiteral(new Long(value)));
        }
        return this;
    }

    /**
     * Gets the float property.
     *
     * @param prop the prop
     * @return the float property
     */
    public float getFloatProperty(SemanticProperty prop) {
        return getFloatProperty(prop, true);
    }

    /**
     * Gets the float property.
     *
     * @param prop        the prop
     * @param evalExtInvo the eval ext invo
     * @return the float property
     */
    public float getFloatProperty(SemanticProperty prop, boolean evalExtInvo) {
        return getFloatProperty(prop, 0F, evalExtInvo);
    }

    /**
     * Gets the float property.
     *
     * @param prop     the prop
     * @param defValue the def value
     * @return the float property
     */
    public float getFloatProperty(SemanticProperty prop, float defValue) {
        return getFloatProperty(prop, defValue, true);
    }

    /**
     * Gets the float property.
     *
     * @param prop        the prop
     * @param defValue    the def value
     * @param evalExtInvo the eval ext invo
     * @return the float property
     */
    public float getFloatProperty(SemanticProperty prop, float defValue, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            return (Float) externalInvokerGet(prop);
        } else {
            SemanticLiteral lit = getLiteralProperty(prop);
            if (lit != null) {
                return lit.getFloat();
            }
        }
        return defValue;
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop  Propiedad a modificar
     * @param value Valor a asignar
     * @return SemanticObject para cascada
     */
    public SemanticObject setFloatProperty(SemanticProperty prop, float value) {
        return setFloatProperty(prop, value, true);
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop        Propiedad a modificar
     * @param value       Valor a asignar
     * @param evalExtInvo the eval ext invo
     * @return SemanticObject para cascada
     */
    public SemanticObject setFloatProperty(SemanticProperty prop, float value, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            externalInvokerSet(prop, value);
        } else {
            setLiteralProperty(prop, new SemanticLiteral(new Float(value)));
        }
        return this;
    }

    /**
     * Gets the double property.
     *
     * @param prop the prop
     * @return the double property
     */
    public double getDoubleProperty(SemanticProperty prop) {
        return getDoubleProperty(prop, true);
    }

    /**
     * Gets the double property.
     *
     * @param prop        the prop
     * @param evalExtInvo the eval ext invo
     * @return the double property
     */
    public double getDoubleProperty(SemanticProperty prop, boolean evalExtInvo) {
        return getDoubleProperty(prop, 0D, evalExtInvo);
    }

    /**
     * Gets the double property.
     *
     * @param prop     the prop
     * @param defValue the def value
     * @return the double property
     */
    public double getDoubleProperty(SemanticProperty prop, double defValue) {
        return getDoubleProperty(prop, defValue, true);
    }

    /**
     * Gets the double property.
     *
     * @param prop        the prop
     * @param defValue    the def value
     * @param evalExtInvo the eval ext invo
     * @return the double property
     */
    public double getDoubleProperty(SemanticProperty prop, double defValue, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            return (Double) externalInvokerGet(prop);
        } else {
            SemanticLiteral lit = getLiteralProperty(prop);
            if (lit != null) {
                return lit.getDouble();
            }
        }
        return defValue;
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop  Propiedad a modificar
     * @param value Valor a asignar
     * @return SemanticObject para cascada
     */
    public SemanticObject setDoubleProperty(SemanticProperty prop, double value) {
        return setDoubleProperty(prop, value, true);
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop        Propiedad a modificar
     * @param value       Valor a asignar
     * @param evalExtInvo the eval ext invo
     * @return SemanticObject para cascada
     */
    public SemanticObject setDoubleProperty(SemanticProperty prop, double value, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            externalInvokerSet(prop, value);
        } else {
            setLiteralProperty(prop, new SemanticLiteral(new Double(value)));
        }
        return this;
    }

    /**
     * Gets the boolean property.
     *
     * @param prop the prop
     * @return the boolean property
     */
    public boolean getBooleanProperty(SemanticProperty prop) {
        return getBooleanProperty(prop, true);
    }

    /**
     * Gets the boolean property.
     *
     * @param prop        the prop
     * @param evalExtInvo the eval ext invo
     * @return the boolean property
     */
    public boolean getBooleanProperty(SemanticProperty prop, boolean evalExtInvo) {
        return getBooleanProperty(prop, false, evalExtInvo);
    }

    /**
     * Gets the boolean property.
     *
     * @param prop        the prop
     * @param defValue    the def value
     * @param evalExtInvo the eval ext invo
     * @return the boolean property
     */
    public boolean getBooleanProperty(SemanticProperty prop, boolean defValue, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            return (Boolean) externalInvokerGet(prop);
        } else {
            SemanticLiteral lit = getLiteralProperty(prop);
            if (lit != null) {
                return lit.getBoolean();
            }
        }
        return defValue;
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop  Propiedad a modificar
     * @param value Valor a asignar
     * @return SemanticObject para cascada
     */
    public SemanticObject setBooleanProperty(SemanticProperty prop, boolean value) {
        return setBooleanProperty(prop, value, true);
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop        Propiedad a modificar
     * @param value       Valor a asignar
     * @param evalExtInvo the eval ext invo
     * @return SemanticObject para cascada
     */
    public SemanticObject setBooleanProperty(SemanticProperty prop, boolean value, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            externalInvokerSet(prop, value);
        } else {
            setLiteralProperty(prop, new SemanticLiteral(new Boolean(value)));
        }
        return this;
    }


    /**
     * Gets the date property.
     *
     * @param prop the prop
     * @return the date property
     */
    public java.util.Date getDateProperty(SemanticProperty prop) {
        return getDateProperty(prop, true);
    }

    /**
     * Gets the date property.
     *
     * @param prop        the prop
     * @param evalExtInvo the eval ext invo
     * @return the date property
     */
    public java.util.Date getDateProperty(SemanticProperty prop, boolean evalExtInvo) {
        return getDateProperty(prop, null, evalExtInvo);
    }

    /**
     * Gets the date property.
     *
     * @param prop     the prop
     * @param defValue the def value
     * @return the date property
     */
    public java.util.Date getDateProperty(SemanticProperty prop, java.util.Date defValue) {
        return getDateProperty(prop, defValue, true);
    }

    /**
     * Gets the date property.
     *
     * @param prop        the prop
     * @param defValue    the def value
     * @param evalExtInvo the eval ext invo
     * @return the date property
     */
    public java.util.Date getDateProperty(SemanticProperty prop, java.util.Date defValue, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            return (java.util.Date) externalInvokerGet(prop);
        } else {
            SemanticLiteral lit = getLiteralProperty(prop);
            if (lit != null) {
                return lit.getDateTime();
            }
        }
        return defValue;
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop  Propiedad a modificar
     * @param value Valor a asignar
     * @return SemanticObject para cascada
     */
    public SemanticObject setDateProperty(SemanticProperty prop, java.util.Date value) {
        return setDateProperty(prop, value, true);
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop        Propiedad a modificar
     * @param value       Valor a asignar
     * @param evalExtInvo the eval ext invo
     * @return SemanticObject para cascada
     */
    public SemanticObject setDateProperty(SemanticProperty prop, java.util.Date value, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            externalInvokerSet(prop, value);
        } else {
            if (value != null) {
                setLiteralProperty(prop, new SemanticLiteral(new Timestamp(value.getTime())));
            } else {
                removeProperty(prop);
            }
        }
        return this;
    }


    /**
     * Gets the sQL date property.
     *
     * @param prop the prop
     * @return the sQL date property
     */
    public Date getSQLDateProperty(SemanticProperty prop) {
        return getSQLDateProperty(prop, true);
    }

    /**
     * Gets the sQL date property.
     *
     * @param prop        the prop
     * @param evalExtInvo the eval ext invo
     * @return the sQL date property
     */
    public Date getSQLDateProperty(SemanticProperty prop, boolean evalExtInvo) {
        return getSQLDateProperty(prop, null, evalExtInvo);
    }

    /**
     * Gets the sQL date property.
     *
     * @param prop     the prop
     * @param defValue the def value
     * @return the sQL date property
     */
    public Date getSQLDateProperty(SemanticProperty prop, Date defValue) {
        return getSQLDateProperty(prop, defValue, true);
    }

    /**
     * Gets the sQL date property.
     *
     * @param prop        the prop
     * @param defValue    the def value
     * @param evalExtInvo the eval ext invo
     * @return the sQL date property
     */
    public Date getSQLDateProperty(SemanticProperty prop, Date defValue, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            return (Date) externalInvokerGet(prop);
        } else {
            SemanticLiteral lit = getLiteralProperty(prop);
            if (lit != null) {
                return lit.getDate();
            }
        }
        return defValue;
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop  Propiedad a modificar
     * @param value Valor a asignar
     * @return SemanticObject para cascada
     */
    public SemanticObject setSQLDateProperty(SemanticProperty prop, Date value) {
        return setSQLDateProperty(prop, value, true);
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop        Propiedad a modificar
     * @param value       Valor a asignar
     * @param evalExtInvo the eval ext invo
     * @return SemanticObject para cascada
     */
    public SemanticObject setSQLDateProperty(SemanticProperty prop, Date value, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            externalInvokerSet(prop, value);
        } else {
            setLiteralProperty(prop, new SemanticLiteral(value));
        }
        return this;
    }

    /**
     * Gets the date time property.
     *
     * @param prop the prop
     * @return the date time property
     */
    public Timestamp getDateTimeProperty(SemanticProperty prop) {
        return getDateTimeProperty(prop, true);
    }

    /**
     * Gets the date time property.
     *
     * @param prop        the prop
     * @param evalExtInvo the eval ext invo
     * @return the date time property
     */
    public Timestamp getDateTimeProperty(SemanticProperty prop, boolean evalExtInvo) {
        return getDateTimeProperty(prop, null, evalExtInvo);
    }

    /**
     * Gets the date time property.
     *
     * @param prop     the prop
     * @param defValue the def value
     * @return the date time property
     */
    public Timestamp getDateTimeProperty(SemanticProperty prop, Timestamp defValue) {
        return getDateTimeProperty(prop, defValue, true);
    }

    /**
     * Gets the date time property.
     *
     * @param prop        the prop
     * @param defValue    the def value
     * @param evalExtInvo the eval ext invo
     * @return the date time property
     */
    public Timestamp getDateTimeProperty(SemanticProperty prop, Timestamp defValue, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            return (Timestamp) externalInvokerGet(prop);
        } else {
            SemanticLiteral lit = getLiteralProperty(prop);
            if (lit != null) {
                return lit.getDateTime();
            }
        }
        return defValue;
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop  Propiedad a modificar
     * @param value Valor a asignar
     * @return SemanticObject para cascada
     */
    public SemanticObject setDateTimeProperty(SemanticProperty prop, Timestamp value) {
        return setDateTimeProperty(prop, value, true);
    }

    /**
     * Asigna la propiedad con el valor especificado.
     *
     * @param prop        Propiedad a modificar
     * @param value       Valor a asignar
     * @param evalExtInvo the eval ext invo
     * @return SemanticObject para cascada
     */
    public SemanticObject setDateTimeProperty(SemanticProperty prop, Timestamp value, boolean evalExtInvo) {
        if (evalExtInvo && prop.isExternalInvocation()) {
            externalInvokerSet(prop, value);
        } else {
            if (value != null) {
                setLiteralProperty(prop, new SemanticLiteral(value));
            } else {
                removeProperty(prop);
            }
        }
        return this;
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return getDisplayName(null);
    }

    /**
     * Gets the display name.
     *
     * @param lang the lang
     * @return the display name
     */
    public String getDisplayName(String lang) {
        String ret = null;
        SemanticClass cls = getSemanticClass();
        SemanticProperty prop = null;

        if (cls != null) {
            prop = cls.getDisplayNameProperty();
        }

        if (prop != null) {
            if (prop.isDataTypeProperty()) {
                ret = getLocaleProperty(prop, lang);
            } else if (prop.isObjectProperty()) {
                SemanticObject obj = getObjectProperty(prop);
                if (obj != null) {
                    ret = obj.getDisplayName(lang);
                }
            }
        }

        if (ret == null) {
            ret = getLabel(lang);
            if (ret == null) {
                if (cls != null) {
                    ret = cls.getName() + ":" + getId();
                } else {
                    ret = getId();
                }
            }
        }
        return ret;
    }

    /**
     * Transform to semantic property.
     *
     * @return the semantic property
     */
    public SemanticProperty transformToSemanticProperty() {
        return SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(getURI());
    }

    /**
     * Transform to semantic class.
     *
     * @return the semantic class
     */
    public SemanticClass transformToSemanticClass() {
        return SWBPlatform.getSemanticMgr().getVocabulary().getSemanticClass(getURI());
    }

    /**
     * List related objects.
     *
     * @return the iterator
     */
    public Iterator<SemanticObject> listRelatedObjects() {
        List arr = new ArrayList();
        StmtIterator stit = getModel().getRDFModel().listStatements(null, null, getRDFResource());

        Iterator it = new SemanticIterator(stit, true);
        while (it.hasNext()) {
            arr.add(it.next());
        }

        it = getSemanticClass().listProperties();
        while (it.hasNext()) {
            SemanticProperty prop = (SemanticProperty) it.next();
            if (prop.isObjectProperty() && prop.isInverseOf()) {
                SemanticObject obj = getObjectProperty(prop);
                if (obj != null) {
                    arr.add(obj);
                }
            }
        }
        return arr.iterator();
    }

    /**
     * List herarquical childs.
     *
     * @return the iterator
     */
    public Iterator<SemanticObject> listHerarquicalChilds() {
        List<SemanticObject> list = new ArrayList<>();
        SemanticClass cls = getSemanticClass();

        if (cls != null) {
            Iterator<SemanticProperty> it = cls.listHerarquicalProperties();
            while (it.hasNext()) {
                SemanticProperty prop = it.next();
                SemanticClass hfcls = prop.getHerarquicalRelationFilterClass();
                Iterator<SemanticObject> it2 = listObjectProperties(prop);
                while (it2.hasNext()) {
                    SemanticObject ch = it2.next();
                    if (hfcls != null && !ch.instanceOf(hfcls)) {
                        continue;
                    }
                    list.add(ch);
                }
            }
        }
        return list.iterator();
    }

    /**
     * Checks for herarquical parents.
     *
     * @return true, if successful
     */
    public boolean hasHerarquicalParents() {
        Iterator<SemanticProperty> it = getSemanticClass().listInverseHerarquicalProperties();
        while (it.hasNext()) {
            SemanticProperty prop = it.next();
            if (hasObjectProperty(prop)) {
                return true;
            }
        }
        return false;
    }

    /**
     * List herarquical parents.
     *
     * @return the iterator
     */
    public Iterator<SemanticObject> listHerarquicalParents() {
        HashSet<SemanticObject> list = new HashSet<>();
        Iterator<SemanticProperty> it = getSemanticClass().listInverseHerarquicalProperties();

        while (it.hasNext()) {
            SemanticProperty prop = it.next();
            Iterator<SemanticObject> it2 = listObjectProperties(prop);
            while (it2.hasNext()) {
                SemanticObject ch = it2.next();
                if (ch != null) {
                    if (!ch.equals(this)) {
                        list.add(ch);
                    }
                } else {
                    LOG.error("Error listHerarquicalParents from:" + this + " prop:" + prop);
                }
            }
        }
        return list.iterator();
    }

    /**
     * Clona un objeto y sus dependencias, el objeto hash sirve para
     * almacenar las dependencias clonadas.
     *
     * @return
     */
    public SemanticObject cloneObject() {
        //Get URI
        String id;
        if (getSemanticClass().isAutogenId()) {
            id = "" + getModel().getAndIncrementCounter(getSemanticClass());
        } else {
            int x = 1;
            do {
                x++;
                id = getId() + x;
            } while (createSemanticObject(id) != null);
        }

        String uri = getModel().getObjectUri(id, getSemanticClass());
        Resource res = getModel().getRDFModel().createResource(uri);

        //Get Herarquical properties
        ArrayList<SemanticProperty> hp = new ArrayList<>();
        Iterator<SemanticProperty> ithp = getSemanticClass().listHerarquicalProperties();
        while (ithp.hasNext()) {
            hp.add(ithp.next());
        }

        ithp = getSemanticClass().listInverseHerarquicalProperties();
        while (ithp.hasNext()) {
            hp.add(ithp.next());
        }

        Iterator<Statement> it = objectResource.listProperties();
        while (it.hasNext()) {
            Statement st = it.next();
            Property prop = st.getPredicate();
            SemanticProperty sprop = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(prop.getURI());
            if (sprop == null || !sprop.isRemoveDependency()) {
                Statement nst = getModel().getRDFModel().createStatement(res, prop, st.getObject());
                getModel().getRDFModel().add(nst);
            }
        }

        SemanticObject ret = SemanticObject.createSemanticObject(res);
        SWBPlatform.getSemanticMgr().notifyChange(ret, null, null, ACT_CLONE);
        return ret;
    }

    /**
     * Gets the herarquical parent.
     *
     * @return the herarquical parent
     */
    public SemanticObject getHerarquicalParent() {
        Iterator<SemanticObject> it = listHerarquicalParents();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    /**
     * Gets the work path.
     *
     * @return the work path
     */
    public String getWorkPath() {
        SemanticClass cls = getSemanticClass();
        if (cls != null) {
            return "/models/" + getModel().getName() + "/" + cls.getClassGroupId() + "/" + getId();
        }
        return null;
    }

    /**
     * Regresa todos los valores de la propiedad sin importar el idioma
     * Utilizado para la indexación del objeto.
     *
     * @param prop the prop
     * @return
     */
    public String getPropertyIndexData(SemanticProperty prop) {
        StringBuilder ret = new StringBuilder();
        StmtIterator stit = objectResource.listProperties(prop.getRDFProperty());

        while (stit.hasNext()) {
            Statement st = stit.nextStatement();
            ret.append(st.getString()).append("\n");
        }
        stit.close();

        return ret.toString();
    }


    /**
     * Regresa el valor de la propiedad rdfs:label del objeto.
     *
     * @return the label
     */
    public String getLabel() {
        return getLabel(null);
    }

    /**
     * Regresa el valor de la propiedad rdfs:label del objeto.
     *
     * @param lang the lang
     * @return the label
     */
    public String getLabel(String lang) {
        return getLocaleProperty(SWBPlatform.getSemanticMgr().
                getVocabulary().getSemanticProperty(SemanticVocabulary.RDFS_LABEL), lang);
    }

    /**
     * Regresa el valor de la propiedad rdfs:comment del objeto.
     *
     * @return the label
     */
    public String getComment() {
        return getComment(null);
    }

    /**
     * Regresa el valor de la propiedad rdfs:comment del objeto.
     *
     * @param lang the lang
     * @return the label
     */
    public String getComment(String lang) {
        return getLocaleProperty(SWBPlatform.getSemanticMgr()
                .getVocabulary().getSemanticProperty(SemanticVocabulary.RDFS_COMMENT), lang);
    }
}
