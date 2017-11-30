package tools;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPConnection {
	private String targetIP;
	private int targetPort;
	private byte[] buf = new byte[512];
	
	public UDPConnection() {}
	
	/**
	 *  @name:   UDPConnection(String ip, int port)
	 *  @description: Constructor that can specify the ip and port of destination
	 *  @param:  ip - String, the ip address of destination host
	 *			 port - int, the port of destination host		
	 */
	public UDPConnection(String ip, int port)
	{
		targetIP = ip;
		targetPort = port;
	}
	
	/**
	 *  @name:   DatagramSocket Send(Message m)
	 *  @description: Send a message to a host decided by the targetIP and targetPort
	 *  @param:  m - Message, the message to be sent
	 *  @return: DatagramSocket, the socket that used to send the message. We return this value
	 *			 for receving reply.
	 */
	public DatagramSocket Send(Message m){
		try {
			InetAddress ipaddr = InetAddress.getByName(targetIP);
			String s = m.pack();
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket p = new DatagramPacket(s.getBytes(), s.length(), ipaddr, targetPort);
			socket.send(p);
			
			return socket;
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 *  @name:   String ReceiveString(DatagramSocket socket)
	 *  @description: Recevie a string message from udp. 
	 *				  The function is always used when we need to reply later on.
	 *  @param:  socket - DatagramSocket, the socket that we use to listen
	 *  @return: String, the content in the udp packet
	 */	
	public String ReceiveString(DatagramSocket socket) {
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		String message = null;
		try {
			socket.receive(packet);
			message = new String(packet.getData(), 0, packet.getLength());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return message;
	}
	
	/**
	 *  @name:   DatagramPacket ReceivePacket(DatagramSocket socket)
	 *  @description: Recevie the original udp packe
	 *  @param:  socket - DatagramSocket, the socket that we use to listen
	 *  @return: String, the content in the udp packet
	 */	
	public DatagramPacket ReceivePacket(DatagramSocket socket) {
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return packet;
	}
	
	public void setTargetIP(String ip) {
		targetIP = ip;
	}
	
	public void setTargetPort(int port) {
		targetPort = port;
	}
	
	
}
