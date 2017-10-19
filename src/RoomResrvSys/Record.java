package RoomResrvSys;

import java.text.DecimalFormat;

public class Record {
	private String timeSlot;
	private boolean occupied;
	private String recordId;
	private String bookerId;
	
	public Record(String time, int id){
		timeSlot = time;
		recordId = new String("RR"+new DecimalFormat("0000").format(id));
		occupied = false;
		bookerId = null;
	}
	
	protected String getTimeSlot(){
		return timeSlot;
	}
	
	protected String getRecordID(){
		return recordId;
	}
	
	protected String getBookerID(){
		return bookerId;
	}
	
	public boolean isOccupied(){
		return occupied;
	}
	
	public void SetBookerID(String stu_id){
		bookerId = stu_id;
	}
	
	public void setOccupied(boolean b){
		occupied = b;
	}
}
