package RoomResrvSys;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.omg.CORBA_2_3.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

import RemoteInterface.ServerRemote;
import RemoteInterface.ServerRemoteHelper;

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
	protected ServerRemote service;
	protected final static HashMap<Identity, String> serverMap;
	
	// Initialize the static variable
	static {
		serverMap = new HashMap<Identity, String>();
		serverMap.put(Identity.DVLA, "ServerRemoteDVL");
		serverMap.put(Identity.KKLA, "ServerRemoteKKL");
		serverMap.put(Identity.WSTA, "ServerRemoteWST");
		serverMap.put(Identity.DVLS, "ServerRemoteDVL");
		serverMap.put(Identity.KKLS, "ServerRemoteKKL");
		serverMap.put(Identity.WSTS, "ServerRemoteWST");
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
		String serverName = serverMap.get(identity);
		if(serverName == null)
			return false;
	
		// get CORBA remote object
		ORB orb = (ORB) ORB.init(args, null);   // TODO - set args
		try {
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContext ncRef = NamingContextHelper.narrow(objRef);
			NameComponent nc = new NameComponent(serverName, "");
			NameComponent path[] = {nc};
			
			service = ServerRemoteHelper.narrow(ncRef.resolve(path));
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public ArrayList<String> AddRecord(String date, short room, ArrayList<String> timeSlots) throws RemoteException{
		return null;
	}
	
	public ArrayList<Boolean> DeleteRecord(String date, short room, ArrayList<String> timeSlots) throws RemoteException{
		return null;
	}
	
	public String GetAvailableTimeSlot(String date) {
		return null;
	}
	
	public String Book(String campus_name, String date, short room, String timeSlot) throws RemoteException{
		return null;
	}
	
	public boolean CancelBook(String bookingID) throws RemoteException {
		return false;
	}
	
	
	
}
