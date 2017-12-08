package tools;
import java.net.DatagramPacket;

public class MistakeToRM extends Message {
	
	private int seq_num;
	private String campusID;
	private boolean status;

	public MistakeToRM(int seq_num, String campusID, boolean status) {
		this.seq_num = seq_num;
		this.campusID = campusID;
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
		if(parts.length < 2)
			return;
		
		seq_num = Integer.valueOf(parts[0]);
		campusID = parts[1];
		status = Boolean.valueOf(parts[2]);
	}
	
	@Override
	public String pack() {
		String ret = seq_num + " " + campusID + " " + status;
		return ret;
	}
	
	public int getSeq_num() {
		return seq_num;
	}
	
	public String getCampusID() {
		return campusID;
	}
	
	public boolean getStatus() {
		return status;
	}

}