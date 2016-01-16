package messages.message;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import messages.engine.ChannelImpl;

public class Connect extends Message {
	
	public Connect(List<ChannelImpl> connections) {
		
		lengthPrefix = 1 + connections.size()*8;
		messageID = 3;
		
		// buffer pour le payload
		ByteBuffer buffer = ByteBuffer.allocate(connections.size()*8);
		
		// On met dans le payload les couples port + add
		int i = 0;
		Iterator<ChannelImpl> it = connections.iterator();
		while (it.hasNext()) {
			
			// On recupere le port et l'adresse
			InetSocketAddress inetAdd = it.next().getRemoteAddress();
			
			// On inscrit le port dans le buffer
			buffer.putInt(i, inetAdd.getPort());
			
			// On recupere 4 octets qui forment l'addIP
			byte[] ipAddress = inetAdd.getAddress().getAddress();

			// On inscrit l'addIP dans le buffer
			byte[] ipAdd = inetAdd.getAddress().getAddress();
			// j < 4
			for (int j = 0; j < ipAdd.length; j++)
				buffer.put(4 + i + j, ipAdd[j]);
			
			i += 8;
			
		}
		
		// On affecte le payload
		payload = buffer.array();	 
	}
	
}
