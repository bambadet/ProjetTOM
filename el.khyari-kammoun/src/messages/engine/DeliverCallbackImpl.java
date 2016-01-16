package messages.engine;

public class DeliverCallbackImpl implements DeliverCallback {

	/**
	 * Callback to notify that a message has been received.
	 * The message is whole, all bytes have been accumulated.
	 *
	 * @param channel
	 * @param bytes
	 */
	@Override
	public void deliver(Channel channel, byte[] bytes) {
		System.out.println(new String(bytes));
	}
	
}
