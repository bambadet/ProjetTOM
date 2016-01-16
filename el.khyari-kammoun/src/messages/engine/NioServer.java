package messages.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class NioServer extends Server {

	private NioEngine m_engine;
	private InetAddress m_localhost;
	private ServerSocketChannel m_sch;
	private int m_port;
	private AcceptCallback m_callback;

	private static final int DISCONNECTED = 0;
	private static final int ACCEPTING = 1;
	int m_state;

	public NioServer(NioEngine engine, int port, AcceptCallback callback) throws UnknownHostException {
		m_engine = engine;
		m_port = port;
		m_localhost = InetAddress.getLocalHost();
		m_callback = callback;
		m_state = DISCONNECTED;
	}

	public void accept() throws IOException {
		m_state = ACCEPTING;

		// create a new non-blocking server socket channel
		m_sch = ServerSocketChannel.open();
		m_sch.configureBlocking(false);

		// bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(m_localhost, m_port);
		m_sch.socket().bind(isa);

		m_engine.register(m_sch, SelectionKey.OP_ACCEPT, m_callback);
	}

	/**
	 * @return the port onto which connections are accepted.
	 */
	@Override
	public int getPort() {
		return m_port;
	}

	/**
	 * Close the server port, no longer accepting connections.
	 */
	@Override
	public void close() {
		try {
			m_sch.close();
		} catch (IOException e) {
			System.err.println("Exception while closing the server socket: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
