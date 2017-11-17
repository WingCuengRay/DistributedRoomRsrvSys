package tools;

import java.net.DatagramPacket;
import java.util.ArrayList;

public class SeqRequest extends Message implements Comparable<SeqRequest> {
	private int seq_num;
	private String requestID;
	private ArrayList<String> function;
	
	public SeqRequest(DatagramPacket packet) {
		super(packet.getAddress(), packet.getPort());
		String message = new String(packet.getData(), 0, packet.getLength());
		String []parts = message.split("\\s+");
		seq_num = Integer.valueOf(parts[0]);
		requestID = parts[1];
		
		function = new ArrayList<String>();
		for(int i=2; i<parts.length; i++)
			function.add(parts[i]);
	}
	
	public SeqRequest(String message)
	{
		String []parts = message.split("\\s+");
		seq_num = Integer.valueOf(parts[0]);
		requestID = parts[1];
		for(int i=2; i<parts.length; i++)
			function.add(parts[i]);
	}
	
	
	@Override
	public String pack() {
		String ret = String.valueOf(seq_num) + " " + requestID;
		for(int i=0; i<function.size(); i++)
			ret = " " + function.get(i);
		
		return ret;
	}
	
	public int getSeqNum() {
		return seq_num;
	}
	
	public String getRequestID() {
		return requestID;
	}
	
	public ArrayList<String> getFunction(){
		return function;
	}


	@Override
	public int compareTo(SeqRequest other) {
		if(seq_num < other.seq_num)
			return -1;
		else if(seq_num > other.seq_num)
			return 1;
		else
			return 0;
	}

}
