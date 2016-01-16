package messages.message;

import java.nio.ByteBuffer;

public class Ack extends Message {
	
	public Ack(int timestamp) {
		lengthPrefix = 5;
		messageID = 5;
		
		// buffer pour le payload
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(0, timestamp);
		
		// On affecte le payload
		payload = buffer.array();	
	}

}
