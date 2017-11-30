package Client;

import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import RoomResrvSys.RemoteServerInterface;
import tools.LogWriter;

public class Client {
	public enum Identity{
		DVLA, KKLA, WSTA, 
		DVLS, KKLS, WSTS, 
		None
	};
	
	protected String user_id;
	protected Identity identity;
	protected LogWriter writer;
	protected String campus;
	protected String ipAddr;
	protected Integer port;
	protected final static HashMap<Identity, String> serverIPMap;
	protected final static HashMap<Identity, Integer> serverPortMap;
	protected static int requestID = 3000;
	protected static HashMap<String, Integer> seq_num;
	protected static DatagramSocket FESocket;
	
	// Initialize the static variable
	static {
		serverIPMap = new HashMap<Identity, String>();
		serverIPMap.put(Identity.DVLA, "127.0.0.1");
		serverIPMap.put(Identity.KKLA, "127.0.0.1");
		serverIPMap.put(Identity.WSTA, "127.0.0.1");
		serverIPMap.put(Identity.DVLS, "127.0.0.1");
		serverIPMap.put(Identity.KKLS, "127.0.0.1");
		serverIPMap.put(Identity.WSTS, "127.0.0.1");
		
		serverPortMap = new HashMap<Identity, Integer>();
		serverPortMap.put(Identity.DVLA, 13320);
		serverPortMap.put(Identity.KKLA, 13321);
		serverPortMap.put(Identity.WSTA, 13322);
		serverPortMap.put(Identity.DVLS, 13320);
		serverPortMap.put(Identity.KKLS, 13321);
		serverPortMap.put(Identity.WSTS, 13322);
		
		seq_num = new HashMap<String, Integer>();
		seq_num.put("DVL", 1);
		seq_num.put("KKL", 1);
		seq_num.put("WST", 1);
		
		try {
			FESocket = new DatagramSocket(13360);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public Client() {
		identity = Identity.None;
	}
	
	public boolean Login(String username, String passwd) {
		// Regex (?<=...) represents positive lookbehind
		String[] part = username.split("(?<=\\D)(?=\\d)");
		if(part.length != 2 || part[1].length()!=4)		// Check the formation of login id
			return false;
		
		if(part[0].equals("DVLS")){
			campus = new String("DVL");
			identity = Identity.DVLS;
		}
		else if(part[0].equals("KKLS")){
			campus = new String("KKL");
			identity = Identity.KKLS;
		}
		else if(part[0].equals("WSTS")){
			campus = new String("WST");
			identity = Identity.WSTS;
		}
		else if(part[0].equals("DVLA")){
			campus = new String("DVL");
			identity = Identity.DVLA;
		}
		else if(part[0].equals("KKLA")){
			campus = new String("KKL");
			identity = Identity.KKLA;
		}
		else if(part[0].equals("WSTA")){
			campus = new String("WST");
			identity = Identity.WSTA;
		}
		else{
			campus = null;
			identity = Identity.None;
		}
		
		if(identity.equals(Identity.None)) {
			user_id = null;
			writer = null;
			return false;
		}
		else {
			user_id = username;
			writer = new LogWriter(user_id + ".log");
			return true;
		}
	}
	
	protected boolean Connect(){
		ipAddr = serverIPMap.get(identity);
		port = serverPortMap.get(identity);
		if(ipAddr == null || port == null)
			return false;		
		return true;
	}
	
	public ArrayList<String> createRoom(String date, String room, ArrayList<String> timeSlots) throws RemoteException{
		return null;
	}
	
	public ArrayList<Boolean> deleteRoom(String date, String room, ArrayList<String> timeSlots) throws RemoteException{
		return null;
	}
	
	public String getAvailableTimeslot(String date) {
		return null;
	}
	
	public String bookRoom(String campus_name, String date, String room, String timeSlot) throws RemoteException{
		return null;
	}
	
	public boolean cancelBook(String bookingID) throws RemoteException {
		return false;
	}

	public String changeReservation(String bookingID, String new_campus_name, String new_room_no, String new_timeslot) {
		return null;
	}
	
	
	
}
