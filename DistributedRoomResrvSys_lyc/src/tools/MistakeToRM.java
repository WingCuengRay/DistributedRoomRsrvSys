package tools;

import java.net.DatagramPacket;
import java.util.ArrayList;

public class MistakeToRM extends Message {
	
	private int seq_num;
	private boolean status;

	public MistakeToRM(int seq_num, boolean status) {
		this.seq_num = seq_num;
		this.status = status;
		
	}
	
	public MistakeToRM(DatagramPacket packet) {
		
		super(packet.getAddress(), packet.getPort());
		String message = new String(packet.getData(), 0, packet.getLength());
		getValueFromString(message);
		
	}
	
	public MistakeToRM(String message) {
		getValueFromString(message);
	}
	
	private void getValueFromString(String message) {
		
		String parts[] = message.split("\\s+");
		if(parts.length < 1)
			return;
		
		seq_num = Integer.valueOf(parts[0]);
		status = Boolean.valueOf(parts[1]);
	}
	
	@Override
	public String pack() {
		String ret = seq_num + " " + status;
		return ret;
	}
	
	public int getSeq_num() {
		return seq_num;
	}
	
	public boolean getStatus() {
		return status;
	}

}
