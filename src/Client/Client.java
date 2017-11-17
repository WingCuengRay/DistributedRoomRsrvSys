package Client;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import RoomResrvSys.LogWriter;
import RoomResrvSys.RemoteServerInterface;

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
	protected RemoteServerInterface service;
	protected final static HashMap<Identity, String> serverMap;
	
	// Initialize the static variable
	static {
		serverMap = new HashMap<Identity, String>();
		serverMap.put(Identity.DVLA, "http://localhost:10030/RemoteServer?wsdl");
		serverMap.put(Identity.KKLA, "http://localhost:10031/RemoteServer?wsdl");
		serverMap.put(Identity.WSTA, "http://localhost:10032/RemoteServer?wsdl");
		serverMap.put(Identity.DVLS, "http://localhost:10030/RemoteServer?wsdl");
		serverMap.put(Identity.KKLS, "http://localhost:10031/RemoteServer?wsdl");
		serverMap.put(Identity.WSTS, "http://localhost:10032/RemoteServer?wsdl");
	}
	
	
	public Client() {
		identity = Identity.None;
		service = null;
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
	
	protected boolean Connect(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		String serverUrl = serverMap.get(identity);
		if(serverUrl == null)
			return false;
	
		URL url = new URL(serverUrl);
		QName qName = new QName("http://RoomResrvSys/", "ServerRemoteImplService");
		Service r_service = Service.create(url, qName);
		service = r_service.getPort(RemoteServerInterface.class);
		
		return true;
	}
	
	public ArrayList<String> AddRecord(String date, String room, ArrayList<String> timeSlots) throws RemoteException{
		return null;
	}
	
	public ArrayList<Boolean> DeleteRecord(String date, String room, ArrayList<String> timeSlots) throws RemoteException{
		return null;
	}
	
	public String GetAvailableTimeSlot(String date) {
		return null;
	}
	
	public String Book(String campus_name, String date, String room, String timeSlot) throws RemoteException{
		return null;
	}
	
	public boolean CancelBook(String bookingID) throws RemoteException {
		return false;
	}

	public String ChangeReservation(String bookingID, String new_campus_name, String new_room_no, String new_timeslot) {
		return null;
	}
	
	
	
}
