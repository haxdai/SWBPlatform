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

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import org.semanticwb.SWBPlatform;

import java.util.Iterator;


/**
 * The Class SemanticClassIterator.
 *
 * @param <T> the generic type
 * @author Jei
 */
public class SemanticClassIterator<T extends SemanticClass> implements Iterator {

    /**
     * The mIt.
     */
    private Iterator<SemanticClass> mIt;

    /**
     * The tmp.
     */
    private SemanticClass tmp = null;

    /**
     * The next.
     */
    private boolean next = false;

    /**
     * The retnext.
     */
    private boolean retnext = false;

    private boolean newInstances = false;


    /**
     * Instantiates a new semantic class iterator.
     *
     * @param it the it
     */
    public SemanticClassIterator(Iterator it) {
        this.mIt = it;
    }

    /**
     * Instantiates a new semantic class iterator.
     *
     * @param it the it
     */
    public SemanticClassIterator(Iterator it, boolean newInstances) {
        this.mIt = it;
        this.newInstances = newInstances;
    }

    public boolean hasNext() {
        if (!next) {
            boolean ret = mIt.hasNext();
            if (ret) {
                tmp = _next();
                if (tmp == null) {
                    ret = hasNext();
                }
            }
            next = true;
            retnext = ret;
        }

        if (!retnext && mIt instanceof ClosableIterator) {
            ((ClosableIterator) mIt).close();
        }

        return retnext;
    }

    /**
     * _next.
     *
     * @return the semantic class
     */
    private SemanticClass _next() {
        Object obj = mIt.next();
        SemanticClass cls = null;

        if (obj instanceof Statement) {
            if (newInstances) {
                OntClass ocls;
                OntModel om = null;
                Model m = ((Statement) obj).getModel();

                if (m instanceof OntModel) {
                    om = (OntModel) m;
                }
                if (om != null) {
                    ocls = om.createClass(((Statement) obj).getResource().getURI());
                } else {
                    ocls = SWBPlatform.getSemanticMgr().getSchema()
                            .getRDFOntModel().createClass(((Statement) obj).getResource().getURI());
                }
                if (ocls != null) {
                    cls = new SemanticClass(ocls);
                }
            } else {
                cls = SWBPlatform.getSemanticMgr().getVocabulary()
                        .getSemanticClass(((Statement) obj).getResource().getURI());
            }
        } else {
            OntClass ocls = (OntClass) obj;

            if (newInstances) {
                cls = new SemanticClass(ocls);
            } else {
                cls = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticClass(ocls.getURI());
            }
        }
        return cls;
    }

    public T next() {
        if (!next) {
            hasNext();
        }
        next = false;
        return (T) tmp;
    }

    public void remove() {
        mIt.remove();
    }
}
