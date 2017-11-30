package Client;

public class TestRestoreData {
	private static String date2 = "2017-09-18";
	
	public static void main(String []args) {
		Client stu = ClientFactory.createClient("WSTS1000");
		Client.seq_num.put("WST", 5);
		stu.Login("WSTS1000", "");
		stu.Connect();
		String ret = stu.getAvailableTimeslot(date2);
		System.out.println(ret);
	}
}
