package RoomResrvSys;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style=Style.RPC)
public interface RemoteServerInterface 
{
  boolean login (String id);
  String[] createRoom (String id, String room, String date, String[] timeslots);
  boolean[] deleteRoom (String id, String room, String date, String[] timeslots);
  String bookRoom (String stu_id, String campus, String room, String date, String timeslots);
  boolean cancelBook (String stu_id, String bookingID);
  String getAvailableTimeslot (String id, String date);
  String changeReservation (String stu_id, String old_booking_id, String new_campus_name, String new_room_no, String new_timeslot);
} // interface ServerRemoteOperations
