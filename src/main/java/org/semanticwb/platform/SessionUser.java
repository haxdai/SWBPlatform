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

import java.security.Principal;
import java.util.HashMap;

/**
 * Class to hold information about the current session user.
 * @author Jei
 */
//TODO: Move this class to SWBModel subproject because is used in SWBContext. After that move both classes to SWBPortal subproject
public class SessionUser {

    /**
     * The req.
     */
    private static long req = 0;

    /**
     * The usersMap.
     */
    private HashMap<String, Principal> usersMap = new HashMap<>();

    /**
     * Constructor. Creates a new {@link SessionUser}.
     * @param user   {@link Principal} object for the user.
     * @param usrrep User repository ID.
     */
    public SessionUser(Principal user, String usrrep) {
        usersMap.put(usrrep, user);
        //TODO: check this condition. It overrides previous assignment.
        if (usrrep != null) {
            usersMap.put(null, user);
        }
        req++;
    }

    /**
     * Gets the session user.
     *
     * @param usrrep the User repository ID.
     * @return the Session user {@link Principal}.
     */
    public Principal getUser(String usrrep) {
        return usersMap.get(usrrep);
    }

    /**
     * Sets the session User.
     *
     * @param user   {@link Principal} object for the user.
     * @param usrrep User repository ID.
     */
    public void setUser(Principal user, String usrrep) {
        usersMap.put(usrrep, user);
        if (usrrep != null) usersMap.put(null, user);
        req++;
    }

    /**
     * Get request id.
     * @return the long
     */
    public long geRequestID() {
        return req;
    }
}
