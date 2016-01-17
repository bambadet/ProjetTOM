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
        if (channel == null)
            System.out.println("Me :" + "\n" + new String(bytes));
        else
            System.out.println("Port : " + channel.getRemoteAddress().getPort() + "\n" + new String(bytes));
    }
    
}
