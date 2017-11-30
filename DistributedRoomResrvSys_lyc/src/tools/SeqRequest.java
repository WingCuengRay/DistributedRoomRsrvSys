package tools;

import java.net.DatagramPacket;
import java.util.ArrayList;

public class SeqRequest extends Message implements Comparable<SeqRequest> {
	private int seq_num;
	private String requestID;
	private ArrayList<String> function;

	/**
	 *  @name:   SeqRequest
	 *  @description: Constructor of class SeqRequest. The constructor will retrieve the source ip&port 
	 *				  from packet as well
	 *  @param:  packet - DatagramPacket. The incoming packet from sequencer			
	 */
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
	
	/**
	 *  @name:   SeqRequest
	 *  @description: Constructor of class SeqRequest. The constructor will resolve a string and fill the
	 *			      member automcaticlly.
	 *  @param: message - String. A formatted string (content in UDP pakcet) from Sequencer
	 *	@notic: Different from the above constructor, this one will not retrieve the source IP and port.
	 */
	public SeqRequest(String message)
	{
		String []parts = message.split("\\s+");
		seq_num = Integer.valueOf(parts[0]);
		requestID = parts[1];
		
		function = new ArrayList<String>();
		for(int i=2; i<parts.length; i++)
			function.add(parts[i]);
	}

	public SeqRequest(int seq, String reqID, ArrayList<String> func)
	{
		seq_num = seq;
		requestID = reqID;
		function = func;
	}
	
	
	/**
	 *  @name:   pack
	 *  @description: pack the member of SeqRequest into a string which will be sent by UDP.
	 *  @return: string. The formatted string which conforms to the specification			
	 */
	@Override
	public String pack() {
		String ret = String.valueOf(seq_num) + " " + requestID;
		for(int i=0; i<function.size(); i++)
			ret = ret + " " + function.get(i);
		
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
