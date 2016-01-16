package messages.message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;



public class Message {

	protected int lengthPrefix;

	protected byte messageID;

	protected byte[] payload;

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

	public byte getMessageID(){
		return messageID ;
	}

	public byte[] getPayload(){
		return payload ;
	}

	public int getLengthPrefix(){
		return lengthPrefix ;
	}

}
