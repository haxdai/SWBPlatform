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

import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.base.SWBObserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class responsible for the reception and shipment of UDP messages for servers synchronization.
 *
 * @author Javier Solis Gonzalez
 */
public class SWBMessageCenter {

    /**
     * The LOG.
     */
    public static final Logger LOG = SWBUtils.getLogger(SWBMessageCenter.class);

    /**
     * The observers.
     */
    private HashMap<String, SWBObserver> observers = new HashMap<>();

    /**
     * The standalone.
     */
    private boolean standalone = true; //standalone

    /**
     * The server.
     */
    private SWBMessageServer server = null;

    /**
     * The sock.
     */
    private DatagramSocket sock = null;

    /**
     * The packets.
     */
    private List<DatagramPacket> packets = new ArrayList<>();

    /**
     * The addr.
     */
    private InetAddress addr = null;

    /**
     * The messages.
     */
    private LinkedList<String> messages;

    /**
     * The dateFormat.
     */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * The localhost.
     */
    private String localhost = "127.0.0.1";

    /**
     * The Timer Sync
     **/
    private Timer timer = new Timer("MessageSynchronizer", true);

    /**
     * The Message
     **/
    private String syncMessage = null;


    /**
     * Creates a new instance of {@link SWBMessageCenter}.
     */
    public SWBMessageCenter() {
        LOG.event("Initializing SWBMessageCenter...");
        messages = new LinkedList<>();
    }

    /**
     * Destroys {@link SWBMessageCenter} closing socket and stopping server.
     */
    public void destroy() {
        LOG.event("Destroy SWBMessageCenter...");
        if (sock != null) {
            sock.close();
        }

        if (server != null) {
            server.stopServer();
        }
    }

    /**
     * Initialize the {@link SWBMessageCenter}.
     */
    public void init() {
        String clientServerConf = SWBPlatform.getEnv("swb/clientServer");
        if (!clientServerConf.equalsIgnoreCase("SASC")) {
            standalone = false;
        }

        try {
            if (!standalone) {
                String localAddr = SWBPlatform.getEnv("swb/localMessageAddress");
                String serverAddr = SWBPlatform.getEnv("swb/serverMessageAddress");

                if (localhost != null && serverAddr != null) {
                    //Nueva versión de registro de mensajes
                    int i = localAddr.lastIndexOf(':'); //MAPS74 Ajuste para IPV6
                    String ipaddr = localAddr.substring(0, i);

                    int port = Integer.parseInt(localAddr.substring(i + 1));
                    i = serverAddr.lastIndexOf(':'); //MAPS74 Ajuste para IPV6

                    String sipaddr = serverAddr.substring(0, i);
                    int sport = Integer.parseInt(serverAddr.substring(i + 1));

                    InetAddress saddr = null;
                    try {
                        if (ipaddr.equalsIgnoreCase("localhost")) {
                            addr = InetAddress.getLocalHost();
                        } else {
                            addr = InetAddress.getByName(ipaddr);
                        }

                        if (sipaddr.equalsIgnoreCase("localhost")) {
                            saddr = InetAddress.getLocalHost();
                        } else {
                            saddr = InetAddress.getByName(sipaddr);
                        }
                    } catch (Exception e) {
                        LOG.error("SWBMessage Server IP Error:", e);
                    }

                    addAddress(addr, port);
                    addAddress(saddr, sport);

                    String message = "ini|hel|" + addr.getHostAddress() + ":" + port;

                    syncMessage = "syn|hel|" + addr.getHostAddress() + ":" + port;

                    server = new SWBMessageServer(this, addr, port);
                    server.start();

                    long period = 1000 * 60 * 5;

                    sendMessage(message);

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            sendMessage(syncMessage);
                        }

                    }, period, period);
                } else {
                    //Version anterior de Registro de Mensajes
                    String message = "ini|MessageServer Iniciado...";
                    byte[] data = message.getBytes();

                    server = new SWBMessageServer(this);
                    server.start();

                    try {
                        String ipaddr = SWBPlatform.getEnv("swb/MessageIPAddr");
                        if (ipaddr.equalsIgnoreCase("localhost")) {
                            addr = InetAddress.getLocalHost();
                        } else {
                            addr = InetAddress.getByName(ipaddr);
                        }
                    } catch (Exception e) {
                        LOG.error("SWBMessage Server IP Error:", e);
                    }

                    //get send address
                    int port = Integer.parseInt(SWBPlatform.getEnv("swb/sendMessagePort"));
                    String sendAddr = SWBPlatform.getEnv("swb/sendMessageIPAddrs");

                    if (sendAddr == null) {
                        String ip = addr.getHostAddress();
                        InetAddress saddr = InetAddress.getByName(ip.substring(0, ip.lastIndexOf('.')) + ".255");
                        LOG.info("BroadCast Addr:" + saddr + ":" + port);
                        packets.add(new DatagramPacket(data, data.length, saddr, port));
                    } else {
                        DatagramPacket packet = null;
                        int aport;
                        boolean fp = false;
                        StringTokenizer st = new StringTokenizer(sendAddr, ":,;", true);
                        while (st.hasMoreTokens()) {
                            String aux = st.nextToken();
                            try {
                                if (aux.equals(":")) {
                                    fp = true;
                                } else if (aux.equals(",") || aux.equals(";")) {
                                    fp = false;
                                } else if (aux.trim().length() > 0) {
                                    if (fp) {
                                        aport = Integer.parseInt(aux.trim());
                                        packet.setPort(aport);
                                    } else {
                                        InetAddress saddr = InetAddress.getByName(aux.trim());
                                        packet = new DatagramPacket(data, data.length, saddr, port);
                                        packets.add(packet);
                                    }
                                }
                            } catch (Exception e) {
                                LOG.error(e);
                            }
                        }

                        for (DatagramPacket apacket : packets) {
                            LOG.info("Send Address " + apacket.getAddress() + ":" + apacket.getPort());
                        }
                    }
                    sendMessage(message);
                }
            }
        } catch (Exception e) {
            LOG.error("SWBMessageCenter Init Error...", e);
        }
    }


    /**
     * Refresh.
     */
    public void refresh() {
    }

    /**
     * Sends a message.
     *
     * @param message the message
     */
    public void sendMessage(String message) {
        if (!standalone && !packets.isEmpty()) {
            //Try to get socket connection
            if (null == sock) {
                try {
                    if (addr != null) {
                        DatagramSocket aux = new DatagramSocket();   //obtener un puerto de salida valido...
                        int x = aux.getLocalPort();
                        aux.close();
                        sock = new DatagramSocket(x, addr);
                    } else {
                        sock = new DatagramSocket();
                    }
                } catch (SocketException sex) {
                    LOG.error("SWBMessageCenter Socket Error:" + message, sex);
                }
            }

            //Send packets
            if (null != sock) {
                try {
                    byte[] data = message.getBytes();
                    for (DatagramPacket refPacket : packets) {
                        DatagramPacket packet = new DatagramPacket(data, data.length, refPacket.getAddress(), refPacket.getPort());
                        sock.send(packet);
                    }
                } catch (IOException ioex) {
                    LOG.error("SWBMessageCenter SendMessage Error:" + message, ioex);
                }
            }
        } else {
            incomingMessage(message, localhost);
        }
    }

    /**
     * Prepare incoming message for queuing.
     *
     * @param message the message
     * @param addr    the address
     */
    public void incomingMessage(String message, String addr) {
        StringBuilder logbuf = new StringBuilder(message.length() + 20);
        logbuf.append(message.substring(0, 4));
        logbuf.append(dateFormat.format(new Date()));
        logbuf.append(message.substring(3));
        pushMessage(logbuf.toString());
        LOG.debug("Message from " + addr + ":(" + message + ")");
    }

    /**
     * Adds a message to the queue.
     *
     * @param message the message
     */
    public void pushMessage(String message) {
        synchronized (messages) {
            messages.addFirst(message);
        }
    }

    /**
     * Pops a message from the queue.
     *
     * @return message string or empty string if pop fails.
     */
    public String popMessage() {
        try {
            synchronized (messages) {
                return messages.removeLast();
            }
        } catch (Exception e) {
            synchronized (messages) {
                messages.clear();
            }
            LOG.error("SWBMessageCenter Pop Message Error...", e);
        }
        return "";
    }

    /**
     * Register a {@link SWBObserver} object for message notifications.
     *
     * @param key observer name.
     * @param obs the {@link SWBObserver} object
     */
    public synchronized void registerObserver(String key, SWBObserver obs) {
        observers.put(key, obs);
    }

    /**
     * Removes a {@link SWBObserver} object.
     *
     * @param key the observer name.
     */
    public synchronized void removeObserver(String key) {
        observers.remove(key);
    }

    /**
     * Gets an iterator to the observers.
     *
     * @return the observers
     */
    public Iterator getObservers() {
        return new ArrayList<>(observers.values()).iterator();
    }

    /**
     * Gets an observer.
     *
     * @param key the observer name.
     * @return the {@link SWBObserver}
     */
    public SWBObserver getObserver(String key) {
        return observers.get(key);
    }

    /**
     * Checks for messages in queue.
     *
     * @return true if there are queued messages.
     */
    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    /**
     * Gets message queue size.
     *
     * @return the queue size.
     */
    public int messageSize() {
        return messages.size();
    }

    /**
     * Gets the socket address.
     *
     * @return socket address.
     */
    public String getAddress() {
        if (sock != null) {
            return sock.getLocalAddress().getHostAddress();
        } else {
            return localhost;
        }
    }

    /**
     * Gets the {@link SWBMessageServer}.
     *
     * @return the message server.
     */
    public SWBMessageServer getMessageServer() {
        return server;
    }

    /**
     * Adds an address to the address list for sending messages.
     *
     * @param addr {@link InetAddress} object.
     * @param port Connection port.
     * @return true if address is added, false if address already exists in a DatagramPacket.
     */
    public synchronized boolean addAddress(InetAddress addr, int port) {
        boolean addressExists = false;
        for (DatagramPacket datagramPacket : packets) {
            if (datagramPacket.getAddress().equals(addr) && datagramPacket.getPort() == port) {
                addressExists = true;
                break;
            }
        }

        if (!addressExists) {
            byte[] data = "".getBytes();
            packets.add(new DatagramPacket(data, data.length, addr, port));
        }

        return !addressExists;
    }

    /**
     * @return
     * @deprecated for naming conventions. Use {@link #getAddressList()}
     */
    @Deprecated
    public String getListAddress() {
        return getAddressList();
    }

    /**
     * Gets a pipe separated String with all addresses for sending messages.
     *
     * @return pipe separated String with all addresses for sending messages
     */
    public String getAddressList() {
        StringBuilder ret = new StringBuilder();
        Iterator<DatagramPacket> it = packets.iterator();

        while (it.hasNext()) {
            DatagramPacket datagramPacket = it.next();
            ret.append(datagramPacket.getAddress().getHostAddress())
                    .append(":").append(datagramPacket.getPort());

            if (it.hasNext()) {
                ret.append("|");
            }
        }
        return ret.toString();
    }
}