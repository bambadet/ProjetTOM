package messages.message;

import java.nio.ByteBuffer;

public class NewPeer extends Message {
    
    public NewPeer(int timestamp) {
        lengthPrefix = 1 + 4;
        messageID = 2;
        this.timestamp = timestamp;
        
        // buffer pour le payload
        ByteBuffer buffer = ByteBuffer.allocate(4);
        // timestamp
        buffer.putInt(0, timestamp);
        payload = buffer.array();
    }
    
    @Override
    public boolean isNewPeer() {
        return true;
    }
    
}
