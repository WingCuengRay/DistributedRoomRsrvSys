package RoomResrvSys;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

import tools.ReplicaReply;
import tools.SeqRequest;
import tools.UDPConnection;

public class RequestWorker extends Thread {
	private RemoteServerInterface service;
	private String replicaID;
	private static AtomicInteger ack_num;
	private static PriorityQueue<SeqRequest> holdback;
	private static String FE_Addr;
	private static int FE_Port;
	
	static {
		ack_num = new AtomicInteger(0);
		holdback = new PriorityQueue<SeqRequest>(50);
		
		//TODO
		FE_Addr = "192.168.1.6";
		FE_Port = 4455;
	}
	
	public RequestWorker(RemoteServerInterface srv, String r_id) {
		service = srv;
		replicaID = r_id;
	}
	
	@Override
	public void run() {
		while(true) {
			SeqRequest request = holdback.peek();
			synchronized(holdback) {
				if(request == null || request.getSeqNum() != ack_num.get()+1) {
					try {
						holdback.wait();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}	
					continue;
				}
				
				// Prevent multiple threads from accessing holdback queue simultaneously
				ack_num.incrementAndGet();
				holdback.remove();
			}
			
			
			ArrayList<String> function = request.getFunction();
			String name = function.get(0);
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
					ArrayList<String> ret = service.createRoom(id, room, date, timeSlots);
					
					ReplicaReply message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret);
					UDPConnection udp = new UDPConnection(FE_Addr, FE_Port);
					udp.Send(message);
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
					
					ReplicaReply message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret_str);
					UDPConnection udp = new UDPConnection(FE_Addr, FE_Port);
					udp.Send(message);
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
					
					ReplicaReply message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret);
					UDPConnection udp = new UDPConnection(FE_Addr, FE_Port);
					udp.Send(message);
					break;
				}
				case "cancelBook":
				{
					if(function.size() != 3) 
						continue;
					String id = function.get(1);
					String bookingID = function.get(2);
					Boolean ret = service.cancelBook(id, bookingID);
					
					ReplicaReply message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret.toString());
					UDPConnection udp = new UDPConnection(FE_Addr, FE_Port);
					udp.Send(message);
					break;
				}
				case "getAvailableTimeslot":
				{
					if(function.size() != 3) 
						continue;
					String id = function.get(1);
					String date = function.get(2);
					String ret = service.getAvailableTimeslot(id, date);
					
					ReplicaReply message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret);
					UDPConnection udp = new UDPConnection(FE_Addr, FE_Port);
					udp.Send(message);
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
					
					ReplicaReply message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret);
					UDPConnection udp = new UDPConnection(FE_Addr, FE_Port);
					udp.Send(message);
					break;
				}
				case "login":
				{
					if(function.size() != 2) 
						continue;
					String id = function.get(1);
					Boolean ret = service.login(id);
					
					ReplicaReply message = new ReplicaReply(request.getSeqNum(), replicaID, request.getRequestID(), ret.toString());
					UDPConnection udp = new UDPConnection(FE_Addr, FE_Port);
					udp.Send(message);
					break;
				}
			}
		}
	}
	
	
	public static void main(String []args) throws SocketException {
		if(args.length != 4) {
			System.out.println("Format: java ServerRemoteImpl Replica_1 13320 DVL 25560");
			return;
		}
		
		String replicaID = args[0];
		int outerPort = Integer.valueOf(args[1]);
		String campus = args[2];
		int innerPort = Integer.valueOf(args[3]);
		
		RemoteServerInterface service = new ServerRemoteImpl(campus, innerPort);
		Thread worker = new RequestWorker(service, replicaID);
		worker.start();
		
		DatagramSocket socket = new DatagramSocket(outerPort);
		while(true) {
			UDPConnection udp = new UDPConnection();
			DatagramPacket packet = udp.ReceivePacket(socket);
			if(packet == null)
				continue;
			
			SeqRequest message = new SeqRequest(packet);
			synchronized(holdback) {
				holdback.add(message);
				holdback.notify();
			}
		}
	}
}
