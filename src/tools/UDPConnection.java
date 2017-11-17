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
	
	public UDPConnection(String ip, int port)
	{
		targetIP = ip;
		targetPort = port;
	}
	
	public DatagramSocket Send(Message m){
		try {
			InetAddress ipaddr = InetAddress.getByName(targetIP);
			String s = m.pack();
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket p = new DatagramPacket(s.getBytes(), s.length(), ipaddr, targetPort);
			
			return socket;
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
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
