package Client;

import java.net.DatagramSocket;

import tools.LogItem;
import tools.Message;
import tools.SeqRequest;
import tools.UDPConnection;
import tools.ReplicaReply;
import tools.RequestType;

public class StudentClient extends Client {
	protected StudentClient(){
		super();
	}
	
	@Override
	public String getAvailableTimeslot(String date){
		UDPConnection udp = new UDPConnection(ipAddr, port);
		String message = seq_num.get(campus) + " REQ" + requestID + 
						" getAvailableTimeslot " + user_id + " " +  date;
		Message req = new SeqRequest(message);
		udp.Send(req);
		ReplicaReply reply = new ReplicaReply(udp.ReceiveString(FESocket));
		String response = reply.getReturnVal();
		
		String[] args = new String[] {date};
		LogItem log = new LogItem(RequestType.GetAvailTimeSlot, user_id, args);
		log.setResult(true);
		log.setResponse(response);
		writer.write(log);
		
		seq_num.put(campus, seq_num.get(campus)+1);
		requestID++;
		
		return response;
	}
	
	@Override
	public String bookRoom(String campus_name, String room, String date, String timeSlot){
		UDPConnection udp = new UDPConnection(ipAddr, port);
		String message = seq_num.get(campus) + " REQ" + requestID + 
						" bookRoom " + user_id + " " +  campus_name + " " + room + " " + date + " " + timeSlot;
		Message req = new SeqRequest(message);
		udp.Send(req);
		ReplicaReply reply = new ReplicaReply(udp.ReceiveString(FESocket));
		String bookingID = reply.getReturnVal();
	
		String[] args = new String[] {user_id, date, String.valueOf(room), timeSlot};
		LogItem log = new LogItem(RequestType.Book, user_id, args);
		
		log.setResponse(bookingID);
		if(bookingID.equals("")) {
			bookingID = null;
			log.setResult(false);
		}
		else 
			log.setResult(true);
		writer.write(log);
		
		seq_num.put(campus, seq_num.get(campus)+1);
		requestID++;
		
		return bookingID;
		
	}
	
	@Override
	public boolean cancelBook(String bookingID){
		if(bookingID == null) {
			return false;
		}
		
		UDPConnection udp = new UDPConnection(ipAddr, port);
		String message = seq_num.get(campus) + " REQ" + requestID + 
						" cancelBook " + user_id + " " +  bookingID;
		Message req = new SeqRequest(message);
		udp.Send(req);
		ReplicaReply reply = new ReplicaReply(udp.ReceiveString(FESocket));
		boolean ret = Boolean.valueOf(reply.getReturnVal());
		
		String[] args = {bookingID};
		LogItem log = new LogItem(RequestType.CancelBook, user_id, args);
		log.setResult(ret);
		writer.write(log);
		
		seq_num.put(campus, seq_num.get(campus)+1);
		requestID++;
		
		return ret;
	}
	
	@Override
	public String changeReservation(String bookingID, String new_campus_name, 
			String new_room_no, String new_timeslot) {
		UDPConnection udp = new UDPConnection(ipAddr, port);
		String message = seq_num.get(campus) + " REQ" + requestID + 
						" changeReservation " + user_id + " " +  bookingID + " " + new_campus_name + " " + new_room_no + " " + new_timeslot;
		Message req = new SeqRequest(message);
		udp.Send(req);
		ReplicaReply reply = new ReplicaReply(udp.ReceiveString(FESocket));
		String new_bookingID = reply.getReturnVal();
		
		seq_num.put(campus, seq_num.get(campus)+1);
		requestID++;
		
		String[] args = {bookingID, new_campus_name, new_room_no, new_timeslot};
		LogItem log = new LogItem(RequestType.ChangeReservation, user_id, args);
		if(new_bookingID.equals("")) {
			log.setResult(false);
			log.setResponse(null);
			writer.write(log);
			return null;
		}
		else {
			log.setResult(true);
			log.setResponse(new_bookingID);
			writer.write(log);
			return new_bookingID;
		}
	}
	
	
	//--------------- Debug ------------------
	private static void testStuFunction() {
		Client student = ClientFactory.createClient("DVLS1000");
		student.Login("DVLS1000", "");
		
		try {		
			String date1 = "2017-09-17";
			String date2 = "2017-09-18";
			String []timeSlots = {"7:30-9:30", "10:00-12:30", "13:30-16:00", "17:00-18:00", "19:00-20:00"};
			Boolean isSuccess;
			String response;
			String availTimeSlots;
			
			student.Connect();
			
			// Test getAvailableTimeslot()
			{
				response = student.getAvailableTimeslot(date1);
				System.out.println(date1 + ": " +response);
				response = student.getAvailableTimeslot(date2);
				System.out.println(date1 + ": " +response);
			}
			System.out.print("\n\n");
			
			
			// Test Add/Delete Booking
			{
				response = student.bookRoom("DVL", "201", date2, timeSlots[0]);
				System.out.println("bookingID:" + response);
				isSuccess = student.cancelBook(response);
				System.out.println("Cancel Booking Result: " + isSuccess);
				availTimeSlots = student.getAvailableTimeslot(date2);
				System.out.println(response + " " + availTimeSlots);
				
			}
			System.out.print("\n\n");
			
			
			// Test booking limitation
			{
				for(int i=0; i<2; i++)
				{
					String bookingID = student.bookRoom("DVL", "201", date2, timeSlots[i]);
					availTimeSlots = student.getAvailableTimeslot(date2);
					System.out.println(bookingID);
					System.out.println(availTimeSlots);
				}
				
				System.out.println("\n\n");
				String bookingID = student.bookRoom("KKL", "201", date2, timeSlots[1]);
				availTimeSlots = student.getAvailableTimeslot(date2);
				System.out.println(bookingID + " " + availTimeSlots);
				
				
				String bookingID2 = student.bookRoom("KKL", "201", date2, timeSlots[2]);		//Should fail
				availTimeSlots = student.getAvailableTimeslot(date2);
				System.out.println(bookingID2 + " " + availTimeSlots);
					
				boolean ret = student.cancelBook(bookingID);			// Should success
				System.out.println("Cancel booking: " + ret);
				availTimeSlots = student.getAvailableTimeslot(date2);
				System.out.println(availTimeSlots + "\n\n");
				
				bookingID2 = student.bookRoom("KKL", "201", date2, timeSlots[2]);		// Should success
				availTimeSlots = student.getAvailableTimeslot(date2);
				System.out.println(bookingID2 + " " + availTimeSlots);
			
				// Test changeReservation
				String new_bookingID = student.changeReservation(bookingID2, "KKL", String.valueOf(201), timeSlots[3]);
				System.out.println("ChangeReservation: " + new_bookingID);
				availTimeSlots = student.getAvailableTimeslot(date2);
				System.out.println(availTimeSlots + "\n\n");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		testStuFunction();
		
		return;
	}
	
}
