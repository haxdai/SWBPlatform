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

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.IteratorFactory;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.GenericObject;
import org.semanticwb.rdf.GraphExt;
import org.semanticwb.rdf.RemoteGraph;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * High level wrapper for an Ontology Model that manages SemanticObjects.
 * @author Jei
 */
public class SemanticModel {
    /**
     * Enumeration of SemanticProperty types.
     */
    public enum SemanticPropertyType {
        DATA,
        OBJECT
    }

    /**
     * The wrapped {@link Model} Object.
     */
    private Model model;

    /**
     * The wrapped {@link OntModel} object.
     */
    OntModel ont;

    /**
     * The ARQ dataset for SPARQL query execution.
     */
    Dataset dataset = null;

    /**
     * The model name.
     */
    private String modelName;

    /**
     * The m_name space.
     */
    private String nameSpace;

    /**
     * The m_model object.
     */
    private String modelObject;

    /**
     * The traceChanges.
     */
    private boolean traceChanges = true;

    //TODO: Check this flag because is never set to true outside this class. Method isDataModel is only used in SWBServiceMgr
    private boolean isDataModel = false;

    /**
     * The modelClasses.
     */
    private List modelClasses = null;

    /**
     * Instantiates a new {@link SemanticModel}.
     *
     * @param name  model name
     * @param model the {@link SemanticModel}
     */
    public SemanticModel(String name, Model model) {
        this.model = model;
        this.modelName = name;
        init();
    }

    /**
     * Inits the {@link SemanticModel}.
     */
    private void init() {
    }


    /**
     * Checks if {@link SemanticModel} will log user changes.
     * @return whether {@link SemanticModel} will log user changes.
     */
    public boolean isTraceable() {
        return traceChanges;
    }

    /**
     * Checks whether this model is a DataModel.
     * @return
     */
    public boolean isDataModel() {
        return isDataModel;
    }

    /**
     * Enables or disables change logging.
     * @param enable enable
     */
    public void setTraceable(boolean enable) {
        traceChanges = enable;
    }

    /**
     * Sets the isDataModel property.
     * @param val the new value.
     */
    public void setDataModel(boolean val) {
        isDataModel = val;
    }

    /**
     * Gets all subjects containing a double literal <code>value</code> as object of all subjects
     * containing the given <code>property</code>. It is equivalent to the following SPARQL query.
     *
     * select ?x where { ?x property "value"^^xsd:double}
     *
     * @param property  {@link SemanticProperty} object
     * @param value     the double value
     * @return          Iterator of {@link SemanticObject}s that contains <code>value</code>
     *                  as object of <code>property</code>.
     */
    public Iterator<SemanticObject> listSubjects(SemanticProperty property, double value) {
        return new SemanticIterator(
                getRDFModel().listStatements(
                                null,
                                property.getRDFProperty(),
                                getRDFModel().createTypedLiteral(value)),
                true);
    }

    /**
     * Gets all subjects containing a float literal <code>value</code> as object of all subjects
     * containing the given <code>property</code>. It is equivalent to the following SPARQL query.
     *
     * select ?x where { ?x property "value"^^xsd:float}
     *
     * @param property  {@link SemanticProperty} object
     * @param value     the float value
     * @return          Iterator of {@link SemanticObject}s that contains <code>value</code>
     *                  as object of <code>property</code>.
     */
    public Iterator<SemanticObject> listSubjects(SemanticProperty property, float value) {
        return new SemanticIterator(
                getRDFModel().listStatements(
                        null,
                        property.getRDFProperty(),
                        getRDFModel().createTypedLiteral(value)),
                true);
    }

    /**
     * Gets all subjects containing a long literal <code>value</code> as object of all subjects
     * containing the given <code>property</code>. It is equivalent to the following SPARQL query.
     *
     * select ?x where { ?x property "value"^^xsd:long}
     *
     * @param property  {@link SemanticProperty} object
     * @param value     the long value
     * @return          Iterator of {@link SemanticObject}s that contains <code>value</code>
     *                  as object of <code>property</code>.
     */
    public Iterator<SemanticObject> listSubjects(SemanticProperty property, long value) {
        return new SemanticIterator(
                getRDFModel().listStatements(
                        null,
                        property.getRDFProperty(),
                        getRDFModel().createTypedLiteral(value)),
                true);
    }

    /**
     * Gets all subjects containing a integer literal <code>value</code> as object of all subjects
     * containing the given <code>property</code>. It is equivalent to the following SPARQL query.
     *
     * select ?x where { ?x property "value"^^xsd:integer}
     *
     * @param property  {@link SemanticProperty} object
     * @param value     the integer value
     * @return          Iterator of {@link SemanticObject}s that contains <code>value</code>
     *                  as object of <code>property</code>.
     */
    public Iterator<SemanticObject> listSubjects(SemanticProperty property, int value) {
        return new SemanticIterator(
                getRDFModel().listStatements(
                        null,
                        property.getRDFProperty(),
                        getRDFModel().createTypedLiteral(value)),
                true);
    }

    /**
     * Gets all subjects containing a boolean literal <code>value</code> as object of all subjects
     * containing the given <code>property</code>. It is equivalent to the following SPARQL query.
     *
     * select ?x where { ?x property "value"^^xsd:boolean}
     *
     * @param property  {@link SemanticProperty} object
     * @param value     the boolean value
     * @return          Iterator of {@link SemanticObject}s that contains <code>value</code>
     *                  as object of <code>property</code>.
     */
    public Iterator<SemanticObject> listSubjects(SemanticProperty property, boolean value) {
        return new SemanticIterator(
                getRDFModel().listStatements(
                        null,
                        property.getRDFProperty(),
                        getRDFModel().createTypedLiteral(value)),
                true);
    }

    /**
     * Gets all subjects containing a string literal <code>value</code> as object of all subjects
     * containing the given <code>property</code>. It is equivalent to the following SPARQL query.
     *
     * select ?x where { ?x property "value"}
     *
     * @param property  {@link SemanticProperty} object
     * @param value     the string value
     * @return          Iterator of {@link SemanticObject}s that contains <code>value</code>
     *                  as object of <code>property</code>.
     */
    public Iterator<SemanticObject> listSubjects(SemanticProperty property, String value) {
        return new SemanticIterator(
                getRDFModel().listStatements(
                        null,
                        property.getRDFProperty(),
                        value),
                true);
    }


    /**
     * Gets all subjects containing a {@link SemanticObject} <code>obj</code> as object of all subjects
     * containing the given <code>property</code>. It is equivalent to the following SPARQL query.
     *
     * select ?x where { ?x property obj}
     *
     * @param property  {@link SemanticProperty} object
     * @param obj       the {@link SemanticObject} value
     * @return          Iterator of {@link SemanticObject}s that contains <code>obj</code>
     *                  as object of <code>property</code>.
     */
    public Iterator<SemanticObject> listSubjects(SemanticProperty property, SemanticObject obj) {
        return new SemanticIterator(
                getRDFModel().listStatements(
                        null,
                        property.getRDFProperty(),
                        obj.getRDFResource()),
                true);
    }

    /**
     * Gets all subjects containing a {@link SemanticObject} <code>obj</code> as object of all subjects
     * containing the given <code>property</code>. This methods resolves only subjects that are instances
     * of <code>cls</code>.
     *
     * It is equivalent to the following SPARQL query.
     *
     * select ?x where {
     *  ?x property obj.
     *  ?x rdf:type ?type.
     *  ?type rdfs:subClassOf* cls
     * }
     *
     * @param property  {@link SemanticProperty} object
     * @param obj       the {@link SemanticObject} value
     * @param cls       the required {@link SemanticClass}
     * @return          Iterator of {@link SemanticObject}s that contains <code>obj</code>
     *                  as object of <code>property</code> and are instances of <code>cls</code>.
     */
    public Iterator<SemanticObject> listSubjectsByClass(SemanticProperty property, SemanticObject obj, SemanticClass cls) {
        List<SemanticObject> ret = new ArrayList<>();

        SemanticIterator<SemanticObject> it = new SemanticIterator(
                getRDFModel().listStatements(
                        null,
                        property.getRDFProperty(),
                        obj.getRDFResource()),
                true);

        //Filter by class
        while (it.hasNext()) {
            SemanticObject semanticObject = it.next();
            if (semanticObject.instanceOf(cls)) {
                ret.add(semanticObject);
            }
        }
        return ret.iterator();
    }

    /**
     * Gets the {@link SemanticModel} name.
     * @return SemanticModel name
     */
    public String getName() {
        return modelName;
    }

    /**
     * Gets the RDF model.
     * @return the RDF model
     */
    public Model getRDFModel() {
        return model;
    }

    /**
     * Begin a new transation.
     * @see Model#begin()
     */
    public void begin() {
        if (model.supportsTransactions()) {
            model.begin();
        }
    }

    /**
     * Commit the current transaction.
     * @see Model#commit()
     */
    public void commit() {
        if (model.supportsTransactions()) {
            model.commit();
        }
    }

    /**
     * Abort the current transaction.
     * @see Model#abort()
     */
    public void abort() {
        if (model.supportsTransactions()) {
            model.abort();
        }
    }

    /**
     * Gets the RDF {@link OntModel}.
     * @return the RDF {@link OntModel}
     */
    public OntModel getRDFOntModel() {
        if (ont == null) {
            ont = ModelFactory.createOntologyModel(SWBPlatform.getSemanticMgr().getModelSpec(), model);
            Iterator<SemanticModel> it = SWBPlatform.getSemanticMgr().listBaseModels();
            while (it.hasNext()) {
                SemanticModel smodel = it.next();
                ont.addSubModel(smodel.getRDFModel());
            }
        }
        return ont;
    }

    /**
     * Gets a semantic object using its URI.
     * @param uri the URI
     * @return the {@link SemanticObject} with the given URI or null.
     */
    public SemanticObject getSemanticObject(String uri) {
        return SemanticObject.createSemanticObject(uri);
    }

    /**
     * Creates a {@link SemanticObject} of type <code>cls</code> using an ID.
     * @param id  the id
     * @param cls the {@link SemanticClass}
     * @return the {@link SemanticObject}.
     */
    public SemanticObject createSemanticObjectById(String id, SemanticClass cls) {
        return createSemanticObject(getObjectUri(id, cls), cls);
    }

    /**
     * Creates a {@link SemanticObject} of type <code>cls</code> using an URI.
     * @param uri the URI
     * @param cls the {@link SemanticClass}
     * @return the {@link SemanticObject}.
     */
    public SemanticObject createSemanticObject(String uri, SemanticClass cls) {
        //Create resource and set type to cls
        Resource res = model.createResource(uri);
        res.addProperty(RDF.type, cls.getOntClass());

        //Create SemanticObject from resource and add it to cache
        SemanticObject ret = new SemanticObject(this, res, cls);
        SemanticObject.cacheSemanticObject(ret);

        //Notify object creation
        SWBPlatform.getSemanticMgr().notifyChange(ret, null, null, SemanticObject.ACT_CREATE);

        //Set default values to properties
        Iterator<SemanticProperty> it = cls.listProperties();
        while (it.hasNext()) {
            SemanticProperty prop = it.next();
            String def = prop.getDefaultValue();
            if (def != null) {
                SemanticLiteral lit = SemanticLiteral.valueOf(prop, def);
                ret.setLiteralProperty(prop, lit, false);
            }
        }
        return ret;
    }

    /**
     * Gets a {@link GenericObject} using an URI.
     * @param uri the uri
     * @return the generic object
     */
    public GenericObject getGenericObject(String uri) {
        SemanticObject obj = getSemanticObject(uri);
        if (obj != null) {
            return obj.createGenericInstance();
        }
        return null;
    }

    /**
     * Gets the generic object.
     *
     * @param uri the uri
     * @param cls the cls
     * @return the generic object
     */
    public GenericObject getGenericObject(String uri, SemanticClass cls) {
        //TODO: Check this method because cls parameter is ignored.
        SemanticObject obj = getSemanticObject(uri);
        if (obj != null) {
            return obj.createGenericInstance();
        }
        //TODO: At this point, object should be created using createSemanticObject
        return null;
    }

    /**
     * Creates a {@link GenericObject} from a {@link SemanticObject} with a given URI of type <code>cls</code>.
     *
     * @param uri the URI
     * @param cls the {@link SemanticClass}
     * @return the {@link GenericObject}
     */
    public GenericObject createGenericObject(String uri, SemanticClass cls) {
        SemanticObject obj = createSemanticObject(uri, cls);
        if (obj != null) {
            return obj.createGenericInstance();
        }
        return null;
    }

    /**
     * Removes a {@link SemanticObject} with a given URI from the model.
     * @param uri the URI
     */
    public void removeSemanticObject(String uri) {
        SemanticObject obj = getSemanticObject(uri);
        if (obj != null) {
            obj.remove();
        }
    }

    /**
     * Removes a {@link SemanticObject} from the model.
     * @param obj the {@link SemanticObject} to remove.
     */
    public void removeSemanticObject(SemanticObject obj) {
        if (null != obj) {
            obj.remove();
        }
    }

    /**
     * Removes a {@link GenericObject} from the model.
     * @param obj {@link GenericObject} to remove.
     */
    public void removeGenericObject(GenericObject obj) {
        if (null != obj) {
            removeSemanticObject(obj.getSemanticObject());
        }
    }

    /**
     * Sets the model name space.
     * @param nameSpace the new name space
     */
    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    /**
     * Gets the model name space.
     * @return the model name space
     */
    public String getNameSpace() {
        if (nameSpace == null) {
            nameSpace = model.getNsPrefixURI(modelName);

            if (nameSpace == null) {
                Iterator<Statement> it = model.listStatements(null,
                        model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        model.getResource("http://www.w3.org/2002/07/owl#Ontology"));

                if (it.hasNext()) {
                    Statement st = it.next();
                    nameSpace = st.getSubject().getURI();
                }
            }
        }
        return nameSpace;
    }

    /**
     * Builds the URI for a {@link SemanticObject} using its <code>id</code> and <code>cls</code>.
     *
     * @param id  the object ID
     * @param cls the object's {@link SemanticClass}
     * @return the object URI
     */
    public String getObjectUri(String id, SemanticClass cls) {
        StringBuilder ret = new StringBuilder(getNameSpace());
        if (cls != null && !cls.isSWBModel()) {
            ret.append(cls.getClassGroupId()).append(":");
        }
        ret.append(id);
        return ret.toString();
    }

    /**
     * Gets the model as a {@link SemanticObject}.
     * @return the model object
     */
    public SemanticObject getModelObject() {
        if (modelObject == null) {
            modelObject = getObjectUri(getName(), null);
        }
        return getSemanticObject(modelObject);
    }

    /**
     * Get an {@link Iterator} of {@link SemanticObject}s that are instances of <code>cls</code>.
     *
     * @param cls the cls
     * @return Iterator of {@link SemanticObject}
     */
    public Iterator<SemanticObject> listInstancesOfClass(SemanticClass cls) {
        return listInstancesOfClass(cls, true);
    }

    /**
     * Get an {@link Iterator} of {@link SemanticObject}s that are instances of <code>cls</code>
     * and instances of a subclass of <code>cls</code> when <code>checkSubClasses</code> is true.
     *
     * @param cls               the {@link SemanticClass}
     * @param checkSubClasses   whether to include {@link SemanticObject} that is instance of subclasses
     *                          of <code>cls</code>
     * @return Iterator of {@link SemanticObject}
     */
    public Iterator<SemanticObject> listInstancesOfClass(SemanticClass cls, boolean checkSubClasses) {
        //Get all subjects of type cls
        StmtIterator stit = getRDFModel().listStatements(null, RDF.type, cls.getOntClass());

        Iterator<SemanticObject> ret;

        //TODO: Check this if-else because SemanticIterator ignores last parameter
        if (cls.isSWBClass()) {
            //Crea instancias de este tiplo de clase en el modelo sin verificar clase
            ret = new SemanticIterator(stit, true, this, cls);
        } else {
            //Crea instancias de este tiplo de clase en el modelo verificando clase
            ret = new SemanticIterator(stit, true, this, null);
        }

        //Add instances of subclases if needed
        if (checkSubClasses) {
            ArrayList<SemanticObject> arr = new ArrayList<>();
            while (ret.hasNext()) {
                arr.add(ret.next());
            }

            Iterator<SemanticClass> clsit = cls.listSubClasses(false);
            while (clsit.hasNext()) {
                SemanticClass scls = clsit.next();
                Iterator<SemanticObject> sit = listInstancesOfClass(scls, false);
                while (sit.hasNext()) {
                    arr.add(sit.next());
                }
            }
            ret = arr.iterator();
        }
        return ret;
    }

    /**
     * @apiNote Use this method only if the GraphExt (swbtse) persistence method is defined. If
     * GraphExt (swbtse) is not used, this method returns always an empty iterator.
     *
     * Gets an Iterator of {@link SemanticObject}s which are instances of <code>cls</code>, ordered
     * by creation date.
     *
     * @param cls the {@link SemanticClass}.
     * @param asc whether to get results in ascending order.
     * @return SemanticObject iterator.
     */
    //TODO: Review this method because only gets results on specific conditions and may cause confusion to developers
    public Iterator<SemanticObject> listInstancesOfClassOrderByCreation(SemanticClass cls, boolean asc) {
        String order = asc ? "timems" : "timems desc";
        StmtIterator stit = listStatements(null, RDF.type, cls.getOntClass(),
                null, null, null, order);


        //TODO: Check this if-else because SemanticIterator ignores last parameter
        if (cls.isSWBClass()) {
            //Crea instancias de este tiplo de clase en el modelo sin verificar clase
            return new SemanticIterator(stit, true, this, cls);
        } else {
            //Crea instancias de este tiplo de clase en el modelo verificando clase
            return new SemanticIterator(stit, true, this, null);
        }
    }

    /**
     * @apiNote Use this method only if the GraphExt (swbtse) persistence method is defined. If
     * GraphExt (swbtse) is not used, this method returns always an empty iterator.
     *
     * Gets an Iterator of {@link SemanticObject}s which have the same GroupID of <code>cls</code>, ordered
     * by the given <code>property</code>.
     *
     * @param cls the {@link SemanticClass}.
     * @param property
     * @param asc whether to get results in ascending order.
     * @return SemanticObject iterator.
     */
    //TODO: Rename and review this method because only gets results on specific conditions and may cause confusion to developers
    public Iterator<SemanticObject> listInstancesOfClassOrderByProperty(SemanticClass cls, SemanticProperty property, boolean asc) {
        String order = asc ? "sort" : "sort desc";

        StmtIterator stit = listStatements(null, property.getRDFProperty(),
                null, cls.getClassGroupId(), null, null, order);

        //TODO: Check this if-else because SemanticIterator ignores last parameter
        if (cls.isSWBClass()) {
            //Crea instancias de este tiplo de clase en el modelo sin verificar clase
            return new SemanticIterator(stit, true, this, cls);
        } else {
            //Crea instancias de este tiplo de clase en el modelo verificando clase
            return new SemanticIterator(stit, true, this, null);
        }
    }

    /**
     * @apiNote Use this method only if the GraphExt (swbtse) persistence method is defined. If
     * GraphExt (swbtse) is not used, this method returns null.
     *
     * Gets an Statement Iterator matching given criteria. Uses extended relational schema of
     * Triple Store to execute efficient SQL queries.
     *
     * @param subject {@link Resource} to match in a triple.
     * @param property {@link Property} to match in a triple.
     * @param object {@link RDFNode} to macth in a triple.
     * @param stype String holding URI of subject class.
     * @param limit maximum number of results to get.
     * @param offset record offset to start from.
     * @param sortBy Sort string one of "subj", "prop", "obj", "sort", "timems", "stype"
     * @return StmtIterator iterator.
     */
    //TODO: Review this method because only gets results on specific conditions and may cause confusion to developers
    public StmtIterator listStatements(Resource subject, Property property, RDFNode object, String stype, Long limit, Long offset, String sortBy) {
        Graph g = getRDFModel().getGraph();
        if (g instanceof GraphExt) {
            GraphExt gext = (GraphExt)g;

            //Create triple matcher
            TripleMatch tm = Triple.createMatch(
                    subject != null ? subject.asNode() : null,
                    property != null ? property.asNode() : null,
                    object != null ? object.asNode() : null);

            //Return an iterator of statements matching criteria
            return IteratorFactory.asStmtIterator(gext.find(tm, stype, limit, offset, sortBy),
                    (ModelCom) getRDFModel());
        }
        return null;
    }

    /**
     * @apiNote Use this method only if the GraphExt (swbtse) persistence method is defined. If
     * GraphExt (swbtse) is not used, this method returns null.
     *
     * Counts statements matching given criteria. Uses extended relational schema of Triple Store
     * to execute efficient SQL queries.
     *
     * @param subject {@link Resource} to match in a triple.
     * @param property {@link Property} to match in a triple.
     * @param object {@link RDFNode} to macth in a triple.
     * @param stype String holding URI of subject class.
     * @return StmtIterator iterator.
     */
    //TODO: Review this method because only gets results on specific conditions and may cause confusion to developers
    public long countStatements(Resource subject, Property property, RDFNode object, String stype) {
        Graph g = getRDFModel().getGraph();

        if (g instanceof GraphExt) {
            GraphExt gext = (GraphExt)g;
            TripleMatch tm = Triple.createMatch(
                    subject != null ? subject.asNode() : null,
                    property != null ? property.asNode() : null,
                    object != null ? object.asNode() : null);

            return gext.count(tm, stype);
        }
        return 0;
    }

    /**
     * @deprecated Use {@link #createSemanticProperty(String, SemanticClass, SemanticPropertyType, String)}
     * Creates a {@link SemanticProperty}.
     * @param uri     SemanticProperty URI.
     * @param cls     domain of <code>property</code>
     * @param propType the uri type
     * @param uriRang the uri rang
     * @return the semantic property
     */
    @Deprecated
    public SemanticProperty createSemanticProperty(String uri, SemanticClass cls, String propType, String uriRang) {
        SemanticPropertyType pt = null;
        if (SemanticVocabulary.OWL_DATATYPEPROPERTY.equals(propType)) {
            pt = SemanticPropertyType.DATA;
        } else if (SemanticVocabulary.OWL_OBJECTPROPERTY.equals(propType)) {
            pt = SemanticPropertyType.OBJECT;
        }
        return createSemanticProperty(uri, cls, pt, uriRang);
    }

    /**
     * Creates a {@link SemanticProperty} with domain set to <code>domain</code> and range
     * set to <code>rangeURI</code>. Property type is set according to <code>propType</code>.
     *
     * @param uri       SemanticProperty URI.
     * @param domain    domain class of the {@link SemanticProperty}
     * @param propType  property type
     * @param rangeURI  URI of range for property
     * @return SemanticProperty or null if <code>uri</code> is invalid or null.
     */
    public SemanticProperty createSemanticProperty(String uri, SemanticClass domain, SemanticPropertyType propType, String rangeURI) {
        Model m = getRDFModel();
        OntModel ontm = SWBPlatform.getSemanticMgr().getSchema().getRDFOntModel();
        OntProperty ontprop = null;

        if (null != uri) {
            if (propType == SemanticPropertyType.DATA) {
                ontprop = ontm.createDatatypeProperty(uri);
            } else if (propType == SemanticPropertyType.OBJECT) {
                ontprop = ontm.createObjectProperty(uri);
            }

            if (null != ontprop) {
                ontprop.setDomain(m.getResource(domain.getURI()));
                ontprop.setRange(m.getResource(rangeURI));

                //Add statements to model
                StmtIterator sit = ontm.listStatements(m.getResource(ontprop.getURI()), null, (RDFNode) null);
                m.add(sit);
                sit.close();

                //Update class definition
                domain = new SemanticClass(domain.getOntClass());
                SWBPlatform.getSemanticMgr().getVocabulary().registerClass(domain);

                //TODO: notify this action
                return new SemanticProperty(ontprop);
            }
        }
        return null;
    }

    /**
     * Creates a {@link SemanticClass} with the given <code>uri</code>.
     * @param uri the class URI
     * @return the {@link SemanticClass}
     */
    public SemanticClass createSemanticClass(String uri) {
        Model m = getRDFModel();
        OntModel ontm = SWBPlatform.getSemanticMgr().getSchema().getRDFOntModel();

        Resource res = ontm.getResource(uri);
        Statement st = m.getProperty(res, RDF.type);
        if (null == st) {
            st = m.createStatement(res, RDF.type, OWL.Class);
            m.add(st);
        }
        //TODO: notify this action
        return registerClass(uri);
    }

    /**
     * Adds a {@link SemanticClass} to the {@link SemanticVocabulary}.
     * @param uri the SemanticClass URI
     * @return the {@link SemanticClass}
     */
    public SemanticClass registerClass(String uri) {
        OntModel ontm = SWBPlatform.getSemanticMgr().getSchema().getRDFOntModel();
        OntClass ontcls = ontm.getOntClass(uri);

        SemanticClass cls = new SemanticClass(ontcls);
        SWBPlatform.getSemanticMgr().getVocabulary().registerClass(cls);
        return cls;
    }

    /**
     * Gets a {@link SemanticProperty} from the {@link SemanticVocabulary}.
     * @param uri the SemanticProperty URI
     * @return the {@link SemanticProperty}
     */
    public SemanticProperty getSemanticProperty(String uri) {
        return SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(uri);
    }

    /**
     * Writes a serialization of this model as an "RDF/XML" document.
     * @see Model#write(OutputStream)
     */
    public void write(OutputStream out) {
        write(out, null);
    }


    /**
     * Writes a serialization of this model as an XML document. The language in which to write
     * the model is specified by the <code>lang</code> argument.
     * Predefined values are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value is
     * represented by <code>null</code> is "RDF/XML".
     * @see Model#write(OutputStream, String)
     *
     * @param out  The output stream to which the XML will be written
     * @param lang the lang
     */
    public void write(OutputStream out, String lang) {
        if (model instanceof OntModel) {
            ((OntModel) model).writeAll(out, lang, null);
        } else {
            model.write(out, lang);
        }
    }

    /**
     * Gets value of resource associated to class counters, used to create class IDs.
     * @param name the name used as identifier of counted class.
     * @return the counter value
     */
    public synchronized long getCounterValue(String name) {
        long ret = 0;
        String uri = getNameSpace() + "counter";
        Resource res = getRDFModel().createResource(uri + ":" + name);
        Property prop = getRDFModel().createProperty(uri);
        StmtIterator it = getRDFModel().listStatements(res, prop, (String) null);
        if (it.hasNext()) {
            Statement stmt = it.nextStatement();
            ret = stmt.getLong();
        }
        it.close();
        return ret;
    }

    /**
     * Sets value of resource associated to class counters, used to create class IDs.
     * @param name the name used as identifier of counted class.
     * @param val the counter value
     */
    public synchronized void setCounterValue(String name, long val) {
        String uri = getNameSpace() + "counter";
        Resource res = getRDFModel().createResource(uri + ":" + name);
        Property prop = getRDFModel().createProperty(uri);
        StmtIterator it = getRDFModel().listStatements(res, prop, (String) null);
        if (it.hasNext()) {
            Statement stmt = it.nextStatement();
            stmt.changeLiteralObject(val);
        } else {
            Statement stmt = getRDFModel().createLiteralStatement(res, prop, val);
            getRDFModel().add(stmt);
        }
        it.close();
    }

    /**
     * @deprecated Use {@link #getAndIncrementCounter(SemanticClass)}
     * Gets value of resource associated to class counters, used to create class IDs
     * and increments that value.
     *
     * @param cls the {@link SemanticClass}
     * @return the counter
     */
    @Deprecated
    public synchronized long getCounter(SemanticClass cls) {
        return getAndIncrementCounter(cls);
    }

    /**
     * @deprecated Use {@link #getAndIncrementCounter(String)}
     * Regresa contador en base a la cadena <i>name</i>, e incrementa el valor en uno.
     *
     * @param name the name
     * @return the counter
     */
    @Deprecated
    public synchronized long getCounter(String name) {
        return getAndIncrementCounter(name);
    }

    /**
     * Gets value of resource associated to class counters, used to create class IDs
     * and increments that value.
     *
     * @param name the class name
     * @return the counter
     */
    public synchronized long getAndIncrementCounter(String name) {
        long ret = getCounterValue(name);
        ret++;
        setCounterValue(name, ret);
        return ret;
    }

    /**
     * Gets value of resource associated to class counters, used to create class IDs
     * and increments that value.
     *
     * @param cls the {@link SemanticClass}
     * @return the counter
     */
    public synchronized long getAndIncrementCounter(SemanticClass cls) {
        String uri;
        long id = getAndIncrementCounter(cls.getClassGroupId());
        long tid = id - 1;

        tid++;
        uri = getObjectUri("" + tid, cls);

        if (SemanticObject.createSemanticObject(uri, this) != null) {
            Iterator<Statement> it = this.getRDFModel().listStatements(null, RDF.type, cls.getOntClass());
            while (it.hasNext()) {
                Statement statement = it.next();
                try {
                    id = Long.parseLong(getResourceId(statement.getSubject()));
                } catch (Exception ne) {
                }

                if (id > tid) {
                    tid = id;
                }
            }
            tid++;
        }

        if (id != tid) {
            setCounterValue(cls.getClassGroupId(), tid);
        }
        return tid;
    }

    /**
     * Gets a resource ID from its URI.
     * @return the resource ID
     */
    private String getResourceId(Resource res) {
        String id = res.getURI();
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
     * Deletes the resource associated to counter class.
     * @param name the name of the class
     */
    public synchronized void deleteCounterValue(String name) {
        String uri = getNameSpace() + "counter";
        Resource res = getRDFModel().createResource(uri + ":" + name);
        Property prop = getRDFModel().createProperty(uri);
        getRDFModel().remove(res, prop, null);
    }

    /**
     * Prepares a {@link QueryExecution} object for executing SPARQL 1.1 queries on model or dataset.
     * @param queryString the SPARQL query string
     * @return the {@link QueryExecution} object.
     */
    public QueryExecution sparQLQuery(String queryString) {
        QueryExecution ret;
        Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

        if (model.getGraph() instanceof RemoteGraph) {
            ret = QueryExecutionFactory.sparqlService(((RemoteGraph) model.getGraph()).getUri(), query);
        } else {
            if (dataset != null) {
                ret = QueryExecutionFactory.create(query, dataset);
            } else {
                ret = QueryExecutionFactory.create(query, model);
            }
        }
        return ret;
    }

    /**
     * Prepares a {@link QueryExecution} object for executing SPARQL 1.1 queries on model.
     * @param queryString the SPARQL query string
     * @return the {@link QueryExecution} object.
     */
    public QueryExecution sparQLOntologyQuery(String queryString) {
        return QueryExecutionFactory.create(QueryFactory.create(queryString), getRDFOntModel());
    }

    /**
     * Gets an iterator of {@link SemanticClass} and super classes with URIs matching a SemanticModel.
     * @return iterator of {@link SemanticClass}
     */
    public Iterator<SemanticClass> listModelClasses() {
        SemanticClass cls = getModelObject().getSemanticClass();
        if (modelClasses == null) {
            modelClasses = SWBUtils.Collections.copyIterator(cls.listModelClasses());
            Iterator<SemanticClass> it = cls.listSuperClasses();
            while (it.hasNext()) {
                SemanticClass semanticClass = it.next();
                if (semanticClass.isSWBModel()) {
                    modelClasses.addAll(SWBUtils.Collections.copyIterator(semanticClass.listModelClasses()));
                }
            }

        }
        return modelClasses.iterator();
    }

    /**
     * Checks if a {@link SemanticClass} <code>cls</code> is a SWBModel class.
     * @param cls the {@link SemanticClass}
     * @return true if <code>cls</code> is a SWBModel class.
     */
    public boolean hasModelClass(SemanticClass cls) {
        if (modelClasses == null) {
            listModelClasses();
        }
        return modelClasses.contains(cls);
    }

    /**
     * Gets underlying dataset.
     * @return Dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * Sets underlying dataset.
     * @param dataset the dataset.
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
}
