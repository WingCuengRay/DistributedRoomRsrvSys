package tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class LogItem {
	
	private static HashMap<RequestType, String> requestMap;
	private Date time;
	private String user_id;
	private RequestType type;
	private String[] args;
	private boolean isSuccess = false;
	private String response;
	
	static {
		requestMap = new HashMap<RequestType, String>();
		requestMap.put(RequestType.Book, "Book");
		requestMap.put(RequestType.CancelBook, "CancelBook");
		requestMap.put(RequestType.AddRecord, "AddRecord");
		requestMap.put(RequestType.DeleteRecord, "DeleteRecord");
		requestMap.put(RequestType.GetAvailTimeSlot, "GetAvailTimeSlot");
		requestMap.put(RequestType.ChangeReservation, "ChangeReservation");
	}
	
	
	public LogItem(RequestType t, String uid, String[] arguments) {
		time = new Date();
		user_id = uid;
		args = arguments;
		type = t;
	}
	
	
	public void setResult(boolean result) {
		isSuccess = result;
	}
	
	public void setResponse(String r) {
		response = r;
	}
	
	public String format() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s_date = dateFormat.format(time);
		String s_type = requestMap.get(type);
		String s_args = new String();
		for(String item:args) {
			s_args = s_args + " " + item;
		}
		
		String s_result;
		if(isSuccess == true)
			s_result = new String("Success");
		else
			s_result = new String("Failure");
		
		String s_respnose;
		if(response == null)
			s_respnose = new String();
		else
			s_respnose = response;
		
		
		return s_date+'\t'+ user_id + '\t' +s_type+'\t'+s_args+'\t'+s_result+"\t "+s_respnose + '\n';
	}

}
