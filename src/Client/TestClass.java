package Client;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class TestClass {
	private static String []timeSlots = {"7:30-9:30", "10:00-12:30", "13:30-16:00", "17:00-18:00", "19:00-20:00"};
	private static String date1 = "2017-09-17";
	private static String date2 = "2017-09-18";
	private static String date3 = "2017-09-19";
	private static String[] args;
	
	public static void main(String[] args_) throws Exception{
		// TODO Auto-generated method stub
		args = args_;
		
		System.out.println("\nTest case： Add record in three campus");
		TestAddRecord();
		System.out.println("\nTest case： Book local room and show booking limitation");
		TestBookLocalRoom();
		System.out.println("\nTest case： Delete record and show delete chain");
		TestDeleteDVLRecord("DVL");
		
		System.out.println("\nTest case: Book remote rooms which are in difference campus");
		ArrayList<String> bookingIDs = TestBookRemoteRoom();
		System.out.println("\nTest case： Book the same room by another student");
		TestBookingSameRoom();
		System.out.println("\nTest case： Cancel booking");
		TestCancelBooking(bookingIDs);
		
		System.out.println("Test case: Cancel a booking that wasn't booked by the user");
		TestCancelNotBooking();
		
		//System.out.println("Reset all records");
		//DeleteAddRecords();
		TestAddRecord();
		TestChangeReservation();
	}
	
	
	public static void TestAddRecord() throws MalformedURLException, RemoteException, NotBoundException {
		// Add Record for DVL campus
		Client admin = ClientFactory.createClient("DVLA1000");
		admin.Login("DVLA1000", "");
		admin.Connect();

		ArrayList<String> ret;	
		ret = admin.createRoom(date1, "201", new ArrayList<String>());
		System.out.println("DVLA1000 add Records: " + ret);
		
		ret = admin.createRoom(date2, "201", new ArrayList<String>(Arrays.asList(timeSlots)));
		System.out.println("DVLA1000 add Records: " + ret);
		
		// Add Record for KKL campus
		admin = ClientFactory.createClient("KKLA1000");
		admin.Login("KKLA1000", "");
		admin.Connect();
		ret = admin.createRoom(date2, "201", new ArrayList<String>(Arrays.asList(timeSlots)));
		System.out.println("KKLA1000 add Records: " + ret);
		
		// Add Record for WST campus
		Client admin_2 = ClientFactory.createClient("WSTA2000");
		admin_2.Login("WSTA2000", "");
		admin_2.Connect();
		ret = admin_2.createRoom(date2, "201", new ArrayList<String>(Arrays.asList(timeSlots)));
		System.out.println("WSTA2000 add Records: " + ret);
		System.out.print("\n\n");
	}

	
	public static void TestDeleteDVLRecord(String campus) throws MalformedURLException, RemoteException, NotBoundException {
		String id = campus+ "A1000";
		Client admin = ClientFactory.createClient(id);
		admin.Login(id, "");
		admin.Connect();
		
		admin.deleteRoom(date2, "201", new ArrayList<String>(Arrays.asList(timeSlots)));
		System.out.println("Delete all record in " + campus + "\n");
	}

	public static void TestBookLocalRoom() throws RemoteException, MalformedURLException, NotBoundException {
		Client student = ClientFactory.createClient("DVLS1000");
		student.Login("DVLS1000", "");
		student.Connect();
		
		for(int i=0; i<5; i++)
			BookAndPrint(student, "DVL", date2, "201", timeSlots[i]);
	}
	
	public static ArrayList<String> TestBookRemoteRoom() throws MalformedURLException, RemoteException, NotBoundException {
		Client student = ClientFactory.createClient("DVLS1000");
		student.Login("DVLS1000", "");
		student.Connect();
		
		ArrayList<String> bookingIDs = new ArrayList<String>();
		for(int i=0; i<5; i++) {
			String bookingId = BookAndPrint(student, "KKL", date2, "201", timeSlots[i]);
			if(bookingId != null)
				bookingIDs.add(bookingId);
		}
		
		return bookingIDs;
	}
	
	public static void TestBookingSameRoom() throws MalformedURLException, RemoteException, NotBoundException {
		Client student = ClientFactory.createClient("KKLS1000");
		student.Login("KKLS1000", "");
		student.Connect();
		
		for(int i=0; i<5; i++) {
			BookAndPrint(student, "KKL", date2, "201", timeSlots[i]);
		}
	}
	
	public static void TestCancelBooking(ArrayList<String> bookingIDs) throws MalformedURLException, RemoteException, NotBoundException {
		Client student = ClientFactory.createClient("DVLS1000");
		student.Login("DVLS1000", "");
		student.Connect();
		
		for(String bookingID:bookingIDs) {
			boolean ret = student.cancelBook(bookingID);
			if(ret == true)
				System.out.println("Cancel bookingID " + bookingID + " Success");
			else
				System.out.println("Cancel bookingID " + bookingID + " Failure");
		}
		String cnt = student.getAvailableTimeslot(date2);
		System.out.println("Available time slots on " + date2 + ": " + cnt);
		System.out.print("\n\n");
	}
	
	public static void TestCancelNotBooking() throws MalformedURLException, RemoteException, NotBoundException {
		Client student = ClientFactory.createClient("DVLS1000");
		student.Login("DVLS1000", "");
		student.Connect();
		String bookingId = BookAndPrint(student, "WST", date2, "201", timeSlots[0]);		
		
		Client student_2 = ClientFactory.createClient("DVLS1100");
		student_2.Login("DVLS1100", "");
		student_2.Connect();
		boolean ret = student_2.cancelBook(bookingId);
		String cnt = student_2.getAvailableTimeslot(date2);
		if(ret == false) 
			System.out.println(student_2.user_id + "CancelBooking fails.");
		else
			System.out.println(student_2.user_id + "CancelBooking success.");
		System.out.println("Available time slots on " + date2+ ": " + cnt);
		System.out.print("\n\n");
	}

	public static void TestChangeReservation() throws MalformedURLException, RemoteException, NotBoundException
	{
		System.out.println("Test case: Change the reservation");
		Client student = ClientFactory.createClient("DVLS1000");
		student.Login("DVLS1000", "");
		student.Connect();
		String bookingID = BookAndPrint(student, "WST", date2, "201", timeSlots[1]);	
		bookingID = student.changeReservation(bookingID, "KKL", String.valueOf(201), timeSlots[0]);
		if(bookingID == null) {
			System.out.println("Failed to change.");
		}
		else {
			System.out.println("Change successfully! New bookingID: " + bookingID);
		}
		String cnt = student.getAvailableTimeslot(date2);
		System.out.println("Available time slots on " + date2+ ": " + cnt);
		System.out.print("\n\n");


		System.out.println("Test case: change reservation to a room that has been booked");
		String ret = student.changeReservation(bookingID, "WST", String.valueOf(201), timeSlots[0]);
		if(ret == null) {
			System.out.println("Failed to change.");
		}
		else {
			System.out.println("Change successfully! New bookingID: " + ret);
		}
		System.out.print("\n\n");
		
		
		System.out.println("Test case: change reservation by another student who didn't book the room");
		Client student_2 = ClientFactory.createClient("WSTS1000");
		student_2.Login("WSTS1000", "");
		student_2.Connect();
		ret = student_2.changeReservation(bookingID, "DVL", String.valueOf(201), timeSlots[1]);
		if(ret == null) {
			System.out.println("Failed to change.");
		}
		else {
			System.out.println("Change successfully! New bookingID: " + ret);
		}
		System.out.println(student_2.getAvailableTimeslot(date2));
		System.out.print("\n\n");
		
		
		
	}
	
	public static void DeleteAddRecords() throws MalformedURLException, RemoteException, NotBoundException {
		// Delete all records
		ArrayList<Boolean> ret;		
		
		Client admin = ClientFactory.createClient("DVLA1000");
		admin.Login("DVLA1000", "");
		admin.Connect();
		ret = admin.deleteRoom(date2, "201", new ArrayList<String>(Arrays.asList(timeSlots)));
		
		admin = ClientFactory.createClient("KKLA1000");
		admin.Login("KKLA1000", "");
		admin.Connect();
		ret = admin.deleteRoom(date2, "201", new ArrayList<String>(Arrays.asList(timeSlots)));
		
		Client admin_2 = ClientFactory.createClient("WSTA2000");
		admin_2.Login("WSTA2000", "");
		admin_2.Connect();
		ret = admin_2.deleteRoom(date2, "201", new ArrayList<String>(Arrays.asList(timeSlots)));
	}
	
	
	
	private static String BookAndPrint(Client student, String campus, String date, String room, String timeslot) throws RemoteException {
		String response;
		
		System.out.println("Booking "+ date + " " + timeslot);
		response = student.bookRoom(campus, room, date, timeslot);
		if(response != null) {
			System.out.println("Succeed to book room in " + campus);
			System.out.println("bookingID: " + response);
		}
		else
			System.out.println("Failed to book.");
		System.out.println("Available Time slots now: " + student.getAvailableTimeslot(date2));
		System.out.print("\n");
		
		return response;
	}
	
}
