package messages.message;

public class NewPeer extends Message {
	
	public NewPeer() {
		lengthPrefix = 1;
		messageID = 2;
		payload = null;
	}
	
}
