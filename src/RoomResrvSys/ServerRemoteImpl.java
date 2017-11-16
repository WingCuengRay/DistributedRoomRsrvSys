package RoomResrvSys;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.omg.CORBA_2_3.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;
import RemoteInterface.ServerRemote;
import RemoteInterface.ServerRemoteHelper;
import RemoteInterface.ServerRemotePOA;

public class ServerRemoteImpl extends ServerRemotePOA {
	private RoomRecorder roomRecorder;
	private String campus;
	private LogWriter writer;
	private DatagramSocket socket;
	private static HashMap<String, String> hostIPMap;
	private static HashMap<String, Integer> hostPortMap;
	
	static{
		hostIPMap = new HashMap<String, String>();
		hostIPMap.put("DVL", "127.0.0.1");
		hostIPMap.put("KKL", "127.0.0.1");
		hostIPMap.put("WST", "127.0.0.1");
		
		hostPortMap = new HashMap<String, Integer>();
		hostPortMap.put("DVL", 25560);
		hostPortMap.put("KKL", 25561);
		hostPortMap.put("WST", 25562);
	}
	
	
	protected ServerRemoteImpl(String campus_name, int listenPort) throws SocketException {
		super();
		
		campus = campus_name;
		roomRecorder = new RoomRecorder(campus_name, listenPort);
		writer = new LogWriter(campus+".log");
		socket = new DatagramSocket();
	}
	
	@Override
	public String[] createRoom (String id, String room, String date, String[] timeSlots){
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
	public boolean[] deleteRoom (String id, String room, String date, String[] timeSlots){
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
	public String bookRoom(String stu_id, String campus, String room, String date, String timeslot){
		String[] args = {stu_id, date, room, timeslot};
		LogItem log = new LogItem(RequestType.Book, args);
		
		int bookingCnt = roomRecorder.GetStuBookingCnt(stu_id, date);
		if(bookingCnt >= 3)
		{
			log.setResponse(null);
			log.setResult(false);
			writer.write(log);
			return "";
		}
		
		
		String request = "Book " + stu_id + " " + campus + " " + date + " " 
				+ room + " " + timeslot;
		
		int targetPort = 0;
		String targetIP = null;
		if(campus.equals("DVL")) {
			targetPort = 25560;
			targetIP = "127.0.0.1";
		}
		else if(campus.equals("KKL")) {
			targetPort = 25561;
			targetIP = "127.0.0.1";
		}
		else if(campus.equals("WST")) {
			targetPort = 25562;
			targetIP = "127.0.0.1";
		}
		
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			return "";
		}
		boolean ret = SendUDPDatagram(socket, request, targetIP, targetPort);
		if(ret == false)
			return "";
		String bookingID = this.ReceiveUDPDatagram(socket);
		
		if(bookingID.equals("")) {
			log.setResponse(null);
			log.setResult(false);
		}
		else {
			roomRecorder.SetStuBookingCnt(stu_id, date, bookingCnt+1);
			log.setResponse(bookingID);
			log.setResult(true);
		}
		
		writer.write(log);
		return bookingID;
	}
	
	@Override
	public boolean cancelBook (String stu_id, String bookingID){
		String[] args = {bookingID};
		LogItem log = new LogItem(RequestType.CancelBook, args);
		
		// Initialize socket information
		int targetPort = 0;
		String targetIP = null;
		if(bookingID.substring(0, 3).equals("DVL")) {
			targetPort = 25560;
			targetIP = "127.0.0.1";
		}
		else if(bookingID.substring(0, 3).equals("KKL")) {
			targetPort = 25561;
			targetIP = "127.0.0.1";
		}
		else if(bookingID.substring(0, 3).equals("WST")) {
			targetPort = 25562;
			targetIP = "127.0.0.1";
		}
		
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
		
		// Get the date of booking record
		String request = "GetBookingDate " + bookingID;
		SendUDPDatagram(socket, request, targetIP, targetPort);
		String date = ReceiveUDPDatagram(socket);
		if(date.equals(""))
			return false;
		
		int bookingCnt = roomRecorder.GetStuBookingCnt(stu_id, date);
		if(bookingCnt == 0)
		{
			log.setResponse(null);
			log.setResult(false);
			writer.write(log);
			return false;
		}
		
		request = "CancelBook " + bookingID + " " + stu_id;
		boolean ret = SendUDPDatagram(socket, request, targetIP, targetPort);
		if(ret == false)
			return false;
		
		String reply = this.ReceiveUDPDatagram(socket);
		boolean isSuccess = Boolean.parseBoolean(reply);
		if(isSuccess == true)
			roomRecorder.SetStuBookingCnt(stu_id, date, bookingCnt-1);
		
		log.setResult(isSuccess);
		writer.write(log);
		return isSuccess;		
	}
	
	@Override
	public String getAvailableTimeslot(String stu_id, String date){
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
	public String changeReservation(String stu_id, String bookingID, 
			String new_campus_name, String new_room_no, String new_timeslot) {
		String[] args = {stu_id, bookingID, new_campus_name, new_room_no, new_timeslot};
		LogItem log = new LogItem(RequestType.ChangeReservation, args);
		
		String targetIP = null;
		int targetPort = 0;
		
		if(bookingID.substring(0, 3).equals("DVL")) {
			targetIP = "127.0.0.1";
			targetPort = 25560;
		}
		else if(bookingID.substring(0, 3).equals("KKL")) {
			targetIP = "127.0.0.1";
			targetPort = 25561;
		}
		else if(bookingID.substring(0, 3).equals("WST")) {
			targetIP = "127.0.0.1";
			targetPort = 25562;
		}
		
		String request;
		String reply;
		String new_bookingID;
		try {
			DatagramSocket socket = new DatagramSocket();
			request = "GetBookingDate " + bookingID;
			SendUDPDatagram(socket, request, targetIP, targetPort);
			String date = this.ReceiveUDPDatagram(socket);
			if(date.equals(""))
				throw new Exception("Can not get booking date");
			
			//Format: CanCancel bookingID stu_id
			request = "CanCancel " + bookingID + " " + stu_id;
			SendUDPDatagram(socket, request, targetIP, targetPort);
			reply = this.ReceiveUDPDatagram(socket);
			boolean canCancel = Boolean.parseBoolean(reply);
			
			//Format: CanBook room_no date timeslot
			request = "CanBook " + new_room_no + " " + date + " " + new_timeslot;
			SendUDPDatagram(socket, request, hostIPMap.get(new_campus_name), hostPortMap.get(new_campus_name));
			reply = this.ReceiveUDPDatagram(socket);
			boolean canBook = Boolean.parseBoolean(reply);
			if(!canCancel || !canBook) 
				throw new Exception("Conditions of changeReservation not satisified");	
			
			request = "Book " + stu_id + " " + new_campus_name  + " " + date 
					+ " " + new_room_no + " " + new_timeslot;
			SendUDPDatagram(socket, request, hostIPMap.get(new_campus_name), hostPortMap.get(new_campus_name));
			new_bookingID = this.ReceiveUDPDatagram(socket);
			if(new_bookingID.equals("")) 
				throw new Exception("Booking failure");
			
			request = "CancelBook " + bookingID + " " + stu_id;
			SendUDPDatagram(socket, request, targetIP, targetPort);
			reply = this.ReceiveUDPDatagram(socket);
			if(Boolean.parseBoolean(reply) == false)
			{
				request = "CancelBook " + new_bookingID + " " + stu_id;
				SendUDPDatagram(socket, request, targetIP, targetPort);
				reply = ReceiveUDPDatagram(socket);
				throw new Exception("Cannot cancel old booking");
			}
			log.setResult(true);
			log.setResponse(new_bookingID);
			
		} catch (SocketException e) {
			e.printStackTrace();
			return "";
		} catch (Exception e) {
			log.setResult(false);
			log.setResponse(null);
			return "";
		} finally {
			writer.write(log);
		}
		
		return new_bookingID;
	}
	
	@Override
	public boolean login(String id) {
		// TODO Auto-generated method stub
		return true;
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
	
	private boolean SendUDPDatagram(DatagramSocket socket, String message, String targetIP, int targetPort) {
		try {
			InetAddress inetAddr = InetAddress.getByName(targetIP);
			DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddr, targetPort);
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
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
	
	private String ReceiveUDPDatagram(DatagramSocket socket) {
		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		String message = null;
		try {
			socket.receive(packet);
			message = new String(packet.getData(), 0, packet.getLength());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return message;
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
