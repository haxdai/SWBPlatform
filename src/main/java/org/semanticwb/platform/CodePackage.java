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

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import org.semanticwb.SWBPlatform;

import java.util.HashMap;
import java.util.Iterator;

/**
 * The Class CodePackage.
 *
 * @author javier.solis
 */
//TODO: Check why this class is needed.
public class CodePackage {

    /**
     * The map.
     */
    private HashMap<String, String> map;

    /**
     * Instantiates a new code package.
     */
    public CodePackage() {
        map = new HashMap();
    }

    /**
     * Gets the package.
     *
     * @param prefix the prefix
     * @return the package
     */
    public String getPackage(String prefix) {
        String spkg = map.get(prefix); //TODO: Remove conditional because map is always empty. Maybe it was intended to cache packages?
        if (spkg == null) {
            SemanticProperty pfx = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(SemanticVocabulary.SWB_PROP_PREFIX);
            SemanticProperty pkg = SWBPlatform.getSemanticMgr().getVocabulary().getSemanticProperty(SemanticVocabulary.SWB_PROP_PACKAGE);
            Iterator<Resource> it = SWBPlatform.getSemanticMgr().getSchema().getRDFOntModel().listSubjectsWithProperty(pfx.getRDFProperty(), prefix);
            while (it.hasNext()) {
                Resource res = it.next();
                Statement st = res.getProperty(pkg.getRDFProperty());
                if (st != null) {
                    spkg = st.getString();
                }
            }
        }
        return spkg;
    }
}
