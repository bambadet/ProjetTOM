package messages.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class NioEngine extends Engine {

    private List<ChannelImpl> connections;
    private Selector sel;
    private NioServer server;
    private int port;

    private int m_state;

    public NioEngine(int port) throws IOException {
        this.port = port;
        sel = SelectorProvider.provider().openSelector();
        connections = new LinkedList<>();
        //TODO: callback implementation
        listen(port, new AcceptCallback() {
            @Override
            public void accepted(Server server, Channel channel) {
                System.out.println("Accepted connection from: " + channel.getRemoteAddress());
            }

            @Override
            public void closed(Channel channel) {

            }
        });
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
            for (;;) {
                sel.select(delay);
                Iterator<?> selectedKeys = this.sel.selectedKeys().iterator();
                if (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();
                    if (!key.isValid()) {
                        continue;
                    } else if (key.isAcceptable()) {
                        System.out.println(port + "ACCEPTABLE");
                        AcceptCallback ac = (AcceptCallback) key.attachment();
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        SocketChannel sch;
                        try {
                            sch = ssc.accept();
                            if (sch != null) {
                                sch.configureBlocking(false);
                                sch.socket().setTcpNoDelay(true);

                                ChannelImpl ch = new ChannelImpl(this, sch);

                                connections.add(ch);

                                ac.accepted(server, ch);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (key.isReadable()) {
                        System.out.println(port + "READABLE");
                        ChannelImpl ch = (ChannelImpl) key.attachment();
                        ch.read();
                    } else if (key.isWritable()) {
                        System.out.println(port + "WRITABLE");
                        ChannelImpl ch = (ChannelImpl) key.attachment();
                        ch.write();
                    } else if (key.isConnectable()) {
                        System.out.println(port + "CONNECTABLE");
                        ConnectCallback cc = (ConnectCallback) key.attachment();
                        SocketChannel ch = (SocketChannel) key.channel();

                        ch.configureBlocking(false);
                        ch.finishConnect();

                        ChannelImpl channel = new ChannelImpl(this, ch);

                        connections.add(channel);

//						cc.connected(channel);
                    }
                }
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

    /**
     * Ask for this NioEngine to accept connections on the given port, calling
     * the given callback when a connection has been accepted.
     *
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
     * Ask this NioEngine to connect to the given port on the given host. The
     * callback will be notified when the connection will have succeeded.
     *
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
}
