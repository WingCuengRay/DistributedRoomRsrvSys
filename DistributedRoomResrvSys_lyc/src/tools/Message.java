/**
 * 
 */
package tools;

import java.net.InetAddress;

/**
 * @author Raymand
 * @description The superclass for all types of message which should be sent with UDP
 *
 */
public abstract class Message {
	// The member variables indicate where the message is sent from
	protected InetAddress srcIP;  
	protected int srcPort;
	
	public Message(){
		
	}
	
	public Message(InetAddress ip, int port){
		srcIP = ip;
		srcPort = port;
	}
	
	
	public abstract String pack();
}
