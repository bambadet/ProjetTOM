package messages.engine;

import messages.message.Message;

public class ToDeliver implements Comparable<ToDeliver> {
	
	private Message message;
	private int acksReceived;
	private ChannelImpl channel;
	
	public ToDeliver(Message message, int acks, ChannelImpl channel) {
		this.message = message;
		acksReceived = acks;
		this.channel = channel;
	}
	
	public void incrementAcks() {
		acksReceived++;
	}
	
	public Message getMessage() {
		return message;
	}
	
	public int getAcksReceived() {
		return acksReceived;
	}
	
	public ChannelImpl getChannel() {
		return channel;
	}
	
	@Override
	public int compareTo(ToDeliver o) {
		if (this.message.getTimestamp() < o.getMessage().getTimestamp())
			return -1;
		else if (this.message.getTimestamp() > o.getMessage().getTimestamp())
			return 1;
		else
			return 0;
	}


}
