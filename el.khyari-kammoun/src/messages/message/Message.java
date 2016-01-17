package messages.message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;



public class Message {
    
    protected int lengthPrefix;
    
    protected byte messageID;
    
    protected byte[] payload = null;
    
    protected int timestamp = 0;
    
    protected byte[] message = null;
    
    public int send(SocketChannel channel) throws Exception{
        
        System.out.println("Sending :" + this.getClass().getName() + " message");
        
        if(this.getPayload() != null){
            
            ByteBuffer wrapped = ByteBuffer.allocate(5 + payload.length);
            System.out.println("LengthPrefix : " + lengthPrefix);
            wrapped.putInt(lengthPrefix);
            wrapped.put(messageID);
            wrapped.put(payload);
            wrapped.flip();
            int bytesWritten = channel.write(wrapped);
            return bytesWritten;
            
        } else {
            
            ByteBuffer wrapped = ByteBuffer.allocate(5);
            wrapped.putInt(lengthPrefix);
            wrapped.put(messageID);
            wrapped.flip();
            int bytesWritten = channel.write(wrapped);
            wrapped.clear();
            return bytesWritten;
        }
        
    }
    
    public ByteBuffer send() throws Exception{
        
        // System.out.println("Sending :" + this.getClass().getName() + " message");
        
        ByteBuffer wrapped = null;
        
        if(this.getPayload() != null){
            
            wrapped = ByteBuffer.allocate(5 + payload.length);
            //System.out.println("LengthPrefix : " + lengthPrefix);
            wrapped.putInt(lengthPrefix);
            wrapped.put(messageID);
            wrapped.put(payload);
            
        } else {
            
            wrapped = ByteBuffer.allocate(5);
            wrapped.putInt(lengthPrefix);
            wrapped.put(messageID);
            
        }
        
        return wrapped;
        
    }
    
    public byte getMessageID(){
        return messageID ;
    }
    
    public byte[] getPayload(){
        return payload ;
    }
    
    public int getLengthPrefix(){
        return lengthPrefix ;
    }
    
    public int getTimestamp(){
        return timestamp ;
    }
    
    public byte[] getMessage() {
        return message;
    }
    
    public boolean isNewPeer() {
        return false;
    }
    
    public boolean isChatMessage() {
        return false;
    }
    
}
