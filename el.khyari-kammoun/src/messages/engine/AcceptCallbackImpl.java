package messages.engine;

public class AcceptCallbackImpl implements AcceptCallback {

	/**
	 * Callback to notify about an accepted connection.
	 * @param server
	 * @param channel
	 */
	@Override
	public void accepted(Server server, Channel channel) {
		System.out.println("Accepted connection from: " + channel.getRemoteAddress());		
	}

	/**
	 * Callback to notify that a previously accepted channel 
	 * has been closed.
	 * @param channel
	 */
	@Override
	public void closed(Channel channel) {
		System.out.println("Connection from " + channel.getRemoteAddress() + " closed");
	}

}
