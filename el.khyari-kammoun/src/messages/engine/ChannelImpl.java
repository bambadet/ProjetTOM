package messages.engine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import messages.handle.MessageListener;
import messages.message.Message;

public class ChannelImpl extends Channel {
    
    // En gros la classe qui correspond à une connexion avec un autre peer
    
    public final static int BUFFER_SIZE = 2048;
    
    private NioEngine m_engine;
    private DeliverCallback deliver;
    private InetSocketAddress m_isa;
    //private ByteBuffer m_outBuffer;
    private SocketChannel sch;
    private SelectionKey m_key;
    private ArrayList<ByteBuffer> m_outBuffers;
    
    public ChannelImpl(NioEngine engine, SocketChannel sch) throws IOException {
        m_engine = engine;
        m_isa = (InetSocketAddress) sch.getRemoteAddress();
        this.sch = sch;
        m_key = m_engine.register(sch, SelectionKey.OP_READ, this);
        m_outBuffers = new ArrayList<ByteBuffer>();
    }
    
    /**
     * Set the callback to deliver messages to.
     * @param callback
     */
    @Override
    public void setDeliverCallback(DeliverCallback callback) {
        deliver = callback;
    }
    
    /**
     * Get the Inet socket address for the other side of this channel.
     * @return
     */
    @Override
    public InetSocketAddress getRemoteAddress() {
        return m_isa;
    }
    
    /**
     * Sending the given byte array, a copy is made into internal buffers,
     * so the array can be reused after sending it.
     * @param bytes
     * @param offset
     * @param length
     */
    @Override
    public void send(byte[] bytes, int offset, int length) {
        ByteBuffer outBuffer = ByteBuffer.wrap(bytes, offset, length);
        m_outBuffers.add(outBuffer);
        //m_outBuffer = ByteBuffer.wrap(bytes, offset, length);
        
        m_key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }
    
    public void write() throws IOException {
        Iterator<ByteBuffer> it = m_outBuffers.iterator();
        while (it.hasNext()) {
            sch.write(it.next());
            it.remove();
        }
        //sch.write(m_outBuffer);
        
        m_key.interestOps(SelectionKey.OP_READ);
    }
    
    public void read(MessageListener handler) throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        int read = sch.read(readBuffer);
        
        if (read > 0) {
            readBuffer.flip();
            handler.handleMessage(readBuffer, this);
        } else if (read == -1) {
            m_engine.removeConnection(this);
            this.close();
            ConnectCallback callback = new ConnectCallbackImpl();
            callback.closed(this);
        }
        
    }
    
    public SocketChannel getSch() {
        return sch;
    }
    
    public SelectionKey getSelectionKey() {
        return m_key;
    }
    
    public DeliverCallback getDeliver() {
        return deliver;
    }
    
    @Override
    public void close() {
        try {
            sch.close();
            m_key.cancel();
        } catch (IOException e) {
            System.err.println("Exception while closing the socket: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
