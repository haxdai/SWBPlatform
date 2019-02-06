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

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import org.semanticwb.Logger;
import org.semanticwb.SWBRuntimeException;
import org.semanticwb.SWBUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Iterator implementation for {@link SemanticObject}.
 *
 * @param <T> the generic type
 * @author victor.lorenzana
 */
public class SemanticIterator<T extends SemanticObject> implements Iterator {
    private static Logger LOG = SWBUtils.getLogger(SemanticIterator.class);

    /**
     * The internal iterator.
     */
    private Iterator iterator;
    private boolean invert;

    private SemanticModel model = null;
    private SemanticClass cls = null;

    /**
     * Creates a new {@link SemanticIterator} using a generic {@link Iterator}.
     *
     * @param iterator the {@link Iterator}
     */
    public SemanticIterator(Iterator iterator) {
        this(iterator, false);
    }

    /**
     * Creates a new {@link SemanticIterator} using a generic {@link Iterator}.
     *
     * @param iterator the {@link Iterator}
     * @param invert   whether to check inverse properties on Resources.
     */
    public SemanticIterator(Iterator iterator, boolean invert) {
        this.iterator = iterator;
        if (this.iterator == null) {
            this.iterator = new ArrayList().iterator();
        }
        this.invert = invert;
    }

    /**
     * Creates a new {@link SemanticIterator} using a generic {@link Iterator}.
     *
     * @param iterator the {@link Iterator}
     * @param invert   whether to check inverse properties on Resources.
     * @param model    the objects model
     * @param model    the objects class
     */
    public SemanticIterator(Iterator iterator, boolean invert, SemanticModel model, SemanticClass cls) {
        //TODO: Check why model and cls are needed. They are not used anywhere in code
        this(iterator, invert);
        this.model = model;
        this.cls = cls;
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     *
     * @return true if the iteration has more elements.
     * @see Iterator#hasNext()
     */
    public boolean hasNext() {
        boolean ret = iterator.hasNext();
        if (!ret && iterator instanceof ClosableIterator) {
            ((ClosableIterator) iterator).close();
        }
        return ret;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return next element in the iteration.
     */
    public T next() {
        Object obj = iterator.next();

        if (obj instanceof Statement) {
            try {
                if (invert) {
                    T aux = (T) SemanticObject.createSemanticObject(((Statement) obj).getSubject());
                    if (aux == null) {
                        LOG.warn("Remove bad statement from cache: " + obj);
                        if (((Statement) obj).getObject().isResource()) {
                            SemanticObject o = SemanticObject
                                    .getSemanticObjectFromCache(((Statement) obj).getResource().getURI());

                            if (o != null) {
                                o.removeInv((Statement) obj);
                            }
                        }
                    }
                    return aux;
                } else {
                    T aux = (T) SemanticObject.createSemanticObject(((Statement) obj).getResource());
                    if (aux == null) {
                        LOG.warn("Remove bad statement from cache:" + obj);
                        SemanticObject o = SemanticObject
                                .getSemanticObjectFromCache(((Statement) obj).getSubject().getURI());

                        if (o != null) {
                            o.remove((Statement) obj, true);
                        }
                    }
                    return aux;
                }
            } catch (SWBRuntimeException re) {
                LOG.error(re);
                //TODO: Check convenience to use a ResourceNotFoundException instead of comparing exception message
                if (re.getMessage().startsWith("Resource not Found")) {
                    LOG.warn("Removing bad link:" + ((Statement) obj).getResource());
                    ((Statement) obj).remove();
                }
                return null;
            } catch (Exception ie) {
                LOG.error(ie);
                return null;
            }
        } else if (obj instanceof Resource) {
            try {
                return (T) SemanticObject.createSemanticObject((Resource) obj);
            } catch (Exception ie) {
                throw new AssertionError(ie.getMessage());
            }
        } else if (obj instanceof String) {
            try {
                return (T) SemanticObject.createSemanticObject((String) obj);
            } catch (Exception ie) {
                throw new AssertionError(ie.getMessage());
            }
        } else {
            throw new AssertionError("No type found...");
        }
    }
}