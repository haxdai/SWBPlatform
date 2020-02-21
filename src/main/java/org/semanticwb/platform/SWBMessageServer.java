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

import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Message Server implementation for UDP hit synchronization.
 *
 * @author Javier Solis Gonzalez
 * @version 1.0
 */
public class SWBMessageServer extends java.lang.Thread {

    /**
     * The Constant PACKET_SIZE.
     */
    private static final int PACKET_SIZE = 65535;
    /**
     * The LOG.
     */
    public static final Logger LOG = SWBUtils.getLogger(SWBMessageServer.class);
    /**
     * The socket.
     */
    private DatagramSocket socket = null;

    /**
     * Flag to stop running server.
     */
    private boolean stop = false;

    /**
     * The {@link SWBMessageCenter}.
     */
    private SWBMessageCenter center;

    /**
     * Constructor. Creates a new {@link SWBMessageServer}.
     * @param center    the {@link SWBMessageCenter}.
     * @param addr      Message Server address
     * @param port      Message Server port.
     * @throws SocketException if no socket connection could be established.
     */
    public SWBMessageServer(SWBMessageCenter center, InetAddress addr, int port) throws java.net.SocketException {
        this.center = center;
        getSocket(addr, port);
        LOG.event("Message Server at:\t" + socket.getLocalAddress().getHostAddress() + ":" + port);
    }

    /**
     * Constructor. Creates a new {@link SWBMessageServer} with default port and address.
     * @param center the center
     * @throws SocketException the socket exception
     */
    public SWBMessageServer(SWBMessageCenter center) throws java.net.SocketException {
        this.center = center;
        InetAddress addr = null;
        int port = 1500;

        try {
            port = Integer.parseInt(SWBPlatform.getEnv("swb/reciveMessagePort"));
            String ipaddr = SWBPlatform.getEnv("swb/MessageIPAddr");
            if (!ipaddr.equalsIgnoreCase("localhost")) {
                addr = InetAddress.getByName(ipaddr);
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        getSocket(addr, port);
        LOG.event("Message Server at:\t" + socket.getLocalAddress().getHostAddress() + ":" + port);
    }

    /**
     * Creates a new socket connection.
     * @param address {@link InetAddress}
     * @param port port.
     * @throws SocketException if no connection can be established.
     */
    private void getSocket(InetAddress address, int port) throws SocketException {
        if (null != address) {
            socket = new DatagramSocket(port, address);
        } else {
            socket = new DatagramSocket(port);
        }
    }

    public void run() {
        LOG.info("Message Server Running...");
        DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

        while (!stop) {
            try {
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                LOG.debug("UDP Msg: " + msg + " ");
                center.incomingMessage(msg, packet.getAddress().getHostAddress());
            } catch (Exception e) {
                LOG.error(e);
            }
        }
    }

    /**
     * Sets this thread to be stopped on next loop.
     */
    public void stopServer() {
        this.stop = true;
    }
}
