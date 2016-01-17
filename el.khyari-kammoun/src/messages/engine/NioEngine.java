package messages.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import messages.handle.MessageListener;
import messages.message.Ack;
import messages.message.Message;

public class NioEngine extends Engine {
    
    private List<ChannelImpl> connections;
    private Selector sel;
    private NioServer server;
    private int port;
    private int timestamp;
    private PriorityQueue<ToDeliver> toDeliver;
    private MessageListener handler;
    private boolean blocked = false;
    
    
    public NioEngine(int port) throws IOException {
        this.port = port;
        sel = SelectorProvider.provider().openSelector();
        connections = new LinkedList<>();
        timestamp = 0;
        toDeliver = new PriorityQueue<>();
        listen(port, new AcceptCallbackImpl());
        handler = new MessageListener(this);
    }
    
    /**
     * NIO engine mainloop Wait for selected events on registered channels
     * Selected events for a given channel may be ACCEPT, CONNECT, READ, WRITE
     * Selected events for a given channel may change over time
     */
    @Override
    public void mainloop() {
        long delay = 50;
        try {
            for (; ; ) {
                sel.select(delay);
                Iterator<?> selectedKeys = this.sel.selectedKeys().iterator();
                if (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    if (!key.isValid()) {
                        continue;
                    } else {
                        if (key.isAcceptable()) {
                            selectedKeys.remove();
                            System.out.println(port+"ACCEPTABLE");
                            AcceptCallback ac = (AcceptCallback) key.attachment();
                            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                            SocketChannel sch;
                            try {
                                sch = ssc.accept();
                                if (sch != null) {
                                    sch.configureBlocking(false);
                                    sch.socket().setTcpNoDelay(true);
                                    
                                    ChannelImpl ch = new ChannelImpl(this, sch);
                                    ch.setDeliverCallback(new DeliverCallbackImpl());
                                    connections.add(ch);
                                    
                                    ac.accepted(server, ch);
                                    blocked = false;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (blocked) {
                            break;
                        } else if (key.isReadable()) {
                            selectedKeys.remove();
                            System.out.println(port+"READABLE");
                            ChannelImpl ch = (ChannelImpl) key.attachment();
                            ch.read(handler);
                        } else if (key.isWritable()) {
                            selectedKeys.remove();
                            System.out.println(port+"WRITABLE");
                            ChannelImpl ch = (ChannelImpl) key.attachment();
                            ch.write();
                        } else if (key.isConnectable()) {
                            selectedKeys.remove();
                            System.out.println(port+"CONNECTABLE");
                            ConnectCallback cc = (ConnectCallback) key.attachment();
                            SocketChannel ch = (SocketChannel) key.channel();
                            
                            ch.configureBlocking(false);
                            ch.finishConnect();
                            
                            ChannelImpl channel = new ChannelImpl(this, ch);
                            channel.setDeliverCallback(new DeliverCallbackImpl());
                            
                            connections.add(channel);
                            
                            cc.connected(channel);
                        }
                    }
                }
                // on delivre tous les messages prets
                int val = checkDeliver();
                while (val == 0)
                    val = checkDeliver();
            }
        } catch (IOException ex) {
            System.err.println("NioEngine got an exception: " + ex.getMessage());
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
    }
    
    public SelectionKey register(SelectableChannel ch, int interests, Object attachment) throws ClosedChannelException {
        SelectionKey key = ch.register(sel, interests);
        key.attach(attachment);
        return key;
    }
    
    public void sendBroadcast(String message) {
        byte[] bytes = message.getBytes();
        for (ChannelImpl c : connections) {
            c.send(bytes, 0, bytes.length);
        }
    }
    
    public void send(Message message, ChannelImpl c) {
        ByteBuffer buffer = null;
        try {
            buffer = message.send();
            c.send(buffer.array(), 0, buffer.array().length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void sendBroadcast(Message message) {
        ByteBuffer buffer = null;
        try {
            buffer = message.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (ChannelImpl c : connections) {
            c.send(buffer.array(), 0, buffer.array().length);
        }
        // si chatmessage ou newpeer
        if (message.isChatMessage() || message.isNewPeer()) {
            // on enregistre le message dans notre queue a delivrer
            // null car channel c'est nous meme, 1 pour representer notre ack a nous meme
            ToDeliver del = new ToDeliver(message, 1, null);
            toDeliver.add(del);
            // on envoie un ack
            Message mess = new Ack(timestamp);
            sendBroadcast(mess);
        }
    }
    
    /**
     * Ask for this NioEngine to accept connections on the given port,
     * calling the given callback when a connection has been accepted.
     * @param port
     * @param callback
     * @return an NioServer wrapping the server port accepting connections.
     * @throws IOException if the port is already used or can't be bound.
     */
    @Override
    public Server listen(int port, AcceptCallback callback) throws IOException {
        server = new NioServer(this, port, callback);
        server.accept();
        
        return server;
    }
    
    /**
     * Ask this NioEngine to connect to the given port on the given host.
     * The callback will be notified when the connection will have succeeded.
     * @param hostAddress
     * @param port
     * @param callback
     */
    @Override
    public void connect(InetAddress hostAddress, int port, ConnectCallback callback) throws UnknownHostException, SecurityException, IOException {
        // create a non-blocking socket channel
        SocketChannel ch = SocketChannel.open();
        ch.configureBlocking(false);
        
        // be notified when the connection to the server will be accepted
        register(ch, SelectionKey.OP_CONNECT, callback);
        
        // request to connect to the server
        ch.connect(new InetSocketAddress(hostAddress, port));
    }
    
    public void removeConnection(ChannelImpl channel) {
        connections.remove(channel);
        channel.close();
    }
    
    public int checkDeliver() {
        ToDeliver deliver = toDeliver.peek();
        if (deliver == null)
            return -1;
        if (deliver.getAcksReceived() == connections.size() + 1) {
            if (deliver.getMessage().isNewPeer()) {
                toDeliver.poll();
                // on stop toute activite jusqu'à l'arrivee du nouveau peer
                blocked = true;
                return 0;
            } else {
                toDeliver.poll();
                ChannelImpl ch = deliver.getChannel();
                ch.getDeliver().deliver(ch, deliver.getMessage().getMessage());
                return 0;
            }
        }
        return -1;
        
    }
    
    public List<ChannelImpl> getConnections() {
        return connections;
    }
    
    public int getTimestamp() {
        return timestamp;
    }
    
    public PriorityQueue<ToDeliver> getToDeliver() {
        return toDeliver;
    }
    
    public void setTimestamp(int val) {
        timestamp = val;
    }
}
