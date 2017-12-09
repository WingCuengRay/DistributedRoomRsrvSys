package RoomResrvSys;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import tools.Message;
import tools.MistakeToRM;
import tools.ReplicaReply;
import tools.ResendRequest;
import tools.SeqRequest;
import tools.UDPConnection;

public class RequestWorker extends Thread {
	private RemoteServerInterface service;
	private String replicaID;
	private String campus;
	private AtomicBoolean flag = new AtomicBoolean(true);		// Set flag to be false if we want intentional error msg
	
	private static AtomicInteger ack_num;
	private static PriorityQueue<SeqRequest> holdback;
	
	private static String datafile_loc = "./ser/";
	private static final String FE_Addr;
	private static final int FE_Port;
	private static final String SEQ_Addr;
	private static final int SEQ_Port;
	
	static {
		ack_num = new AtomicInteger(0);
		holdback = new PriorityQueue<SeqRequest>(50);
		
		//TODO
		FE_Addr = "192.168.0.146";
		FE_Port = 13360;
		SEQ_Addr = "192.168.0.153";
		SEQ_Port = 13370;
	}
	
	public RequestWorker(RemoteServerInterface srv, String r_id, String campus) {
		this.service = srv;
		this.replicaID = r_id;
		this.campus = campus;
		
		Thread WrgAnswrFixer = new Thread(new Runnable() {
			@Override
			public void run()
			{
				DatagramSocket listener = null;
				try {
					int port = 13325;
					if(campus.equals("DVL"))
						port = 13325;
					else if(campus.equals("KKL"))
						port = 13335;
					else if(campus.equals("WST"))
						port = 13345;
					listener = new DatagramSocket(port);
				} catch (SocketException e) {
					e.printStackTrace();
					return;
				}
				UDPConnection udp = new UDPConnection();
				
				while(true) {
					DatagramPacket dp = udp.ReceivePacket(listener);
					MistakeToRM msg = new MistakeToRM(dp);
					if(msg.getStatus() == false)
					{
						flag.set(true);
					}
				}
			}
		});
		WrgAnswrFixer.start();
	}
	
	@Override
	public void run() {
		while(true) {
			SeqRequest request;
			
			synchronized(holdback) {
				request = holdback.peek();
				if(request == null)		// No message available
				{
					try {
						holdback.wait();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				else if(request.getSeqNum() > ack_num.get()+1){
					// Missing requests exist
					Message resendReq = new ResendRequest(ack_num.get()+1, replicaID, campus);
					UDPConnection udpsender = new UDPConnection(SEQ_Addr, SEQ_Port);
					udpsender.Send(resendReq);
					try {
						holdback.wait();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				else
					request = holdback.remove();
			}
			ack_num.incrementAndGet();
			
			
			ArrayList<String> function = request.getFunction();
			String name = function.get(0);
			ReplicaReply message = null;
			switch(name) {
				case "createRoom":
				{
					if(function.size() < 4) 
						continue;
					String id = function.get(1);
					String room = function.get(2);
					String date = function.get(3);
					ArrayList<String> timeSlots = new ArrayList<String>();
					for(int i=4; i<function.size(); i++)
						timeSlots.add(function.get(i));
					ArrayList<Boolean> ret = service.createRoom(id, room, date, timeSlots);
					
					message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret);
					break;
				}
				case "deleteRoom":
				{
					if(function.size() < 4) 
						continue;
					String id = function.get(1);
					String room = function.get(2);
					String date = function.get(3);
					ArrayList<String> timeSlots = new ArrayList<String>();
					for(int i=4; i<function.size(); i++)
						timeSlots.add(function.get(i));
					ArrayList<Boolean> ret = service.deleteRoom(id, room, date, timeSlots);
					String ret_str = "";
					for(Boolean item:ret)
						ret_str = ret_str + " " + item.toString();
					
					message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret_str);
					break;
				}
				case "bookRoom":
				{
					if(function.size() != 6) 
						continue;
					String id = function.get(1);
					String campus = function.get(2);
					String room = function.get(3);
					String date = function.get(4);
					String timeslot = function.get(5);
					String ret = service.bookRoom(id, campus, room, date, timeslot);
					
					message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret);
					if(flag.get() == false)
						message.setReply("Wrong_Answe_Here");
					break;
				}
				case "cancelBook":
				{
					if(function.size() != 3) 
						continue;
					String id = function.get(1);
					String bookingID = function.get(2);
					Boolean ret = service.cancelBook(id, bookingID);
					
					message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret.toString());
					if(flag.get() == false)
						message.setReply("Wrong_Answe_Here");
					break;
				}
				case "getAvailableTimeslot":
				{
					if(function.size() != 3) 
						continue;
					String id = function.get(1);
					String date = function.get(2);
					String ret = service.getAvailableTimeslot(id, date);
					
					message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret);
					if(flag.get() == false)
						message.setReply("Wrong_Answe_Here");
					break;
				}
				case "changeReservation":
				{
					if(function.size() != 6) 
						continue;
					String id = function.get(1);
					String old_bookingID = function.get(2);
					String new_campus_name = function.get(3);
					String new_room_no = function.get(4);
					String new_timeslot = function.get(5);
					String ret = service.changeReservation(id, old_bookingID, new_campus_name, new_room_no, new_timeslot);
					
					message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret);
					if(flag.get() == false)
						message.setReply("Wrong_Answe_Here");
					break;
				}
				case "login":
				{
					if(function.size() != 2) 
						continue;
					String id = function.get(1);
					Boolean ret = service.login(id);
					
					message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret.toString());
					if(flag.get() == false)
						message.setReply("Wrong_Answe_Here");
					break;
				}
			}
			
			storeData(datafile_loc+campus+".ser");
			UDPConnection udp = new UDPConnection(FE_Addr, FE_Port);
			udp.Send(message);
		}
	}
	
	@SuppressWarnings("resource")
	private boolean storeData(String f_name) {
		FileOutputStream fs;
	
		try {
			fs = new FileOutputStream(datafile_loc+"RW_" + campus + ".ser");
			ObjectOutputStream os = new ObjectOutputStream(fs);
			
			os.writeObject(ack_num);
			os.writeObject(holdback);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		if(!service.storeData(f_name))
			return false;
			
			return true;
	}
	
	@SuppressWarnings({ "unchecked", "resource" })
	private boolean loadData(String f_name) {
		try {
			FileInputStream fileStream = new FileInputStream(datafile_loc+"RW_" + campus + ".ser");
			ObjectInputStream is = new ObjectInputStream(fileStream);
			
			ack_num = (AtomicInteger)is.readObject();
			holdback = (PriorityQueue<SeqRequest>)is.readObject();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		if(!service.loadData(f_name))
			return false;
		
		return true; 
	}
	
	/**
	private static void setShutdownHook(String rID, int innerPort, String campus, int outwardPort) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Runtime hook!!!!");
				String cmd = "java -cp " + "./" +  " " + "RoomResrvSys.RequestWorker" + " " + rID + " " + outwardPort
						 + " " + campus + " " + innerPort;	
				System.out.print(cmd);

				Process p = null;
				try {
					p = Runtime.getRuntime().exec(cmd);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	**/
	
	
	public static void main(String []args) throws SocketException {
		if(args.length != 4 && args.length != 5) {
			System.out.println("Format: java ServerRemoteImpl Replica_1 13320 DVL 25560 <fname.ser>");
			return;
		}
		
		String replicaID = args[0];
		int outerPort = Integer.valueOf(args[1]);
		String campus = args[2];
		int innerPort = Integer.valueOf(args[3]);
		
		//setShutdownHook(replicaID, innerPort, campus, outerPort);
		RemoteServerInterface service = new ServerRemoteImpl(campus, innerPort);
		Thread worker = new RequestWorker(service, replicaID, campus);
		if(args.length == 5){
			String fname = args[4];
			if(((RequestWorker) worker).loadData(datafile_loc+fname)) {
				System.out.println("Error: Cannot restore data from " + fname);
			}
		}
		
		worker.start();
		System.out.println(campus + " of " + replicaID + " is running.");
		
		DatagramSocket socket = new DatagramSocket(outerPort);
		while(true) {
			UDPConnection udp = new UDPConnection();
			DatagramPacket packet = udp.ReceivePacket(socket);
			if(packet == null)
				continue;
			
			
			SeqRequest message = new SeqRequest(packet);
			synchronized(holdback) {
				if(message.getSeqNum() <= ack_num.get())
					continue;		// duplicated request
				holdback.add(message);
				holdback.notify();
			}
		}
	}
}
