package tools;

import java.net.DatagramPacket;

public class ResendRequest extends Message {
	private int seq_num;
	private String replicaID;
	
	public ResendRequest(int seq, String replica_id){
		seq_num = seq;
		replicaID = replica_id;
	}
	
	public ResendRequest(String message){
		String parts[] = message.split("\\s+");
		seq_num = Integer.valueOf(parts[0]);
		replicaID = parts[1];
	}
	
	public ResendRequest(DatagramPacket packet){
		super(packet.getAddress(), packet.getPort());
		String message = new String(packet.getData(), 0, packet.getLength());
		String []parts = message.split("\\s+");
		seq_num = Integer.valueOf(parts[0]);
		replicaID = parts[1];
	}
	
	@Override
	public String pack() {
		// TODO Auto-generated method stub
		String ret = seq_num + " " + replicaID;
		
		return ret;
	}
	
	public int getSeqNum(){
		return seq_num;
	}
	
	public String getReplicaID() {
		return replicaID;
	}

}
