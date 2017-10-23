package RoomResrvSys;

public class StudentClient extends Client {
	protected StudentClient(){
		super();
	}
	
	@Override
	public String GetAvailableTimeSlot(String date){
		String response = service.GetAvailTimeSlots(date);
		
		String[] args = new String[] {date};
		LogItem log = new LogItem(RequestType.GetAvailTimeSlot, args);
		log.setResult(true);
		log.setResponse(response);
		writer.write(log);
		
		return response;
	}
	
	@Override
	public String Book(String campus_name, String date, short room, String timeSlot){
		String bookingID = null;
		bookingID = service.Book(user_id, campus_name, date, room, timeSlot);
		
		String[] args = new String[] {user_id, date, String.valueOf(room), timeSlot};
		LogItem log = new LogItem(RequestType.Book, args);
		
		log.setResponse(bookingID);
		if(bookingID.equals("")) {
			bookingID = null;
			log.setResult(false);
		}
		else 
			log.setResult(true);
		writer.write(log);
		
		return bookingID;
		
	}
	
	@Override
	public boolean CancelBook(String bookingID){
		if(bookingID == null) {
			return false;
		}
		
		boolean ret = service.CancelBook(bookingID, this.user_id);
		
		String[] args = {bookingID};
		LogItem log = new LogItem(RequestType.CancelBook, args);
		log.setResult(ret);
		writer.write(log);
		
		return ret;
	}
	
	
	//--------------- Debug ------------------
	private static void testStuFunction(String []args) {
		Client student = ClientFactory.createClient("DVLS1000");
		student.Login("DVLS1000", "");
		
		try {		
			String date1 = "2017-09-17";
			String date2 = "2017-09-18";
			String []timeSlots = {"7:30-9:30", "10:00-12:30", "13:30-16:00", "17:00-18:00", "19:00-20:00"};
			Boolean isSuccess;
			String response;
			String availTimeSlots;
			
			student.Connect(args);
			
			// Test GetAvailableTimeSlot()
			{
				response = student.GetAvailableTimeSlot(date1);
				System.out.println(date1 + ": " +response);
				response = student.GetAvailableTimeSlot(date2);
				System.out.println(date1 + ": " +response);
			}
			System.out.print("\n\n");
			
			
			// Test Add/Delete Booking
			{
				response = student.Book("DVL", date2, (short)201, timeSlots[0]);
				System.out.println("bookingID:" + response);
				isSuccess = student.CancelBook(response);
				System.out.println("Cancel Booking Result: " + isSuccess);
				availTimeSlots = student.GetAvailableTimeSlot(date2);
				System.out.println(response + " " + availTimeSlots);
				
			}
			System.out.print("\n\n");
			
			
			// Test booking limitation
			{
				for(int i=1; i<=2; i++)
				{
					String bookingID = student.Book("DVL", date2, (short)201, timeSlots[i]);
					availTimeSlots = student.GetAvailableTimeSlot(date2);
					System.out.println(bookingID);
					System.out.println(availTimeSlots);
				}
				
				System.out.println("\n\n");
				String bookingID = student.Book("KKL", date2, (short)201, "10:00-12:30");
				availTimeSlots = student.GetAvailableTimeSlot(date2);
				System.out.println(bookingID + " " + availTimeSlots);
				
				
				String bookingID2 = student.Book("KKL", date2, (short)201, timeSlots[3]);
				availTimeSlots = student.GetAvailableTimeSlot(date2);
				System.out.println(bookingID2 + " " + availTimeSlots);
				
				boolean ret = student.CancelBook(bookingID);
				System.out.println("Cancel booking: " + ret);
				availTimeSlots = student.GetAvailableTimeSlot(date2);
				System.out.println(availTimeSlots + "\n\n");
				
				bookingID2 = student.Book("KKL", date2, (short)201, timeSlots[3]);
				availTimeSlots = student.GetAvailableTimeSlot(date2);
				System.out.println(bookingID2 + " " + availTimeSlots);
			}			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		testStuFunction(args);
		
		return;
	}
	
}
