package messages.handle;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import messages.engine.ChannelImpl;
import messages.engine.ConnectCallbackImpl;
import messages.engine.Engine;
import messages.engine.NioEngine;
import messages.engine.ToDeliver;
import messages.message.Ack;
import messages.message.ChatMessage;
import messages.message.Connect;
import messages.message.Message;
import messages.message.NewPeer;

public class MessageListener {
	
	public static final byte messageID_MAX = 5 ;
	public static final byte messageID_MIN = 1 ;
	public static final int message_MINLENGTH = 1 ;
	
	private NioEngine engine;
	
	public MessageListener(NioEngine engine) {
		this.engine = engine;
	}

	public void handleMessage(ByteBuffer wrapped, ChannelImpl channel) {

		System.out.println("Handling Message");

		int lengthPrefix = -1;
		try {
			lengthPrefix = wrapped.getInt();
			System.out.println(" Length : " + lengthPrefix);
			if(lengthPrefix < message_MINLENGTH){
				System.out.println("Error : Invalid length message for Decoding.");
				engine.removeConnection(channel);
				channel.close();
				return ;
			}
		} catch (BufferUnderflowException b) {
			System.out.println("Exception catchée par le buffer");
			return;
		}

		Message message = null;
		Message deliver = null;
		ToDeliver toDeliver = null;
		
		byte messageID = (byte) wrapped.get();
		if(messageID < messageID_MIN || messageID > messageID_MAX ){
			System.out.println("Error : Invalid message ID.");
			engine.removeConnection(channel);
			channel.close();
			return ;
		}

		System.out.println("iD Message : " + messageID);

		try {

			switch (messageID) {
			case 1:
				// hello
				// on previent les autres que quelqu'un arrive
				engine.setTimestamp(engine.getTimestamp()+1);
				message = new NewPeer(engine.getTimestamp());
				engine.sendBroadcast(message);
				// on envoie la liste des peers au nouvel arrivant
				message = new Connect(engine.getConnections());
				engine.send(message, channel);
				// on evoie les messages de notre file au nouvel arrivant
				Iterator<ToDeliver> it = engine.getToDeliver().iterator();
				while (it.hasNext()) {
				    ToDeliver del = it.next();
				    engine.send(del.getMessage(), channel);
				}
				break;
				
			case 2:
				// newpeer
				int timestampNP = wrapped.getInt();
				// on enregistre le message dans notre queue a delivrer
				deliver = new NewPeer(timestampNP);
				toDeliver = new ToDeliver(deliver, 0, channel);
				engine.getToDeliver().add(toDeliver);
				
				// On modifie le timestamp
				if (engine.getTimestamp() < timestampNP)
					engine.setTimestamp(timestampNP);
				engine.setTimestamp(engine.getTimestamp()+1);
				
				// on envoie un ack
				message = new Ack(timestampNP);
				engine.sendBroadcast(message);
				break;

			case 3:
				// connect
				// On recupere la liste des couples port + adresse
				List<InetSocketAddress> remotePeers = new LinkedList<InetSocketAddress>();
				int port;
				byte[] address = new byte[4];
				for (int i = 0; i < (lengthPrefix-1)/8; i++) {
					port = wrapped.getInt();
					wrapped.get(address, 0, address.length);
					InetAddress add = InetAddress.getByAddress(address);
					InetSocketAddress couple = new InetSocketAddress(add, port);
					remotePeers.add(couple);
				}
				// on se connecte aux autres
				for (InetSocketAddress couple : remotePeers) {
					engine.connect(couple.getAddress(), couple.getPort(), new ConnectCallbackImpl());
				}
				break;

			case 4:
				// chatmessage
				// timestamp
				int timestampCM = wrapped.getInt();
				// checksum
				byte[] checksum = new byte[16];
				wrapped.get(checksum, 0, checksum.length);
				// message
				byte[] chatMessage = new byte[lengthPrefix-4-16];
				wrapped.get(chatMessage, 0, chatMessage.length);
				
				// on verifie le checksum
				MessageDigest md = null;
				try {
					md = MessageDigest.getInstance("MD5");
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				byte[] test = md.digest(chatMessage);
				if (!Arrays.equals(test, chatMessage))
					System.err.println("Le hash du message n'est pas bon !");
				
				// on enregistre le message dans notre queue a delivrer
				deliver = new ChatMessage(timestampCM, chatMessage);
				toDeliver = new ToDeliver(deliver, 0, channel);
				engine.getToDeliver().add(toDeliver);
				
				// On modifie le timestamp
				if (engine.getTimestamp() < timestampCM)
					engine.setTimestamp(timestampCM);
				engine.setTimestamp(engine.getTimestamp()+1);
				
				// on envoie un ack
				message = new Ack(timestampCM);
				engine.sendBroadcast(message);
				
				break;

			case 5:
				// ack
				int timestampA = wrapped.getInt();
				// on actualise le nombre de acks recus
				Iterator<ToDeliver> iter = engine.getToDeliver().iterator();
				while (iter.hasNext()) {
				    ToDeliver del = iter.next();
				    if (del.getMessage().getTimestamp() == timestampA) {
				    	del.incrementAcks();
				    	break;
				    }
				}
				break;

			}

		} catch (BufferUnderflowException b) {
			System.out.println("Fin de la fonction : exception");
			return;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (wrapped.hasRemaining())
			handleMessage(wrapped, channel);
		return;

	}

}
