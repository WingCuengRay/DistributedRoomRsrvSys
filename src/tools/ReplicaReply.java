package tools;

import java.net.DatagramPacket;
import java.util.ArrayList;

public class ReplicaReply extends Message {
	private int seq_num;
	private String replicaID;
	private String requestID;
	private String returnVal;
	
	public ReplicaReply(DatagramPacket packet) {
		super(packet.getAddress(), packet.getPort());
		String message = new String(packet.getData(), 0, packet.getLength());
		getValueFromString(message);
	}
	
	public ReplicaReply(String message) {
		getValueFromString(message);
	}
	
	private void getValueFromString(String message) {
		String parts[] = message.split("\\s+");
		if(parts.length < 3)
			return;
		
		seq_num = Integer.valueOf(parts[0]);
		replicaID = parts[1];
		requestID = parts[2];
		returnVal = "";
		for(int i=3; i<parts.length; i++) {
			returnVal = returnVal + parts[i];
			if(i != parts.length-1)
				returnVal += " ";
		}
	}
	
	public ReplicaReply(int seq, String replica_id, String request_id, ArrayList<Boolean> strs){
		seq_num = seq;
		replicaID = replica_id;
		requestID = request_id;
		setReply(strs);
	}
	
	public ReplicaReply(int seq, String replica_id, String request_id, String str){
		seq_num = seq;
		replicaID = replica_id;
		requestID = request_id;
		setReply(str);
	}
	
	public void setReply(String str) {
		returnVal = str;
	}
	public void setReply(ArrayList<Boolean> strs) {
		returnVal = "";
		for(Boolean item:strs) 
			returnVal = returnVal + item.toString() + " ";
	}
	
	@Override
	public String pack() {
		String ret = seq_num + " " + replicaID + " " + requestID + " " + returnVal;

		return ret;
	}
	
	public String getReturnVal() {
		return returnVal;
	}

}
