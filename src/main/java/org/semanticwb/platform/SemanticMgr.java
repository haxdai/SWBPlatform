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

import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.rdf.AbstractStore;
import org.semanticwb.rdf.GraphCached;
import org.semanticwb.rdf.RemoteGraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.Map.Entry;

/**
 * Class responsible for managing {@link SemanticObject}s (RDF resources) and related ontology.
 *
 * @author Jei
 */
public class SemanticMgr implements SWBInstanceObject {
    /**
     * Supported Ontology Model Specifications.
     * <li>{@link #OWL_MEM} - In memory OWL model, no reasoning</li>
     * <li>{@link #OWL_MEM_TRANS_INF} - In memory OWL model, transitive inference</li>
     * <li>{@link #OWL_LITE_MEM_RDFS_INF} - In memory OWL-LITE model, RDFS inferencer</li>
     * <li>{@link #OWL_MEM_MINI_RULE_INF} - In memory OWL model, mini OWL rules inference</li>
     * <li>{@link #RDFS_MEM_RDFS_INF} - In memory RDFS model, RDFS inferencer</li>
     * <li>{@link #DAML_MEM_RDFS_INF} - In memory DAML model, RDFS inferencer</li>
     * <li>{@link #OWL_DL_MEM_RDFS_INF} - In memory OWL-DL model, RDFS inferencer</li>
     * <li>{@link #OWL_MEM_RDFS_INF} - In memory OWL model, RDFS inferencer</li>
     *
     */
    public enum ModelSchema {
        OWL_MEM,
        OWL_MEM_TRANS_INF,
        OWL_LITE_MEM_RDFS_INF,
        OWL_MEM_MINI_RULE_INF,
        RDFS_MEM_RDFS_INF,
        DAML_MEM_RDFS_INF,
        OWL_DL_MEM_RDFS_INF,
        OWL_MEM_RDFS_INF
    }

    private boolean tripleCache = false;
    private boolean semobjCache = false;
    private ArrayList<String> semobjModelCache = new ArrayList<>();
    private boolean userRepCache = false;
    private ClassLoader classLoader = getClass().getClassLoader();
    private static ModelSchema modelSchema = ModelSchema.OWL_MEM_TRANS_INF;
    private static Logger log = SWBUtils.getLogger(SemanticMgr.class);

    //TODO: Move this constants to SWBAproperties Class in SWBPortal project
    public static final String SWBAdmin = "SWBAdmin";
    public static final String SWBAdminURI = "http://www.semanticwb.org/SWBAdmin#";
    public static final String SWBOntEdit = "SWBOntEdit";

    private SemanticOntology ontology;
    private SemanticOntology schema;
    /**
     * Map of {@link SemanticModel}s arranged by name.
     */
    private HashMap<String, SemanticModel> namedModels = null;

    /**
     * Map of {@link SemanticModel}s arranged by namespace.
     */
    private HashMap<String, SemanticModel> namespacedModels = null;
    private HashMap<Model, SemanticModel> internalModels = null;
    private HashMap<String, SemanticModel> baseModels = null;
    private SemanticVocabulary vocabulary;
    private List<SemanticObserver> modelObservers = null;
    private List<SemanticTSObserver> tsObservers = null;
    private CodePackage codepackage = null;
    private AbstractStore store = null;

    /**
     * Sets Model specification to use by the {@link SemanticMgr}.
     * @param modelSchema Enum value for the Schema.
     */
    public static void setSchemaModel(ModelSchema modelSchema) {
        SemanticMgr.modelSchema = modelSchema;
    }

    /**
     * Gets the Model Specification of the {@link SemanticMgr}.
     * @return Model Specification.
     */
    public OntModelSpec getModelSpec() {
        OntModelSpec modelSpec;

        //Create Schema
        switch (modelSchema) {
            case OWL_MEM:
                modelSpec = OntModelSpec.OWL_MEM;
                break;
            case OWL_DL_MEM_RDFS_INF:
                modelSpec = OntModelSpec.OWL_DL_MEM_RDFS_INF;
                log.event("ModelSpecification: OWL_DL_MEM_RDFS_INF");
                break;
            case OWL_MEM_TRANS_INF:
                modelSpec = OntModelSpec.OWL_MEM_TRANS_INF;
                log.event("ModelSpecification: OWL_MEM_TRANS_INF");
                break;
            case OWL_MEM_MINI_RULE_INF:
                modelSpec = OntModelSpec.OWL_MEM_MINI_RULE_INF;
                log.event("ModelSpecification: OWL_MEM_MINI_RULE_INF");
                break;
            case OWL_MEM_RDFS_INF:
                modelSpec = OntModelSpec.OWL_MEM_RDFS_INF;
                log.event("ModelSpecification: OWL_MEM_RDFS_INF");
                break;
            case RDFS_MEM_RDFS_INF:
                modelSpec = OntModelSpec.RDFS_MEM_RDFS_INF;
                log.event("ModelSpecification: RDFS_MEM_RDFS_INF");
                break;
            case DAML_MEM_RDFS_INF:
                modelSpec = OntModelSpec.DAML_MEM_RDFS_INF;
                log.event("ModelSpecification: DAML_MEM_RDFS_INF");
                break;
            case OWL_LITE_MEM_RDFS_INF:
                modelSpec = OntModelSpec.OWL_LITE_MEM_RDFS_INF;
                log.event("ModelSpecification: OWL_LITE_MEM_RDFS_INF");
                break;
            default:
                modelSpec = OntModelSpec.OWL_MEM_TRANS_INF;
                log.event("ModelSpecification: OWL_MEM_TRANS_INF");
        }
        return modelSpec;
    }

    public void init() {
        log.event("Initializing SemanticMgr...");
        codepackage = new CodePackage();
        namedModels = new HashMap<>();
        namespacedModels = new HashMap<>();
        internalModels = new HashMap<>();   //Arreglo de RDFModel
        baseModels = new HashMap<>();       //Arreglo de RDFModel
        modelObservers = Collections.synchronizedList(new ArrayList<>());
        tsObservers = Collections.synchronizedList(new ArrayList<>());


        OntModelSpec modelSpec = getModelSpec();

        //Create Schema
        schema = new SemanticOntology("SWBSchema", ModelFactory.createOntologyModel(modelSpec));

        //Create Ontology
        ontology = new SemanticOntology("SWBOntology",
                ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF));

        //Agrega ontologia a los modelos
        SemanticModel ontModel = new SemanticModel("swb_ontology", ontology.getRDFOntModel());
        internalModels.put(ontModel.getRDFModel(), ontModel);

        //Agrega esquema a los modelos
        SemanticModel ontSchemaModel = new SemanticModel("swb_schema", schema.getRDFOntModel());

        //para busqueda inversa
        internalModels.put(ontSchemaModel.getRDFModel(), ontSchemaModel);
    }

    /**
     * @deprecated Use {@link #initializeTripleStore(boolean, boolean, String, boolean, String)}
     * getting parameters from configuration files or environment variables.
     *
     * Initialize db.
     */
    @Deprecated
    public void initializeDB() {
        initializeTripleStore(Boolean.parseBoolean(SWBPlatform.getEnv("swb/tripleFullCache", "false")),
                Boolean.parseBoolean(SWBPlatform.getEnv("swb/semanticObjectFullCache", "true")),
                SWBPlatform.getEnv("swb/semanticObjectModelsCache", null),
                Boolean.parseBoolean(SWBPlatform.getEnv("swb/userRepositoryFullCache", "false")),
                SWBPlatform.getEnv("swb/tripleStoreClass", "org.semanticwb.store.leveldb.SWBTSLevelDB"));
    }

    /**
     * Initializes {@link SemanticMgr}'s underlying Triple Store using default configuration.
     */
    public void initializeTripleStore() {
        initializeTripleStore(false, true, null, false, "org.semanticwb.store.leveldb.SWBTSLevelDB");
    }

    /**
     * Initializes {@link SemanticMgr}'s underlying Triple Store.
     * @param triplesFullCache              whether to use full cache of triples.
     * @param semObjectsFullCache           whether to use full cache of semantic objects.
     * @param cachedSemanticObjectModels    String with a delimited list of cached SemanticModels.
     * @param userRepositoryFullCache       whether to use full cache of user repository model.
     * @param genericTripleStoreClass       Qualified class name of TripleStore implementation.
     *                                      Used when persistence type is {@link SWBPlatform#PRESIST_TYPE_SWBTSGEN}.
     */
    public void initializeTripleStore(boolean triplesFullCache, boolean semObjectsFullCache,
                                      String cachedSemanticObjectModels, boolean userRepositoryFullCache,
                                      String genericTripleStoreClass) {

        tripleCache = triplesFullCache;
        semobjCache = semObjectsFullCache;
        userRepCache = userRepositoryFullCache;
        log.event("TripleFullCache:" + tripleCache);
        log.event("SemanticObjectFullCache:" + semobjCache);

        if (cachedSemanticObjectModels != null) {
            log.event("SemanticObjectModelsCache:" + cachedSemanticObjectModels);
            StringTokenizer st = new StringTokenizer(cachedSemanticObjectModels, " ,;");
            while (st.hasMoreTokens()) {
                semobjModelCache.add(st.nextToken());
            }
        }

        String clsname = "org.semanticwb.rdf.RDBStore";
        if (SWBPlatform.isSDB()) {
            clsname = "org.semanticwb.rdf.SDBStore";
        } else if (SWBPlatform.isTDB()) {
            clsname = "org.semanticwb.rdf.TDBStore";
        } else if (SWBPlatform.isBigdata()) {
            clsname = "org.semanticwb.bigdata.BigdataStore";
        } else if (SWBPlatform.isRemotePlatform()) {
            clsname = "org.semanticwb.remotetriplestore.SWBRemoteTripleStore";
        } else if (SWBPlatform.isSWBTripleStore()) {
            clsname = "org.semanticwb.triplestore.SWBTripleStore";
        } else if (SWBPlatform.isSWBTripleStoreExt()) {
            clsname = "org.semanticwb.triplestore.ext.SWBTripleStoreExt";
        } else if (SWBPlatform.isSWBTSMongo()) {
            clsname = "org.semanticwb.triplestore.mongo.SWBTSMongo";
        } else if (SWBPlatform.isSWBTSMongoE()) {
            clsname = "org.semanticwb.triplestore.mongo.ext.SWBTSMongoExt";
        } else if (SWBPlatform.isVirtuoso()) {
            clsname = "org.semanticwb.triplestore.virtuoso.SWBTSVirtuoso";
        } else if (SWBPlatform.isSWBTSGen()) {
            clsname = genericTripleStoreClass;
        } else if (SWBPlatform.isSWBTSGemFire()) {
            clsname = "org.semanticwb.triplestore.gemfire.SWBTSGemFire";
        }

        log.event("TripleStoreClass:" + clsname);

        try {
            Class cls = Class.forName(clsname);
            store = (AbstractStore) cls.newInstance();
        } catch (Exception e) {
            log.error("Error Initializing Store", e);
        }

        store.init();
    }

    /**
     * Loads an ontology model from a path or URI and adds it to the {@link SemanticMgr}'s base ontologies.
     * @param filePathOrURI the ontology file path or URI.
     * @return the semantic model
     */
    public SemanticModel addBaseOntology(String filePathOrURI) {
        log.event("Adding base Ontology: " + filePathOrURI);

        //Load model from file or URI
        Model model = SWBPlatform.getSemanticMgr().loadRDFFileModel(filePathOrURI);
        SemanticModel smodel = new SemanticModel(new File(filePathOrURI).getName(), model);

        //Register model in schema and ontology
        getSchema().addOWLModel(filePathOrURI, smodel, false);
        getOntology().addOWLModel(filePathOrURI, smodel, false);

        //Add model to the base models of SemanticMgr
        baseModels.put(smodel.getName(), smodel);

        //Add new model namespaces to namespace model hashmap
        for (String namespace: smodel.getRDFModel().getNsPrefixMap().values()) {
            namespacedModels.put(namespace, internalModels.get(getSchema().getRDFOntModel()));
            log.debug("Adding Namespace: " + namespace + " " + getSchema().getName());
        }
        return smodel;
    }

    /**
     * Loads classes of the {@link SemanticMgr} schema into a {@link SemanticVocabulary}.
     */
    public void loadBaseVocabulary() {
        //Create Vocabulary
        if (vocabulary == null) {
            vocabulary = new SemanticVocabulary();
        }

        Iterator<SemanticClass> tpcit = new SemanticClassIterator(schema.getRDFOntModel().listClasses(), true);
        while (tpcit.hasNext()) {
            SemanticClass cls = tpcit.next();
            if (cls != null) {
                vocabulary.registerClass(cls, false);
            }
        }
        vocabulary.filterProperties();
        vocabulary.init();
    }

    /**
     * Loads an RDF model making a connection to a defined URI.
     * @param uri the uri of the model.
     * @return the model.
     */
    public Model loadRDFRemoteModel(String uri) {
        try {
            log.info("-->Loading Remote Model: " + uri);
            URLConnection u = new URL(uri).openConnection();
            u.connect();
            return ModelFactory.createModelForGraph(new RemoteGraph(uri));
        } catch (IOException e) {
            log.warn("-->Can´t create remote model: " + uri);
        }
        return null;
    }

    /**
     * Loads an RDF model making a connection to a defined URI. The resulting model is added to
     * the {@link SemanticMgr}'s ontology if <code>add</code> is true.
     * @param name          model name
     * @param uri           model URI
     * @param namespace     model namespace
     * @param add           whether to add loaded model to the {@link SemanticMgr}'s ontology.
     * @return the semantic model
     */
    public SemanticModel loadRemoteModel(String name, String uri, String namespace, boolean add) {
        SemanticModel m = null;
        Model model = loadRDFRemoteModel(uri);
        if (model != null) {
            m = new SemanticModel(name, model);
            m.setNameSpace(namespace);

            //TODO: notify this action
            addModel(m, add);
        }
        return m;
    }

    /**
     * Loads statements from a serialization format in HTML language.
     * @param url URL of remote HTML webpage
     * @return Model
     */
    public Model loadRDFaRemoteModel(String url) {
        return loadRDFaRemoteModel(url, "HTML");
    }

    /**
     * Loads statements from a serialization format in a language.
     * @see Model#read(String, String).
     *
     * @param url   url of remote webpage
     * @param lang  "HTML" or "XHTML", default HTML
     * @return RDF Model
     */
    public Model loadRDFaRemoteModel(String url, String lang) {
        //TODO: Review this code because language description in documentation does not match languages in Jena's Model class.
        Model model = null;
        try {
            Class.forName("net.rootdev.javardfa.RDFaReader");
            model = ModelFactory.createDefaultModel();
            if (lang == null) {
                lang = "HTML";
            }
            model.read(url, lang);
            log.info("-->Loading Remote RDFa Model:" + url);
        } catch (Exception e) {
            log.warn("-->Can´t create remote RDFa model:" + url);
        }
        return model;
    }

    /**
     * Loads statements from a serialization format in a language.
     * @param name  model name
     * @param url   model URL
     * @param lang  serialization language
     * @param add   whether to add loaded model to the {@link SemanticMgr}'s ontology.
     * @return the semantic model
     */
    public SemanticModel loadRemoteRDFaModel(String name, String url, String lang, boolean add) {
        SemanticModel m = null;
        Model model = loadRDFaRemoteModel(url, lang);
        if (model != null) {
            m = new SemanticModel(name, model);
            //TODO: Impĺement submodel creation and addition to ontology when add is true
        }
        return m;
    }

    /**
     * Loads and RDF model from a file or URI.
     * @see FileManager#loadModel(String);
     *
     * @param path the path
     * @return the model
     */
    public Model loadRDFFileModel(String path) {
        return FileManager.get().loadModel(path);
    }

    /**
     * Loads an RDF model from a file or URI using a base URI.
     * @see FileManager#loadModel(String, String, String)
     *
     * @param filenameOrURI the file path or URI of the model
     * @param baseURI       the base URI
     * @return the model
     */
    public Model loadRDFFileModel(String filenameOrURI, String baseURI) {
        return FileManager.get().loadModel(filenameOrURI, baseURI, null);
    }

    /**
     * Builds a {@link SemanticModel} loading a RDF model.
     *
     * @param name model name
     * @param path model path or URI.
     * @return the semantic model
     */
    public SemanticModel readRDFFile(String name, String path) {
        SemanticModel ret = null;
        Model m = loadRDFFileModel(path);
        if (m != null) {
            ret = new SemanticModel(name, m);
        }
        return ret;
    }

    /**
     * Loads an RDF model from the underlying Triple Store.
     *
     * @param name model name
     * @return the model
     */
    private Model loadRDFDBModel(String name) {
        return store.loadModel(name);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (store != null) {
            store.close();
        }
        log.event("SemanticMgr stopped...");
    }

    /**
     * Gets the namedModels.
     * @return the namedModels
     */
    public Set<Entry<String, SemanticModel>> getModels() {
        return namedModels.entrySet();
    }

    /**
     * Gets a model.
     * @param name model name
     * @return the model
     */
    public SemanticModel getModel(String name) {
        return namedModels.get(name);
    }

    /**
     * Gets a model.
     * @param nameSpace model namespace
     * @return the model
     */
    public SemanticModel getModelByNS(String nameSpace) {
        return namespacedModels.get(nameSpace);
    }

    /**
     * Gets a model.
     * @param model the model
     * @return the model
     */
    public SemanticModel getModel(Model model) {
        return internalModels.get(model);
    }

    /**
     * List base models.
     * @return the iterator
     */
    public Iterator<SemanticModel> listBaseModels() {
        return baseModels.values().iterator();
    }

    /**
     * @deprecated for naming conventions. Use {@link #loadTripleStoreModels()}.
     * Loads RDF models from the underlying Triple Store into memory.
     */
    @Deprecated
    public void loadDBModels() {
        loadTripleStoreModels();
    }

    /**
     * Loads RDF models from the underlying Triple Store into memory.
     */
    public void loadTripleStoreModels() {
        log.debug("loadDBModels");

        //LoadModels
        Iterator<String> it = store.listModelNames();
        while (it.hasNext()) {
            String name = it.next();
            log.trace("Loading model: " + name);
            SemanticModel model = loadTripleStoreModel(name);
            model.setDataset(store.getDataset(name));

            if ((semobjCache || semobjModelCache.contains(name)) &&
                    !(model.getRDFModel().getGraph() instanceof GraphCached)) {
                //Se cambia cache de grafo por cache de semanticObjects
                if (userRepCache || !name.endsWith("_usr")) {
                    log.event("Loading SemanticObject:" + name + " FullCache");
                    SemanticObject.loadFullCache(model);
                }
            }
        }
    }

    /**
     * @deprecated for naming conventions. Use {@link #loadTripleStoreModel(String)}
     * Loads or creates a named {@link SemanticModel}.
     * @param name model name
     * @return the model
     */
    @Deprecated
    private SemanticModel loadDBModel(String name) {
        return loadTripleStoreModel(name);
    }

    /**
     * Loads or creates a named {@link SemanticModel}.
     * @param name model name
     * @return the model
     */
    private SemanticModel loadTripleStoreModel(String name) {
        return loadTripleStoreModel(name, tripleCache);
    }

    /**
     * @deprecated for naming conventions. Use {@link #loadTripleStoreModel(String, boolean)}.
     * @param name model name
     * @param cached whether to cache the {@link SemanticModel}
     * @return the model
     */
    @Deprecated
    private SemanticModel loadDBModel(String name, boolean cached) {
        return loadTripleStoreModel(name, cached);
    }

    /**
     * Loads or creates a named {@link SemanticModel}.
     * @param name model name
     * @param cached whether to cache the {@link SemanticModel}
     * @return the model
     */
    private SemanticModel loadTripleStoreModel(String name, boolean cached) {
        Model model = loadRDFDBModel(name);
        return loadTripleStoreModel(name, model, cached);
    }

    /**
     * @deprecated for naming conventions. Use {@link #loadTripleStoreModel(String, Model, boolean)}.
     *
     * @param name      model name
     * @param cached    whether to cache the {@link SemanticModel}
     * @return the SemanticModel
     */
    @Deprecated
    private SemanticModel loadDBModel(String name, Model model, boolean cached) {
        return loadTripleStoreModel(name, model, cached);
    }

    /**
     * Loads SWBOntEdit N-TRIPLES model.
     * @param model Model where SWBOntEdit is to be loaded.
     * @param ntFile Path to N-TRIPLES file.
     */
    private void loadSWBOntEditModel(Model model, String ntFile) {
        if (SWBPlatform.createInstance().isAdminDev()) {
            NsIterator it = model.listNameSpaces();
            if (!it.hasNext()) {
                log.info("Importing SWBOntEdit...");
                it.close();
                try (FileInputStream in = new FileInputStream(ntFile)) {
                    if (model instanceof ModelRDB) {
                        ModelRDB m = (ModelRDB) model;
                        try {
                            m.begin();
                            m.read(in, null, "N-TRIPLE");
                        } catch (Exception e) {
                            log.error(e);
                        } finally {
                            m.commit();
                        }
                    } else {
                        model.read(in, null, "N-TRIPLE");
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        } else {
            log.info("Loading SWBOntEdit in editing mode...");
            OntModel omodel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, model);
            try {
                Model m = ModelFactory.createDefaultModel();
                FileInputStream in = new FileInputStream(ntFile);
                m.read(in, null, "N-TRIPLE");
                omodel.addSubModel(m, true);
                in.close();
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
            model = omodel;
        }
    }

    /**
     * Loads SWBAdmin N-TRIPLES model.
     * @param model Model where SWBAdmin is to be loaded.
     * @param ntFile Path to N-TRIPLES file.
     */
    private void loadSWBAdminModel(Model model, String ntFile) {
        if (!SWBPlatform.createInstance().isAdminDev()) {
            List ns = SWBUtils.Collections.copyIterator(model.listNameSpaces());

            if (ns.size() <= 1) { // verifica que no exista mas de un namespace
                log.info("Importing SWBAdmin...");
                try (FileInputStream in = new FileInputStream(ntFile)) {
                    if (model.supportsTransactions()) {
                        ModelRDB m = (ModelRDB) model;
                        try {
                            m.begin();
                            m.read(in, null, "N-TRIPLE");
                        } catch (Exception e) {
                            log.error(e);
                        } finally {
                            m.commit();
                        }
                    } else {
                        model.read(in, null, "N-TRIPLE");
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        } else {
            log.info("Loading SWBAdmin model in editing mode...");

            OntModel omodel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, model);
            try {
                Model m = ModelFactory.createDefaultModel();
                FileInputStream in = new FileInputStream(ntFile);
                m.read(in, null, "N-TRIPLE");
                omodel.addSubModel(m, true);
                in.close();
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
            model = omodel;
        }
    }

    /**
     * Loads or creates a {@link SemanticModel}. Checks if SWBAdmin or SWBOntEdit models are requested.
     *
     * @param name      model name
     * @param cached    whether to cache the {@link SemanticModel}
     * @return the SemanticModel
     */
    private SemanticModel loadTripleStoreModel(String name, Model model, boolean cached) {
        if (cached) {
            log.info("Loading cached Model: " + name);
            model = new ModelCom(new GraphCached((model.getGraph())));
        }

        //TODO: Move loading of specific SWBPortal models (SWBAdmin, SWBOntEdit) outside SWBPlatform.
        if (SWBAdmin.equals(name)) {
            loadSWBAdminModel(model, SWBUtils.getApplicationPath() + SWBPlatform.createInstance().getAdminFile());
        }

        if (SWBOntEdit.equals(name)) {
            loadSWBOntEditModel(model, SWBUtils.getApplicationPath() + SWBPlatform.createInstance().getOntEditFile());
        }

        SemanticModel m = null;

        //Verificar si es una ontologia
        Resource res = model.getResource(model.getNsPrefixURI(name) + name);
        StmtIterator it = res.listProperties(RDF.type);
        while (it.hasNext()) {
            Statement stm = it.next();
            Resource type = stm.getResource();
            if (type != null && type.getLocalName().equals("Ontology")) {
                model = new ModelCom(new GraphCached((model.getGraph())));
                m = new SemanticModel(name, model);
            }
        }
        it.close();

        //Si no es una ontología
        if (m == null) {
            m = new SemanticModel(name, model);
        }

        //TODO:notify this action
        addModel(m, true);
        return m;
    }

    /**
     * Adds a model to the {@link SemanticMgr} models.
     * @param model Model
     * @param add   whether to add the model to the {@link SemanticMgr}'s ontology.
     */
    public void addModel(SemanticModel model, boolean add) {
        log.debug("Add NS:" + model.getNameSpace() + " " + model.getName());

        namedModels.put(model.getName(), model);
        namespacedModels.put(model.getNameSpace(), model);
        internalModels.put(model.getRDFModel(), model);
        if (add) {
            ontology.addSubModel(model, false);
        }
    }

    /**
     * Creates a model in the underlying Triple Store.
     *
     * @param name      the name
     * @param nameSpace the name space
     * @return the semantic model
     */
    public SemanticModel createModel(String name, String nameSpace) {
        return createTripleStoreModel(name, nameSpace, tripleCache);
    }

    /**
     * @deprecated for naming conventions. Use {@link #createTripleStoreModel(String, String, boolean)}.
     * Creates a model in the underlying Triple Store.
     *
     * @param name      the name
     * @param nameSpace the name space
     * @param cached    the cached
     * @return the semantic model
     */
    @Deprecated
    public SemanticModel createDBModel(String name, String nameSpace, boolean cached) {
        return createTripleStoreModel(name, nameSpace, cached);
    }

    /**
     * Creates a model in the underlying Triple Store.
     *
     * @param name      model name
     * @param nameSpace model name space
     * @param cached    whether to cache model
     * @return the semantic model
     */
    public SemanticModel createTripleStoreModel(String name, String nameSpace, boolean cached) {
        //Limpiar Cache
        SemanticObject.clearCache();

        Model model = loadRDFDBModel(name);
        model.setNsPrefix(name, nameSpace);
        return loadTripleStoreModel(name, model, cached);
    }

    /**
     * @deprecated for naming conventions. Use {@link #createTripleStoreModelByRDF(String, String, InputStream)}
     * Creates the model by rdf.
     *
     * @param name      the name
     * @param namespace the namespace
     * @param in        the in
     * @return the semantic model
     */
    @Deprecated
    public SemanticModel createDBModelByRDF(String name, String namespace, InputStream in) {
        return createTripleStoreModelByRDF(name, namespace, in, null);
    }

    /**
     * Creates a model by loading RDF statements from an {@link InputStream}.
     *
     * @param name      model name
     * @param namespace model namespace
     * @param in        {@link InputStream} object
     * @return the semantic model
     */
    public SemanticModel createTripleStoreModelByRDF(String name, String namespace, InputStream in) {
        return createTripleStoreModelByRDF(name, namespace, in, null);
    }

    /**
     * @deprecated for naming conventions. Use {@link #createTripleStoreModelByRDF(String, String, InputStream, String)}
     *
     * @param name      the name
     * @param namespace the namespace
     * @param in        the in
     * @param lang      the lang
     * @return the semantic model
     */
    @Deprecated
    public SemanticModel createDBModelByRDF(String name, String namespace, InputStream in, String lang) {
        return createTripleStoreModelByRDF(name, namespace, in, lang);
    }

    /**
     * Creates a model by loading RDF statements from an {@link InputStream}.
     *
     * @param name      model name
     * @param namespace model namespace
     * @param in        {@link InputStream} object
     * @param lang      model language
     * @return the semantic model
     */
    public SemanticModel createTripleStoreModelByRDF(String name, String namespace, InputStream in, String lang) {
        //Create SemanticModel and get RDF model
        SemanticModel ret = createModel(name, namespace);
        Model model = ret.getRDFModel();

        //Read statements into model
        if (model.supportsTransactions()) {
            model.begin();
        }

        try {
            model.read(in, null, lang);
            in.close();
        } catch (IOException ioex) {
            log.error(ioex);
        }

        if (model.supportsTransactions()) {
            model.commit();
        }
        return ret;
    }

    /**
     * Removes a named model.
     * @param name model name
     */
    public void removeModel(String name) {
        SemanticModel model = namedModels.get(name);
        //TODO: notify this action
        namedModels.remove(name);

        namespacedModels.remove(model.getNameSpace());
        internalModels.remove(model.getRDFModel());
        ontology.removeSubModel(model, true);

        store.removeModel(name);
    }

    /**
     * Gets the {@link SemanticMgr}'s ontology.
     * @return the ontology
     */
    public SemanticOntology getOntology() {
        return ontology;
    }

    /**
     * Gets the {@link SemanticMgr}'s schema.
     * @return the schema
     */
    public SemanticOntology getSchema() {
        return schema;
    }

    /**
     * Gets the {@link SemanticMgr}'s vocabulary.
     * @return the vocabulary
     */
    public SemanticVocabulary getVocabulary() {
        return vocabulary;
    }

    /**
     * Registers a model observer.
     * @param observer the observer
     */
    public void registerObserver(SemanticObserver observer) {
        modelObservers.add(observer);
    }

    /**
     * Removes a model observer.
     * @param observer the observer
     */
    public void removeObserver(SemanticObserver observer) {
        modelObservers.remove(observer);
    }

    /**
     * Registers a Triple Store observer.
     * @param observer the observer
     */
    public void registerTSObserver(SemanticTSObserver observer) {
        tsObservers.add(observer);
    }

    /**
     * Removes a TripleStore observer.
     * @param observer the observer
     */
    public void removeTSObserver(SemanticTSObserver observer) {
        tsObservers.remove(observer);
    }


    /**
     * Notifies a change in a model to registered observers.
     *
     * @param obj       the changed {@link SemanticObject}
     * @param prop      the changed {@link SemanticProperty}
     * @param lang      the language
     * @param action    the action that triggered the change
     */
    public void notifyChange(SemanticObject obj, Object prop, String lang, String action) {
        //Return inmediately if object is not provided or property is not observable
        if (null == obj.getURI() || (prop instanceof SemanticProperty && ((SemanticProperty) prop).isNotObservable())) {
            return;
        }

        //Notifies Model observers
        for (SemanticObserver observer : modelObservers) {
            try {
                observer.notify(obj, prop, lang, action);
            } catch (Exception e) {
                log.error(e);
            }
        }

        //Notifies SemanticClass observers
        SemanticClass cls = obj.getSemanticClass();
        if (cls != null) {
            try {
                cls.notifyChange(obj, prop, lang, action);
            } catch (Exception e) {
                log.error(e);
            }
        }

        //Notifies SemanticProperty observers
        if (prop instanceof SemanticProperty) {
            try {
                ((SemanticProperty) prop).notifyChange(obj, prop, lang, action);
            } catch (Exception e) {
                log.error(e);
            }
        }
    }


    /**
     * Notifies a change in a local Triple Store to registered observers.
     *
     * @param obj       the changed {@link SemanticObject}
     * @param stmt      the changed statement
     * @param action    the action that triggered the change
     */
    public void notifyTSChange(SemanticObject obj, Statement stmt, String action) {
        notifyTSChange(obj, stmt, action, false);
    }

    /**
     * Notifies a change in a Triple Store to registered observers.
     *
     * @param obj       the changed {@link SemanticObject}
     * @param stmt      the changed statement
     * @param action    the action that triggered the change
     * @param remote    whether the changed TripleStore is not local
     */
    public void notifyTSChange(SemanticObject obj, Statement stmt, String action, boolean remote) {
        if (obj != null && obj.getURI() != null) {
            for(SemanticTSObserver observer : tsObservers) {
                try {
                    observer.notify(obj, stmt, action, remote);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }

    /**
     * Triggers an action on SemanticObject's RDF statements to add or delete a {@link Node} as a property.
     *
     * @param objURI    changed object URI
     * @param propURI   changed property URI
     * @param action    the action (one of {@link SemanticObject#ACT_ADD} or SemanticObject#ACT_REMOVE).
     */
    public void processExternalChange(String objURI, String propURI, Node node, String action) {
        //Get related Semantic Object
        SemanticObject obj = SemanticObject.getSemanticObjectFromCache(objURI);
        if (obj == null) {
            SemanticObject.clearNotFoundURI(objURI);
            obj = SemanticObject.createSemanticObject(objURI);
        }

        if (obj != null) {
            Model model = obj.getModel().getRDFModel();
            SemanticProperty prop = null;
            Statement stmt = null;

            //Get related SemanticProperty
            if (propURI != null) {
                prop = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(propURI);
            }

            //TODO: Check NPEs and consistency of additions and deletions
            if (action.equals(SemanticObject.ACT_ADD)) {
                stmt = model.createStatement(obj.getRDFResource(), prop.getRDFProperty(), model.asRDFNode(node));
                obj.addStatement(stmt, true);
            } else if (action.equals(SemanticObject.ACT_REMOVE)) {
                if (propURI != null) {
                    stmt = model.createStatement(obj.getRDFResource(), prop.getRDFProperty(), model.asRDFNode(node));
                    obj.remove(stmt, true);
                } else {
                    obj.remove(true);
                }
            }
            SWBPlatform.getSemanticMgr().notifyTSChange(obj, stmt, action, true);
        }
    }


    /**
     * Gets the {@link SemanticMgr}'s code package.
     * @return CodePackage.
     */
    public CodePackage getCodePackage() {
        return codepackage;
    }

    /**
     * Closes all named models.
     */
    public void close() {
        store.close();
        store = null;
    }

    /**
     * Destroys {@link SemanticMgr}.
     */
    public void destroy() {
        close();
    }

    /**
     * Creates a key pair and stores it in SWBAdmin model.
     */
    public void createKeyPair() {
        //TODO: Move this method outside SemanticMgr because is SWBAdmin site specific and each call changes Model's keypair.
        SemanticModel model = getModel(SWBAdmin);
        if (null != model && null != model.getModelObject()) {
            String[] llaves = SWBUtils.CryptoWrapper.storableKP();
            SemanticProperty priv = model.createSemanticProperty(SWBAdminURI + "/PrivateKey", model.getModelObject().getSemanticClass(), SemanticVocabulary.OWL_DATATYPEPROPERTY, SemanticVocabulary.XMLS_STRING);
            SemanticProperty publ = model.createSemanticProperty(SWBAdminURI + "/PublicKey", model.getModelObject().getSemanticClass(), SemanticVocabulary.OWL_DATATYPEPROPERTY, SemanticVocabulary.XMLS_STRING);
            model.getModelObject().setProperty(priv, llaves[0]);
            model.getModelObject().setProperty(publ, llaves[1]);
            log.debug("New KeyPair created... ");
        }
    }

    /**
     * Gets the underlying Triple Store.
     * @return AbstractStore.
     */
    public AbstractStore getSWBStore() {
        return store;
    }

    /**
     * Gets the tripleCache property.
     * @return tripleCache property.
     */
    public boolean isTripleFullCache() {
        return tripleCache;
    }

    /**
     * Gets the {@link SemanticMgr} class loader.
     * @return ClassLoader.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets the {@link SemanticMgr} class loader.
     * @param classLoader {@link ClassLoader}
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Causes the inference model of the schema and ontology of the {@link SemanticMgr}
     * to reconsult the underlying data to take into account changes.
     *
     * @see InfModel#rebind()
     */
    public void rebind() {
        getSchema().rebind();
        getOntology().rebind();
    }
}
