package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ReplicaManager {
	private String replicaID;
	private int failure_cnt;
	HashMap<String, Process> runningReplicas = new HashMap<String, Process>();;
	
	static String file_path;
	static final HashMap<String, Integer> outwardPortMap;
	static final HashMap<String, Integer> innerPortMap;
	
	static {
		file_path = new String("./");
		
		outwardPortMap = new HashMap<String, Integer>();
		outwardPortMap.put("DVL", 13320);
		outwardPortMap.put("KKL", 13321);
		outwardPortMap.put("WST", 13322);
		
		innerPortMap = new HashMap<String, Integer>();
		innerPortMap.put("DVL", 25560);
		innerPortMap.put("KKL", 25561);
		innerPortMap.put("WST", 25562);
	}
	 
	
	public static ReplicaManager getReplicaManger() {
		ReplicaManager RM = null;
		
		if(RM == null) {
			RM = new ReplicaManager();
		}
		
		return RM;
	}
	
	private ReplicaManager() {
	}
	
	private ReplicaManager(String rID) {
		this();
		replicaID = rID;
	}
	
	
	private boolean startReplica(String campus_name){
		if(runningReplicas.get(campus_name) != null) 
			return false;

		ProcessBuilder builder = new ProcessBuilder("java", file_path, 
									outwardPortMap.get(campus_name).toString(), campus_name, innerPortMap.get(campus_name).toString());
		String cmd = "java -cp " + file_path +  " " + "RoomResrvSys.RequestWorker" + " " + replicaID + " " +
									outwardPortMap.get(campus_name) + " " + campus_name + " " + innerPortMap.get(campus_name);		
		
		Process p = null;
		try {
			//p = builder.start();
			final String dir = System.getProperty("user.dir");
			p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		runningReplicas.put(campus_name, p);
		
		return true;
	}
	
	private boolean stopReplica(String campus_name) {
		Process p = runningReplicas.get(campus_name);
		if(p == null)
			return true;
		
		p.destroy();
		runningReplicas.remove(campus_name);
		return true;
	}
	
	public boolean recvOpResult() {
		return false;
	}
	
	public void setReplicaID(String rid) {
		replicaID = rid;
	}
	
	
	public static void main(String []args) {
		ReplicaManager RM = ReplicaManager.getReplicaManger();
		RM.setReplicaID("Replica_1");
		
		RM.startReplica("DVL");
		RM.startReplica("KKL");
		RM.startReplica("WST");
		
		while(true) {
			
		}
	}
	
	
}
