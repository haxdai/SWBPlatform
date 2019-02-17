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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Utility class used to filter properties of a {@link SemanticObject} primarily for display purposes.
 *
 * @author javier.solis
 */
public class SWBObjectFilter {

    /**
     * Filter an iterator of {@link SemanticObject} using a matching property filter String.
     *
     * @param objects Iterator of {@link SemanticObject} to filter.
     * @param filter  String property filter.
     * @return Iterator of filtered {@link SemanticObject}.
     */
    public static Iterator<SemanticObject> filter(Iterator<SemanticObject> objects, String filter) {
        if (filter != null) {
            ArrayList<SemanticObject> arr = new ArrayList<>();
            while (objects.hasNext()) {
                SemanticObject obj = objects.next();
                if (filter(obj, filter)) {
                    arr.add(obj);
                }
            }
            return arr.iterator();
        } else {
            return objects;
        }
    }

    /**
     * Filters properties on a {@link SemanticObject} using a filter string.
     * Filter rules are separated by character ",", ";", "|", or "&".
     * Valid comparisons are equality ("==" , "=") or inequality ("!=", "<>").
     *
     * @param obj       {@link SemanticObject} to filter properties from
     * @param filter    Filter string
     * @return boolean  when filter rule is evaluated to true
     */
    public static boolean filter(SemanticObject obj, String filter) {
        if (filter == null) {
            return true;
        }

        boolean ret = false;
        StringTokenizer st = new StringTokenizer(filter, ",;|&");
        while (st.hasMoreTokens()) {
            String txt = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(txt, "=><!");
            while (st2.hasMoreTokens()) {
                String key = st2.nextToken();
                if (st2.hasMoreTokens()) {
                    String value = st2.nextToken();
                    String sep = txt.substring(key.length(), txt.length() - value.length());
                    SemanticClass cls = obj.getSemanticClass();
                    if (cls.hasProperty(key)) {
                        String val = obj.getProperty(cls.getProperty(key));
                        if ((sep.equals("=") || sep.equals("==")) && value.equals(val)) {
                            ret = true;
                        }
                        if ((sep.equals("!=") || sep.equals("<>")) && !value.equals(val)) {
                            ret = true;
                        }
                    }
                }
            }
        }
        return ret;
    }
}
