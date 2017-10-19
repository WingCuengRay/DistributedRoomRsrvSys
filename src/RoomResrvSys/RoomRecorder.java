package RoomResrvSys;

import java.util.HashMap;
import java.util.Random;
import java.util.TimerTask;
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
	private HashMap<String, Integer> stuBkngCntMap;		//TODO: implement booking times limitation every week
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
		stuBkngCntMap = new HashMap<String, Integer>();
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
				Date date = null;
				
				String receive = new String(packet.getData(), 0, packet.getLength());
				String[] parts = receive.split(" ");
				if(parts.length==2 && parts[0].equals("GetAvailTimeSlot")) {					
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
				// params: DecreaseStuCounting DVL10000
				else if(parts.length==2 && parts[0].equals("DecreaseStuCounting")) {
					String stu_id = parts[1];
					
					lock.writeLock().lock();
					Integer cnt = stuBkngCntMap.get(stu_id);
					if(cnt!=null && cnt>0)
						stuBkngCntMap.put(stu_id, cnt-1);
					lock.writeLock().unlock();
				}
				// params: CancelBook DVL123481759134 DVL10000
				else if(parts.length==3 && parts[0].equals("CancelBook")) {
					String bookingID = parts[1];
					String stu_id = parts[2];
					
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
					try {
						date = dateFormat.parse(parts[3]);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					String bookingID = Book(stu_id, targetCampus, date, room, timeslot);
					if(bookingID == null)
						bookingID = new String("null");
					SendUDPDatagram(socket, bookingID, targetAddr, targetPort);
				}
			}
			
		}
	}
	
	
	
	//@return: If successfully, return the random booking id. Otherwise null. 
	public String Book(String stu_id, String targetCampus, Date date, int room, String time_slot){
		String bookingID = null;
		
		if(stu_id.contains(campus)) {
			// Check if the booking count of students was the maximum count.
			Integer bookingCnt = stuBkngCntMap.get(stu_id);
			if(bookingCnt!=null&&bookingCnt>=3)
				return null;
		}
		
		if(targetCampus.equals(campus)) {
			// 瑕侀璁㈡埧闂寸殑璇锋眰宸茶鍙戦�佸埌瀵瑰簲鐨勬湇鍔″櫒澶勭悊
			lock.writeLock().lock();
			Record record = getRecord(date, room, time_slot);
			if(record == null || record.isOccupied()==true) { 
				lock.writeLock().unlock();
				return null;			
			}
			int randVal;
			Random rand = new Random();
			randVal = rand.nextInt(Integer.MAX_VALUE);
			bookingID = new String(campus + String.valueOf(randVal));
			bookingIDMap.put(bookingID, record);
			
			record.SetBookerID(stu_id);
			record.setOccupied(true);
			if(stu_id.contains(campus)) {
				// Increase the booking count of students if the operation was ran on local server
				Integer bookingCnt = stuBkngCntMap.get(stu_id);
				if(bookingCnt == null)
					stuBkngCntMap.put(stu_id, 1);
				else if(bookingCnt < 3)
					stuBkngCntMap.put(stu_id, bookingCnt+1);
			}
			lock.writeLock().unlock();
			
			return bookingID;
		}
		else {
			// 鑻ヤ笉鍦ㄥ悓涓�涓牎鍖猴紝鍒欓�氳繃 UDP 鍚戣繙绋嬫湇鍔″櫒鍙戦�侀璁㈣姹傘��
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String request = "Book " + stu_id + " " + targetCampus + " " + dateFormat.format(date) + " "
					+ room + " " + time_slot;
			if(targetCampus.equals("DVL"))
				port = 25560;
			else if(targetCampus.equals("KKL"))
				port = 25561;
			else if(targetCampus.equals("WST"))
				port = 25562;
			else
				return null;
			
			InetAddress targetIP;
			DatagramSocket socket;
			try {
				targetIP = InetAddress.getByName("127.0.0.1");
				socket = new DatagramSocket();
				this.SendUDPDatagram(socket, request, targetIP, port);
				bookingID = this.ReceiveUDPDatagram(socket);
				if(bookingID==null || bookingID.equals("null"))
					return null;
			} catch (UnknownHostException | SocketException e) {
				e.printStackTrace();
				return null;
			}
			
			lock.writeLock().lock();
			Integer bookingCnt = stuBkngCntMap.get(stu_id);
			if(bookingCnt == null)
				stuBkngCntMap.put(stu_id, 1);
			else if(bookingCnt < 3)
				stuBkngCntMap.put(stu_id, bookingCnt+1);
			lock.writeLock().unlock();
			
			return bookingID;
		}
	}
	
	//@return: If cancel successfully, return true. Otherwise false 
	public boolean CancelBook(String bookingID, String stu_id){
		if(bookingID == null)
			return false;
		
		if(bookingID.contains(campus)) {
			// Cancel booking on local server		
			lock.writeLock().lock();
			try {
				Record record = bookingIDMap.get(bookingID);
				if(record == null || record.isOccupied()==false || !record.getBookerID().equals(stu_id))
					return false;
				
				if(stu_id.contains(campus)) {
					// 濡傛灉褰撳墠鐢ㄦ埛鍦ㄦ湰鍦版湇鍔″櫒涓婅繘琛屼簡鍙栨秷鎿嶄綔锛屽垯鍙互鐩存帴瀵规湰鍦拌〃杩涜鎿嶄綔
					Integer bookingCnt = stuBkngCntMap.get(record.getBookerID());
					if(bookingCnt==null || bookingCnt==0)
						return false;
					stuBkngCntMap.put(record.getBookerID(), bookingCnt-1);
				}
				
				bookingIDMap.remove(bookingID);
				record.SetBookerID(null);
				record.setOccupied(false);
			}finally {
				lock.writeLock().unlock();
			}
			
			return true;
		}
		else {
			// Cancel booking through UDP remote connection
			int targetPort = 0;
			if(bookingID.contains("DVL")) 
				targetPort = 25560;
			else if(bookingID.contains("KKL"))
				targetPort = 25561;
			else if(bookingID.contains("WST"))
				targetPort = 25562;
			String message = "CancelBook " + bookingID + " " + stu_id;
			
			InetAddress targetIP = null;;
			DatagramSocket socket;
			try {
				targetIP = InetAddress.getByName("127.0.0.1");
				socket = new DatagramSocket();
			} catch (UnknownHostException | SocketException e) {
				e.printStackTrace();
				return false;
			}
			this.SendUDPDatagram(socket, message, targetIP, targetPort);
			String returnVal = this.ReceiveUDPDatagram(socket);
			if(returnVal==null || returnVal.equals("false"))
				return false;
			else {
				// 鍑忓皬鐢ㄦ埛鐨勬�婚璁㈡鏁� - booking cnt
				lock.writeLock().lock();
				Integer bookingCnt = stuBkngCntMap.get(stu_id);
				if(bookingCnt==null || bookingCnt==0)
					return false;
				stuBkngCntMap.put(stu_id, bookingCnt-1);
				lock.writeLock().unlock();
				
				return true;
			}	
		}
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
		Record record = new Record(timeSlot, record_id);
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
						socket = new DatagramSocket();
						String message = "DecreaseStuCounting " + bookerID; 
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
