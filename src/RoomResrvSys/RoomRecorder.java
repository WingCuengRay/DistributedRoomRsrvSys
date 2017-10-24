package RoomResrvSys;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Date;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class RoomRecorder {
	
	private static int record_id;
	private String campus;
	private HashMap<Date, HashMap<Integer, ArrayList<Record>>> recordDateMap;
	private HashMap<String, Record> bookingIDMap;
	private ArrayList<HashMap<String, Integer>> stuBkngCntMap;  	
	private Thread thread;
	private int port;
	private ReadWriteLock lock;
	
	static {
		Random rand = new Random();
		record_id = rand.nextInt(10000);
	}
	
	public RoomRecorder(String camp, int listenPort){
		recordDateMap = new HashMap<Date, HashMap<Integer, ArrayList<Record>>>();
		bookingIDMap = new HashMap<String, Record>();
		stuBkngCntMap = new ArrayList<HashMap<String, Integer>>(55);
		for(int i=0; i<55; i++)
			stuBkngCntMap.add(new HashMap<String, Integer>());
		campus = camp;
		port = listenPort;
		lock = new ReentrantReadWriteLock();
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date expectRunTime = calendar.getTime();
		if(expectRunTime.before(new Date())) {	
			// If current time is after the expected running time, the task will be started immediately.
			// We need to avoid this.
			calendar.add(Calendar.WEEK_OF_YEAR, 1);
			expectRunTime = calendar.getTime();
		}
		
		thread = new Thread(new UDPReceiver());
		thread.start();
	}
	
	public class UDPReceiver implements Runnable {
		@Override
		public void run() {
			byte []buf = new byte[256];
			DatagramSocket socket = null;
			DatagramPacket packet = null;
			
			try {
				socket = new DatagramSocket(port);
			} catch (SocketException e1) {
				e1.printStackTrace();
				return;
			}
			
			while(true) {
				try {
					packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
				}catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				
				InetAddress targetAddr = packet.getAddress();
				int targetPort = packet.getPort();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				
				String receive = new String(packet.getData(), 0, packet.getLength());
				String[] parts = receive.split(" ");
				if(parts.length==2 && parts[0].equals("GetAvailTimeSlot")) {	
					Date date = null;
					int cnt = 0;
					try {
						date = dateFormat.parse(parts[1]);
					} catch (ParseException e) {
						e.printStackTrace();
						continue;
					}
					
					cnt = GetAvailableTimeSlot(date);
					String sent = new String(campus+": " + cnt+"; ");
					SendUDPDatagram(socket, sent, targetAddr, targetPort);
				}
				else if(parts.length==2 && parts[0].equals("GetBookingDate")) {
					String bookingID = parts[1];
					Record record = bookingIDMap.get(bookingID);
					String reply;
					if(record != null) {
						Date date = record.getDate();
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						reply = df.format(date); 
					}
					else
						reply = "";
					SendUDPDatagram(socket, reply, targetAddr, targetPort);
				}
				// params: CanCancel bookingID  stu_id
				else if(parts.length==3 && parts[0].equals("CanCancel")) {
					String bookingID = parts[1];
					String stu_id = parts[2];
					Record record = bookingIDMap.get(bookingID);
					
					String reply;
					if(record==null || !stu_id.equals(record.getBookerID()))
						reply = "false";
					else
						reply = "true";
							
					SendUDPDatagram(socket, reply, targetAddr, targetPort);
				}
				// params: CanBook room_no date timeslot
				else if(parts.length==4 && parts[0].equals("CanBook")) {
					int room_no = Integer.parseInt(parts[1]);
					Date date = null;
					try {
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						date = df.parse(parts[2]);
					} catch (ParseException e) {
						e.printStackTrace();
						continue;
					}
					String timeslot = parts[3];
					
					 boolean ret = isTimeslotAvailable(date, room_no, timeslot);
					 SendUDPDatagram(socket, String.valueOf(ret), targetAddr, targetPort);
				}
				// params: DecreaseStuCounting DVL10000
				else if(parts.length==3 && parts[0].equals("DecreaseStuCounting")) {
					String stu_id = parts[1];
					String s_date = parts[2];
					Date date = null;
					try {
						date = dateFormat.parse(s_date);
					} catch (ParseException e) {
						e.printStackTrace();
						continue;
					}
					
					lock.writeLock().lock();
					int cnt = GetStuBookingCnt(stu_id, s_date);
					SetStuBookingCnt(stu_id, s_date, cnt-1);
					lock.writeLock().unlock();
				}
				// params: CancelBook DVL123481759134 DVL10000
				else if(parts.length==3 && parts[0].equals("CancelBook")) {
					String bookingID = parts[1];
					String stu_id = parts[2];
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					
					Record record = bookingIDMap.get(bookingID);
					boolean ret = CancelBook(bookingID, stu_id);
					String message = String.valueOf(ret);
					
					SendUDPDatagram(socket, message, targetAddr, targetPort);
				}
				//params: Book DVL10000 DVL 2017-9-18 201 7:30-9:30
				else if(parts.length==6 && parts[0].equals("Book")) {
					String stu_id = parts[1];
					String targetCampus = parts[2];
					int room = Integer.parseInt(parts[4]);
					String timeslot = parts[5];
					Date date = null;
					
					try {
						date = dateFormat.parse(parts[3]);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					String bookingID = Book(stu_id, targetCampus, date, room, timeslot);
					if(bookingID == null)
						bookingID = new String("");
					SendUDPDatagram(socket, bookingID, targetAddr, targetPort);
				}
			}
			
		}
	}
	
	
	
	//@return: If successfully, return the random booking id. Otherwise null. 
	public String Book(String stu_id, String targetCampus, Date date, int room, String time_slot){
		String bookingID = null;
		
		lock.writeLock().lock();
		try {
			Record record = getRecord(date, room, time_slot);
			if(record == null || record.isOccupied()==true) { 
				return null;			
			}
			int randVal;
			Random rand = new Random();
			randVal = rand.nextInt(Integer.MAX_VALUE);
			bookingID = new String(campus + String.valueOf(randVal));
			bookingIDMap.put(bookingID, record);
			
			record.SetBookerID(stu_id);
			record.setOccupied(true);
		}
		finally {
			lock.writeLock().unlock();
		}
		
		return bookingID;
	}
	
	//@return: If cancel successfully, return true. Otherwise false 
	public boolean CancelBook(String bookingID, String stu_id){
		if(bookingID == null)
			return false;
		
		lock.writeLock().lock();
		try {
			Record record = bookingIDMap.get(bookingID);
			if(record == null || record.isOccupied()==false || !record.getBookerID().equals(stu_id))
				return false;
			bookingIDMap.remove(bookingID);
			record.SetBookerID(null);
			record.setOccupied(false);
		}
		finally {
			lock.writeLock().unlock();
		}
		return true;
	}
	
	
	public String AddRecord(Date date, int room, String timeSlot){
		if(isRecordExist(date, room, timeSlot) == true) {
			return null;
		}
		
		lock.writeLock().lock();
		HashMap<Integer, ArrayList<Record>> submap = recordDateMap.get(date);
		if(submap == null){
			HashMap<Integer, ArrayList<Record>> newsubmap = new HashMap<Integer, ArrayList<Record>>();
			recordDateMap.put(date, newsubmap);
			submap = newsubmap;
		}
		
		
		ArrayList<Record> records = submap.get(room);
		if(records == null){
			submap.put(room, new ArrayList<Record>());
			records = submap.get(room);
		}
		Record record = new Record(timeSlot, record_id, date);
		IncrementRecordID();
		records.add(record);
		lock.writeLock().unlock();
		
		return record.getRecordID();
	}
	
	
	// @return: If successfully, return the record that was deleted. Otherwise return null
	public Record DeleteRecord(Date date, int room, String time_slot){
		if(isRecordExist(date, room, time_slot) == false)
			return null;
		
		lock.writeLock().lock();
		ArrayList<Record> records = recordDateMap.get(date).get(room);
		for(int i=0; i<records.size(); i++){
			if(time_slot.equals(records.get(i).getTimeSlot())){
				Record del = records.remove(i);
				if(del.isOccupied()) {
					String bookerID = del.getBookerID();
					
					int port = 0;
					if(bookerID.contains("DVL"))
						port = 25560;
					else if(bookerID.contains("KKL"))
						port = 25561;
					else
						port = 25562;
					
					DatagramSocket socket;
					try {
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						socket = new DatagramSocket();
						String message = "DecreaseStuCounting " + bookerID + " " + df.format(del.getDate()); 
						SendUDPDatagram(socket, message, InetAddress.getByName("127.0.0.1"), port);
					} catch (SocketException | UnknownHostException e) {
						e.printStackTrace();
					}
				}
				
				lock.writeLock().unlock();
				return del;
			}
		}
		lock.writeLock().unlock();
		
		return null;
	}
	
	
	public int GetAvailableTimeSlot(Date date) {
		int cnt = 0;
		
		lock.readLock().lock();
		HashMap<Integer, ArrayList<Record>> subMap = recordDateMap.get(date);
		if(subMap == null) {
			lock.readLock().unlock();
			return 0;
		}
		for(Integer each_room:subMap.keySet()) {
			ArrayList<Record> records = subMap.get(each_room);
			for(Record record:records) {
				if(record.isOccupied() == false)
					cnt++;
			}
		}
		lock.readLock().unlock();
		
		return cnt;
	}
	

	public int GetStuBookingCnt(String stu_id, String s_date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
		try {
			date = df.parse(s_date);
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int week = cal.get(Calendar.WEEK_OF_YEAR);
		
		HashMap<String, Integer> stuMap = stuBkngCntMap.get(week);
		Integer cnt = stuMap.get(stu_id);
		if(cnt == null)
			return 0;
		else
			return cnt;
	}
	
	public void SetStuBookingCnt(String stu_id, String s_date, int cnt) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
		try {
			date = df.parse(s_date);
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int week = cal.get(Calendar.WEEK_OF_YEAR);
		
		HashMap<String, Integer> stuMap = stuBkngCntMap.get(week);
		stuMap.put(stu_id, cnt);
		
		return;
	}

	
	private synchronized static void IncrementRecordID(){
		record_id++;
	}
	
	private boolean isRecordExist(Date date, int room, String time_slot){
		lock.readLock().lock();
		try {
			HashMap<Integer, ArrayList<Record>> submap = recordDateMap.get(date);
			if(submap == null)
				return false;
			
			ArrayList<Record> time_slots = submap.get(room);
			if(time_slots == null)
				return false;
			for(Record item:time_slots){
				if(time_slot.equals(item.getTimeSlot()))
					return true;
			}
		}finally {
			lock.readLock().unlock();
		}
		
		
		return false;
	}


	private Record getRecord(Date date, int room, String time_slot){
		lock.readLock().lock();
		try {
			HashMap<Integer, ArrayList<Record>> submap = recordDateMap.get(date);
			if(submap == null)
				return null;
			
			ArrayList<Record> time_slots = submap.get(room);
			if(time_slots == null)
				return null;
			for(Record item:time_slots){
				if(time_slot.equals(item.getTimeSlot()))
					return item;
			}
		}finally {
			lock.readLock().unlock();
		}
		
		return null;
	}
	
	private boolean isTimeslotAvailable(Date date, int room_no, String timeslot) {
		HashMap<Integer, ArrayList<Record>> roomMap = recordDateMap.get(date);
		if(roomMap == null)
			return false;
		
		ArrayList<Record> records = roomMap.get(room_no);
		if(records == null)
			return false;
		
		for(Record item:records) {
			if(timeslot.equals(item.getTimeSlot())) {
				if(!item.isOccupied())
					return true;
			}
		}
		
		return false;
	}
	
	private boolean SendUDPDatagram(DatagramSocket socket, String message, InetAddress targetIP, int port) {
		DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, targetIP, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void PrintMap(){
		for(Date item : recordDateMap.keySet()){
			System.out.println(item + ":");
			HashMap<Integer, ArrayList<Record>> subMap = recordDateMap.get(item);
			for(Integer room : subMap.keySet()){
				System.out.println("\t" + room + ":" );
				for(Record record : subMap.get(room)){
					String output = "\t\tTime: " + record.getTimeSlot() + ", " + " Record_ID: " + record.getRecordID()
									+ " Status: " + record.isOccupied() + " Booker_ID: " + record.getBookerID();
					System.out.println(output);
				}
			}
		}
	}
	
}
