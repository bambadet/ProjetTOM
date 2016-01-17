package messages.message;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChatMessage extends Message {
    
    public ChatMessage(int timestamp, byte[] message) {
        
        lengthPrefix = 1 + 4 + 16 + message.length;
        messageID = 4;
        this.timestamp = timestamp;
        this.message = message;
        
        // On determine le checksum
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] checksum = md.digest(message);
        
        // buffer pour le payload
        ByteBuffer buffer = ByteBuffer.allocate(4 + 16 + message.length);
        
        int j = 0;
        
        // timestamp
        buffer.putInt(j, timestamp);
        
        j += 4;
        
        // checksum (i < 16)
        for (int i = 0; i < checksum.length; i++)
            buffer.put(j + i, checksum[i]);
        
        j += 16;
        
        // message
        for (int i = 0; i < message.length; i++)
            buffer.put(j + i, message[i]);
        
        // On affecte le payload
        payload = buffer.array();	
        
    }
    
    @Override
    public boolean isChatMessage() {
        return true;
    }
    
}
