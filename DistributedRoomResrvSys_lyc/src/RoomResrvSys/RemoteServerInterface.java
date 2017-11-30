package RoomResrvSys;

import java.util.ArrayList;

public interface RemoteServerInterface 
{
  boolean login (String id);
  ArrayList<Boolean> createRoom (String id, String room, String date, ArrayList<String> timeslots);
  ArrayList<Boolean> deleteRoom (String id, String room, String date, ArrayList<String> timeslots);
  String bookRoom (String stu_id, String campus, String room, String date, String timeslots);
  boolean cancelBook (String stu_id, String bookingID);
  String getAvailableTimeslot (String id, String date);
  String changeReservation (String stu_id, String old_booking_id, String new_campus_name, String new_room_no, String new_timeslot);
  boolean storeData(String f_name);
  boolean loadData(String f_name);
} // interface ServerRemoteOperations
