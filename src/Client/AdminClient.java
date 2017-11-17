package Client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import RoomResrvSys.LogItem;
import RoomResrvSys.RequestType;

public class AdminClient extends Client {
	protected AdminClient(){
		super();
		service = null;
	}
	
	@Override
	public ArrayList<String> AddRecord(String date, String room, ArrayList<String> timeSlots) throws RemoteException {
		String[] recordID = service.createRoom(user_id, room, date, timeSlots.toArray(new String[timeSlots.size()]));
		
		for(int i=0; i<timeSlots.size(); i++) {
			String[] args = new String[] {date, String.valueOf(room), timeSlots.get(i)};
			LogItem log = new LogItem(RequestType.AddRecord, args);
			
			log.setResponse(recordID[i]);
			if(recordID[i] != null)
				log.setResult(true);
			else
				log.setResult(false);
			writer.write(log);
		}
		
		return new ArrayList<String>(Arrays.asList(recordID));
	}
	
	@Override
	public ArrayList<Boolean> DeleteRecord(String date, String room, ArrayList<String> timeSlots) throws RemoteException {
		boolean[] result = service.deleteRoom(user_id, room, date, timeSlots.toArray(new String[timeSlots.size()]));
		
		for(int i=0; i<timeSlots.size(); i++) {
			String[] args = new String[] {date, String.valueOf(room), timeSlots.get(i)};
			LogItem log = new LogItem(RequestType.DeleteRecord, args);
			
			if(result[i] == true) {
				log.setResult(true);
				log.setResponse(String.valueOf(true));
			}
			else {
				log.setResult(false);
				log.setResponse(String.valueOf(false));
			}
			writer.write(log);
		}
		
		ArrayList<Boolean> ret = new ArrayList<Boolean>();
		for(int i=0; i<result.length; i++)
			ret.add(result[i]);
		return ret;
	}
	
	
	
	//-----------Debug------------------
	private static void testAdminFunction1(String[] args)
	{
		Client admin = ClientFactory.createClient("DVLA1000");
		admin.Login("DVLA1000", "");
		
		try {
			String date1 = "2017-09-17";
			String date2 = "2017-09-18";
			String date3 = "2017-09-19";
			String []timeSlots = {"7:30-9:30", "10:00-12:30", "13:30-16:00", "17:00-18:00", "19:00-20:00"};
			ArrayList<String> ret;
			
			admin.Connect(args);
			ret = admin.AddRecord(date1, "201", new ArrayList<String>());
			System.out.println(ret);
			
			ret = admin.AddRecord(date3, "203", new ArrayList<String>());
			System.out.println(ret);
			
			ret = admin.AddRecord(date2, "201", new ArrayList<String>());
			System.out.println(ret);
			
			ret = admin.AddRecord(date2, "201", new ArrayList<String>(Arrays.asList(timeSlots)));
			System.out.println(ret);
			
			ret = admin.AddRecord(date2, "203", new ArrayList<String>());
			System.out.println(ret);
			
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

	private static void testAdminFunction2(String[] args)
	{
		Client admin = ClientFactory.createClient("KKLA1000");
		admin.Login("KKLA1000", "");
		
		try {
			String date2 = "2017-09-18";
			String []timeSlots = {"7:30-9:30", "10:00-12:30", "13:30-16:00", "17:00-18:00"};
			ArrayList<String> ret;
			
			admin.Connect(args);
			ret = admin.AddRecord(date2, "201", new ArrayList<String>(Arrays.asList(timeSlots)));
			System.out.println(ret);
			
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		testAdminFunction1(args);
		testAdminFunction2(args);
	}
	
}
