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
import java.util.HashMap;

import tools.LogItem;
import tools.LogWriter;
import tools.RequestType;


public class ServerRemoteImpl implements RemoteServerInterface {
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
	
	public ServerRemoteImpl()
	{
		
	}
	
	public ServerRemoteImpl(String campus_name, int listenPort) throws SocketException {
		super();
		
		campus = campus_name;
		roomRecorder = new RoomRecorder(campus_name, listenPort);
		writer = new LogWriter(campus+".log");
		socket = new DatagramSocket();
	}
	
	@Override
	public ArrayList<Boolean> createRoom (String id, String room, String date, ArrayList<String> timeSlots){
		ArrayList<Boolean> ret = new ArrayList<Boolean>();
		for(String item:timeSlots){
			// Prepare for log istance
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String[] args = new String[]{date, String.valueOf(room), item};
			LogItem log = new LogItem(RequestType.AddRecord, id, args);
			
			
			String recordID = null;
			
			try {
				recordID = roomRecorder.AddRecord(dateFormat.parse(date), room, item);
			} catch (ParseException e) {
				e.printStackTrace();
				return ret;
			}
			if(recordID == null) {
				log.setResult(false);
				ret.add(false);
			}
			else{
				log.setResult(true);
				ret.add(true);
			}

			log.setResponse(recordID);			
			writer.write(log);	
		}
		
		
		return ret;
	}
	
	@Override
	public ArrayList<Boolean> deleteRoom (String id, String room, String date, ArrayList<String> timeSlots){
		ArrayList<Boolean> result = new ArrayList<Boolean>();
		
		int i=0;
		for(String item:timeSlots){
			// Prepare for log istance
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String[] args = {date, String.valueOf(room), item};
			LogItem log = new LogItem(RequestType.DeleteRecord, id, args);
			
			Record record = null;
			try {
				record = roomRecorder.DeleteRecord(dateFormat.parse(date), room, item);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			if(record != null) {
				result.add(true);
				log.setResult(true);
			}
			else {
				result.add(false);
				log.setResult(false);
			}
				
			writer.write(log);
		}
		
		return result;
	}
	
	@Override
	public String bookRoom(String stu_id, String campus, String room, String date, String timeslot){
		String[] args = {stu_id, date, room, timeslot};
		LogItem log = new LogItem(RequestType.Book, stu_id, args);
		
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
		LogItem log = new LogItem(RequestType.CancelBook, stu_id, args);
		
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
		LogItem log = new LogItem(RequestType.GetAvailTimeSlot, stu_id, args);
				
		SendUDPDatagram("127.0.0.1", 25560, "GetAvailTimeSlot " + date);
		SendUDPDatagram("127.0.0.1", 25561, "GetAvailTimeSlot " + date);
		SendUDPDatagram("127.0.0.1", 25562, "GetAvailTimeSlot " + date);
		String s1 = ReceiveUDPDatagram();
		String s2 = ReceiveUDPDatagram();
		String s3 = ReceiveUDPDatagram();
		
		ArrayList<String> ss = new ArrayList<String>() ;
		ss.add(s1);
		ss.add(s2);
		ss.add(s3);
		ss.sort(String::compareTo);
		String ret = ss.get(0)+ " " + ss.get(1) + " " + " " + ss.get(2);

		log.setResult(true);
		log.setResponse(ret);
		writer.write(log);
		
		return ret;		
	}

	
	@Override
	public String changeReservation(String stu_id, String bookingID, 
			String new_campus_name, String new_room_no, String new_timeslot) {
		String[] args = {stu_id, bookingID, new_campus_name, new_room_no, new_timeslot};
		LogItem log = new LogItem(RequestType.ChangeReservation, stu_id, args);
		if(bookingID == null || bookingID.equals("null"))
			return null;
		
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
		
		return true;
	}
	
	@Override
	public boolean storeData(String f_name) {
		return roomRecorder.storeData(f_name);
	}
	
	@Override
	public boolean loadData(String f_name) {
		return roomRecorder.loadData(f_name);
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
	

}
