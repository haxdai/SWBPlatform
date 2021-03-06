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
package org.semanticwb.rdf;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

import java.util.Iterator;

/**
 * Interface that defines methods to be implemented by different Triple Stores.
 *
 * @author jei
 */
public interface AbstractStore {
    /**
     * Initializes store internal state.
     */
    void init();

    /**
     * Removes a model with matching <code>name</code> from store.
     * @param name Model name.
     */
    void removeModel(String name);

    /**
     * Loads model with matching <coode>name</coode> to store.
     * @param name Model name
     * @return loaded Model
     */
    Model loadModel(String name);

    /**
     * Gets a model with matching <coode>name</coode> from store.
     * @param name Model name
     * @return loaded Model
     */
    Model getModel(String name);

    /**
     * Gets an iterator to existing model names..
     * @return Iterator of model names.
     */
    Iterator<String> listModelNames();

    /**
     * Closes store.
     */
    void close();

    /**
     * Gets internal dataset of Triple Store matching <code>name</code>.
     * @param name name
     * @return DataSet
     */
    Dataset getDataset(String name);
}
