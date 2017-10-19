package RoomResrvSys;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA_2_3.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import RemoteInterface.ServerRemote;
import RemoteInterface.ServerRemoteHelper;
import RemoteInterface.ServerRemotePOA;

public class ServerRemoteImpl extends ServerRemotePOA {
	private RoomRecorder roomRecorder;
	private String campus;
	private LogWriter writer;
	private DatagramSocket socket;
	
	
	protected ServerRemoteImpl(String campus_name, int listenPort) throws SocketException {
		super();
		
		campus = campus_name;
		roomRecorder = new RoomRecorder(campus_name, listenPort);
		writer = new LogWriter(campus+".log");
		socket = new DatagramSocket();
	}
	
	@Override
	public String[] AddRecord (String date, short room, String[] timeSlots){
		ArrayList<String> l_res = new ArrayList<String>();
		for(String item:timeSlots){
			// Prepare for log istance
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String[] args = new String[]{date, String.valueOf(room), item};
			LogItem log = new LogItem(RequestType.AddRecord, args);
			
			String recordID = null;
			try {
				recordID = roomRecorder.AddRecord(dateFormat.parse(date), room, item);
				if(recordID == null) {
					log.setResult(false);
					recordID = "";
				}
				else
					log.setResult(true);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			l_res.add(recordID);
			log.setResponse(recordID);			
			writer.write(log);	
		}
	
		return l_res.toArray(new String[l_res.size()]);
	}
	
	@Override
	public boolean[] DeleteRecord (String date, short room, String[] timeSlots){
		boolean[] result = new boolean[timeSlots.length];
		
		int i=0;
		for(String item:timeSlots){
			// Prepare for log istance
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String[] args = {date, String.valueOf(room), item};
			LogItem log = new LogItem(RequestType.DeleteRecord, args);
			
			Record record = null;
			try {
				record = roomRecorder.DeleteRecord(dateFormat.parse(date), room, item);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			if(record != null) {
				result[i++] = true;
				log.setResult(true);
			}
			else {
				result[i++] = false;
				log.setResult(false);
			}
				
			writer.write(log);
		}
		
		return result;
	}
	
	@Override
	public String Book (String stu_id, String campus, String date, short room, String timeslots){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String[] args = {stu_id, date, String.valueOf(room), timeslots};
		LogItem log = new LogItem(RequestType.Book, args);
		
		// Invocate the methods in server
		String bookingID = null;
		try {
			bookingID = roomRecorder.Book(stu_id, campus, dateFormat.parse(date), room, timeslots);
			if(bookingID == null)
			{
				bookingID = "";
				log.setResponse(bookingID);
				log.setResult(false);
			}
			else {
				log.setResponse(bookingID);
				log.setResult(true);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		writer.write(log);
		return bookingID;		
	}
	
	@Override
	public boolean CancelBook (String booking_id, String stu_id){
		String[] args = {booking_id};
		LogItem log = new LogItem(RequestType.CancelBook, args);
		
		boolean ret = roomRecorder.CancelBook(booking_id, stu_id);
		
		log.setResult(ret);
		writer.write(log);
		
		return ret;
		
	}
	
	@Override
	public String GetAvailTimeSlots (String date){
		String[] args = {date};
		LogItem log = new LogItem(RequestType.GetAvailTimeSlot, args);
				
		SendUDPDatagram("127.0.0.1", 25560, "GetAvailTimeSlot " + date);
		SendUDPDatagram("127.0.0.1", 25561, "GetAvailTimeSlot " + date);
		SendUDPDatagram("127.0.0.1", 25562, "GetAvailTimeSlot " + date);
		String s1 = ReceiveUDPDatagram();
		String s2 = ReceiveUDPDatagram();
		String s3 = ReceiveUDPDatagram();		

		log.setResult(true);
		log.setResponse(s1+s2+s3);
		writer.write(log);
		
		return s1+s2+s3;		
	}
	
	
	@Override
	public boolean ChangeReservation (String booking_id, String new_campus_name, String new_room_no, String new_timeslot){
		//TODO
		
		return false;
	}

	
	private boolean SendUDPDatagram(String targetAddr, int targetPort, String message) {
		try {
			InetAddress inetAddr = InetAddress.getByName(targetAddr);
			DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), inetAddr, targetPort);
			socket.send(packet);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return false;
	}
	
	
	private String ReceiveUDPDatagram() {
		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		try {
			socket.receive(packet);
			String message = new String(packet.getData(), 0, packet.getLength());
			return message;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	public static void main(String[] args){
		// parse parameters
		// Format: -ORBInitialHost localhost -ORBInitialPort 1050 -campus KKL -udpPort 25560
		String[] orbArgs = Arrays.copyOfRange(args, 0, 4);
		String campus = args[5];
		int port = Integer.parseInt(args[7]);
		
		try {
			ORB orb = (ORB) ORB.init(orbArgs, null);
			POA rootpoa = (POA) orb.resolve_initial_references("RootPOA");
			rootpoa.the_POAManager().activate();
			
			ServerRemoteImpl serverImpl = new ServerRemoteImpl(campus, port);
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(serverImpl);
			ServerRemote serverInterface = ServerRemoteHelper.narrow(ref);
			
			// naming context
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContext ncRef = NamingContextHelper.narrow(objRef);
			
			// bind interface object in Naming context
			NameComponent nc = new NameComponent("ServerRemote"+campus, "");
			NameComponent path[] = {nc};
			ncRef.rebind(path, serverInterface);
			System.out.println("Server of" + campus + "ready and waiting...");
			
			orb.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("Helllo World");
	}
}
