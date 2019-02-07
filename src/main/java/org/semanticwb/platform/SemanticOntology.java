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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.model.GenericObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * High level wrapper class for an Ontology Model.
 *
 * @author Jei
 */
public class SemanticOntology {

    /**
     * The LOG.
     */
    private static final Logger LOG = SWBUtils.getLogger(SemanticOntology.class);

    /**
     * The ontology model.
     */
    private OntModel ontology;

    /**
     * The ontology name.
     */
    private String name;

    /**
     * The ontology sub models.
     */
    private ArrayList<SemanticModel> subModels = new ArrayList<>();

    private HashMap<String, SemanticModel> ontologies = new HashMap<>();

    /**
     * Constructor. Creates a new instance of a {@link SemanticOntology}.
     *
     * @param name      ontology name
     * @param ontology the ontology model
     */
    public SemanticOntology(String name, OntModel ontology) {
        this.ontology = ontology;
        this.name = name;
        init();
    }

    /**
     * Inits the {@link SemanticOntology}.
     */
    private void init() {

    }

    /**
     * Gets the ontology name.
     *
     * @return the ontology name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the ontology {@link OntModel}.
     *
     * @return the {@link OntModel}
     */
    public OntModel getRDFOntModel() {
        return ontology;
    }

    /**
     * Adds a {@link SemanticModel} as submodel of the SemanticOntology.
     *
     * @param model  the model
     * @param rebind whether to rebind model.
     */
    public void addSubModel(SemanticModel model, boolean rebind) {
        subModels.add(model);
        ontology.addSubModel(model.getRDFModel(), rebind);
    }

    /**
     * Adds a {@link SemanticModel} with the given name as submodel of the SemanticOntology.
     *
     * @param owl  the model name
     * @param model  the model
     * @param rebind whether to rebind model
     * @return true if model is added.
     */
    public boolean addOWLModel(String owl, SemanticModel model, boolean rebind) {
        if (!ontologies.containsKey(owl)) {
            ontologies.put(owl, model);

            subModels.add(model);
            ontology.add(model.getRDFModel());
            ontology.setNsPrefixes(model.getRDFModel().getNsPrefixMap());

            if (rebind) {
                ontology.rebind();
            }
            return true;
        }
        return false;
    }

    /**
     * Removes <code>model</code> as submodel of SemanticOntology.
     *
     * @param model  the model
     * @param rebind whether to rebind model
     */
    public void removeSubModel(SemanticModel model, boolean rebind) {
        subModels.remove(model);
        ontology.removeSubModel(model.getRDFModel(), rebind);
    }

    /**
     * Gets an iterator of all SemanticOntology submodels.
     *
     * @return the iterator of SemanticModel
     */
    public Iterator<SemanticModel> listSubModels() {
        return subModels.iterator();
    }

    /**
     * Rebinds the SemanticOntology.
     * @see OntModel#rebind()
     */
    public void rebind() {
        ontology.rebind();
    }

    //TODO: Mejorar performance
    /**
     * Gets a Resource from this SemanticOntology.
     *
     * @param uri the resource URI.
     * @return the resource
     */
    public Resource getResource(String uri) {
        LOG.debug("getResource:" + uri);
        if (uri == null || uri.length() == 0) {
            return null;
        }

        Resource ret = null;
        Property type = SWBPlatform.getSemanticMgr().getVocabulary()
                .getSemanticProperty(SemanticVocabulary.RDF_TYPE).getRDFProperty();

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
                if (model.getRDFModel().contains(res, type)) {
                    ret = res;
                }
            }
        }

        if (ret == null) {
            LOG.trace("getResource in Schema(2):");
            Model model = SWBPlatform.getSemanticMgr().getSchema().getRDFOntModel();
            Resource res = model.getResource(uri);
            if (model.contains(res, type)) {
                ret = res;
            }
        }

        if (ret == null) {
            LOG.trace("getResource in All Model(3):");
            Iterator<Entry<String, SemanticModel>> it = SWBPlatform.getSemanticMgr().getModels().iterator();
            while (it.hasNext()) {
                Entry<String, SemanticModel> ent = it.next();
                SemanticModel model = ent.getValue();
                Resource res = model.getRDFModel().getResource(uri);
                if (model.getRDFModel().contains(res, type)) {
                    ret = res;
                }
                if (ret != null) {
                    break;
                }
            }
        }
        if (ret == null) {
            LOG.trace("getResource in Ontology(4):");
        }
        return ret;
    }

    /**
     * Gets a {@link SemanticObject} from the SemanticOntology.
     *
     * @param uri the SemanticObject URI
     * @return the SemanticObject
     */
    public SemanticObject getSemanticObject(String uri) {
        return SemanticObject.createSemanticObject(uri);
    }

    /**
     * Gets a {@link GenericObject} from the SemanticOntology.
     *
     * @param uri the GenericObject URI
     * @return the GenericObject
     */
    public GenericObject getGenericObject(String uri) {
        SemanticObject sobj = getSemanticObject(uri);
        if (sobj != null) {
            return sobj.createGenericInstance();
        }
        return null;
    }

    /**
     * Gets a {@link GenericObject} of type <code>cls</code> from the SemanticOntology.
     *
     * @param uri the GenericObject URI
     * @param uri the {@link SemanticClass} of the GenericObject
     * @return the GenericObject
     */
    public GenericObject getGenericObject(String uri, SemanticClass cls) {
        SemanticObject obj = getSemanticObject(uri);
        if (obj != null) {
            return cls.newGenericInstance(obj);
        }
        return null;
    }

    /**
     * Gets an iterator of {@link SemanticObject}s that are instances of <code>cls</code> in all
     * resources of the SemanticOntology.
     *
     * @param cls the {@link SemanticClass}
     * @return the iterator of SemanticObject
     */
    public Iterator<SemanticObject> listInstancesOfClass(SemanticClass cls) {
        Property rdf = ontology.getProperty(SemanticVocabulary.RDF_TYPE);
        StmtIterator stit = ontology.listStatements(null, rdf, cls.getOntClass());
        return new SemanticIterator(stit, true);
    }

    /**
     * Gets the {@link SemanticProperty} with URI equals to <code>uri</code>.
     *
     * @param uri the {@link SemanticProperty} URI.
     * @return the semantic property
     */
    public SemanticProperty getSemanticProperty(String uri) {
        return SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(uri);
    }
}
