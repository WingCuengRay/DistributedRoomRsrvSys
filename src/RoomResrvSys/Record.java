package RoomResrvSys;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

public class Record implements Serializable{
	private String timeSlot;
	private boolean occupied;
	private String recordId;
	private String bookerId;
	private Date date;
	
	public Record(String time, int id, Date d){
		timeSlot = time;
		recordId = new String("RR"+new DecimalFormat("0000").format(id));
		date = d;
		occupied = false;
		bookerId = null;
		
	}
	
	protected Date getDate() {
		return date;
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
	
	public void setDate(Date d) {
		date = d;
	}
}
