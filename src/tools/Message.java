/**
 * 
 */
package tools;

import java.net.InetAddress;

/**
 * @author ray
 *
 */
public abstract class Message {
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
