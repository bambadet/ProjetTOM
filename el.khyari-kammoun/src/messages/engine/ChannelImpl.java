package messages.engine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ChannelImpl extends Channel {

	// En gros la classe qui correspond à une connexion avec un autre peer

	public final static int BUFFER_SIZE = 2048;

	private NioEngine m_engine;
	private DeliverCallback deliver;
	private InetSocketAddress m_isa;
	private ByteBuffer buffer;
	private ByteBuffer m_outBuffer;
	private SocketChannel sch;
	private SelectionKey m_key;

	public ChannelImpl(NioEngine engine, SocketChannel sch) throws IOException {
		m_engine = engine;
		m_isa = (InetSocketAddress) sch.getRemoteAddress();
		buffer = ByteBuffer.allocate(BUFFER_SIZE);
		this.sch = sch;

		m_key = m_engine.register(sch, SelectionKey.OP_READ, this);
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

	public void setSending() {

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
		m_outBuffer = ByteBuffer.wrap(bytes, offset, length);

		m_key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}

	public void write() throws IOException {
		sch.write(m_outBuffer);

		m_key.interestOps(SelectionKey.OP_READ);
	}

	public void read() throws IOException {
		ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		int read = sch.read(readBuffer);

		//TODO: message not complete?

		byte[] bytes = new byte[read];
		readBuffer.flip();
		readBuffer.get(bytes, 0, read);

		//int length = Util.readInt32(bytes, 0);

		System.out.println("READ: "+read+new String(bytes));

//		deliver.deliver(this, bytes);
	}

	@Override
	public void close() {
		try {
			sch.close();
		} catch (IOException e) {
			System.err.println("Exception while closing the socket: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
