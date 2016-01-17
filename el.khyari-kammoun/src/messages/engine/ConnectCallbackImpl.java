package messages.engine;

public class ConnectCallbackImpl implements ConnectCallback {
    
    /**
     * Callback to notify that a previously connected channel has been closed.
     *
     * @param channel
     */
    @Override
    public void closed(Channel channel) {
        System.out.println("Connection from " + channel.getRemoteAddress() + " closed");
    }
    
    /**
     * Callback to notify that a connection has succeeded.
     *
     * @param channel
     */
    @Override
    public void connected(Channel channel) {
        System.out.println("Connected to : " + channel.getRemoteAddress());
    }
}
