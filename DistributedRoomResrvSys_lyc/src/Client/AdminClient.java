package Client;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import tools.LogItem;
import tools.Message;
import tools.ReplicaReply;
import tools.RequestType;
import tools.SeqRequest;
import tools.UDPConnection;

public class AdminClient extends Client {
	protected AdminClient(){
		super();
	}
	
	@Override
	public ArrayList<String> createRoom(String date, String room, ArrayList<String> timeSlots) throws RemoteException {
		UDPConnection udp = new UDPConnection(ipAddr, port);
		String message = seq_num.get(campus) + " REQ" + requestID + 
						" createRoom " + user_id + " " + room + " " + date;
		for(String timeslot : timeSlots)
			message = message + " " + timeslot;
		Message req = new SeqRequest(message);
		udp.Send(req);
		ReplicaReply reply = new ReplicaReply(udp.ReceiveString(FESocket));
		String response = reply.getReturnVal();
		String[] recordID = response.split("\\s+");
		if(recordID.length==1 && recordID[0].equals("")) {
			recordID = new String[timeSlots.size()];
			for(int i=0; i<recordID.length; i++)
				recordID[i] = new String("");
		}
		
		seq_num.put(campus, seq_num.get(campus)+1);
		requestID++;
		
		for(int i=0; i<timeSlots.size(); i++) {
			String[] args = new String[] {date, String.valueOf(room), timeSlots.get(i)};
			LogItem log = new LogItem(RequestType.AddRecord, user_id,args);
			
			log.setResponse(recordID[i]);
			if(!recordID[i].equals(""))
				log.setResult(true);
			else
				log.setResult(false);
			writer.write(log);
		}
		
		return new ArrayList<String>(Arrays.asList(recordID));
	}
	
	@Override
	public ArrayList<Boolean> deleteRoom(String date, String room, ArrayList<String> timeSlots) {
		UDPConnection udp = new UDPConnection(ipAddr, port);
		String message = seq_num.get(campus) + " REQ" + requestID + 
						" deleteRoom " + user_id + " " + room + " " + date;
		for(String timeslot : timeSlots)
			message = message + " " + timeslot;
		Message req = new SeqRequest(message);
		udp.Send(req);
		ReplicaReply reply = new ReplicaReply(udp.ReceiveString(FESocket));
		String[] response = reply.getReturnVal().split("\\s+");
		boolean[] result = new boolean[response.length];
		for(int i=0; i<result.length; i++)
			result[i] = Boolean.valueOf(response[i]);
		
		seq_num.put(campus, seq_num.get(campus)+1);
		requestID++;
		
		for(int i=0; i<timeSlots.size(); i++) {
			String[] args = new String[] {date, String.valueOf(room), timeSlots.get(i)};
			LogItem log = new LogItem(RequestType.DeleteRecord, user_id, args);
			
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
	private static void testAdminFunction1()
	{
		Client admin = ClientFactory.createClient("DVLA1000");
		admin.Login("DVLA1000", "");
		
		try {
			String date1 = "2017-09-17";
			String date2 = "2017-09-18";
			String date3 = "2017-09-19";
			String []timeSlots = {"7:30-9:30", "10:00-12:30", "13:30-16:00", "17:00-18:00", "19:00-20:00"};
			ArrayList<String> ret;
			
			admin.Connect();
			ret = admin.createRoom(date1, "201", new ArrayList<String>());
			System.out.println(ret);
			
			ret = admin.createRoom(date3, "203", new ArrayList<String>());
			System.out.println(ret);
			
			ret = admin.createRoom(date2, "201", new ArrayList<String>());
			System.out.println(ret);
			
			ret = admin.createRoom(date2, "201", new ArrayList<String>(Arrays.asList(timeSlots)));
			System.out.println(ret);
			
			ret = admin.createRoom(date2, "203", new ArrayList<String>());
			System.out.println(ret);
			
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

	private static void testAdminFunction2()
	{
		Client admin = ClientFactory.createClient("KKLA1000");
		admin.Login("KKLA1000", "");
		
		try {
			String date2 = "2017-09-18";
			String []timeSlots = {"7:30-9:30", "10:00-12:30", "13:30-16:00", "17:00-18:00"};
			ArrayList<String> ret;
			
			admin.Connect();
			ret = admin.createRoom(date2, "201", new ArrayList<String>(Arrays.asList(timeSlots)));
			System.out.println(ret);
			
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		testAdminFunction1();
		testAdminFunction2();
	}
	
}
