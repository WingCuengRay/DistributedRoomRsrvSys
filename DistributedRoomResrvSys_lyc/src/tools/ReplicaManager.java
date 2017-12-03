package tools;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class ReplicaManager {
	private class Replica{
		Replica(Process p){
			runningReplica = p;
		}
		public Process runningReplica;
		public int last_failure_seq = -1;
		public int failure_cnt = 0;
	}
	
	private class monitorProcess extends Thread{
		private Replica replica;
		private String campus;
		private String cmd;
		
		monitorProcess(Replica replica, String campus, String cmd){
			this.replica = replica;
			this.cmd = cmd;
			this.campus = campus;
		}
		
		@Override
		public void run() {
			try {
				int ret = replica.runningReplica.waitFor();
				
				if(ret != 0) {
					// Restart replica
					cmd = cmd + " " + campus + ".ser";
					Process p = Runtime.getRuntime().exec(cmd);
					replica.runningReplica = p;
					replica.failure_cnt = 0;
					replica.last_failure_seq = -1;
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	private String replicaID;
	private HashMap<String, Replica> runningReplicas = new HashMap<String, Replica>();

	final static int RM_Port = 13355;
	static ArrayList<String> file_paths;
	static final HashMap<String, Integer> outwardPortMap;
	static final HashMap<String, Integer> innerPortMap;

	
	static {
		// TODO
		file_paths = new ArrayList<String>();
		file_paths.add("./");
		file_paths.add("./");
		file_paths.add("./");
		
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
	
	
	private boolean startReplica(String campus_name, int impl_no){
		if(runningReplicas.get(campus_name) != null) 
			return false;

		String file_path = file_paths.get(impl_no);
		//ProcessBuilder builder = new ProcessBuilder("java", file_path, 
		//							outwardPortMap.get(campus_name).toString(), campus_name, innerPortMap.get(campus_name).toString());
		String cmd = "java -cp " + file_path +  " " + "RoomResrvSys.RequestWorker" + " " + replicaID + " " +
									outwardPortMap.get(campus_name) + " " + campus_name + " " + innerPortMap.get(campus_name);		
		
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		Replica replica = new Replica(p);
		runningReplicas.put(campus_name, replica);
		
		Thread monitor = new monitorProcess(replica, campus_name, cmd);
		monitor.start();
		
		return true;
	}
	
	private boolean stopReplica(String campus_name) {
		Replica replica = runningReplicas.get(campus_name);
		if(replica == null)
			return true;
		
		replica.runningReplica.destroy();
		runningReplicas.remove(campus_name);
		return true;
	}
	
	
	public void UpdateFailureCnt(String campus, int failure_seq) {
		Replica replica = runningReplicas.get(campus);
		if(replica == null)
			return;
		
		if(failure_seq == replica.last_failure_seq+1) {
			replica.failure_cnt++;
		}
		else {
			replica.failure_cnt = 1;
		}
		
		replica.last_failure_seq = failure_seq;
	}
	
	public int getFailureCnt(String campus) {
		Replica replica = runningReplicas.get(campus);
		if(replica == null)
			return 0;
		return replica.failure_cnt;
	}
	
	public boolean recvOpResult() {
		return false;
	}
	
	public void setReplicaID(String rid) {
		replicaID = rid;
	}
	
	
	public static void main(String []args) {
		if(args.length != 1)
		{
			System.out.println("Format: java ReplicaManager Replica_1");
			return;
		}
		ReplicaManager RM = ReplicaManager.getReplicaManger();
		RM.setReplicaID(args[0]);
		
		int impl_no = 0;
		RM.startReplica("DVL", impl_no);
		RM.startReplica("KKL", impl_no);
		RM.startReplica("WST", impl_no);
		
		UDPConnection udp = new UDPConnection();
		DatagramSocket socket;
		try {
			socket = new DatagramSocket(RM_Port);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		System.out.print(args[0] + " is running...");
		while(true) {
			String failure = udp.ReceiveString(socket);
			MistakeToRM failure_msg = new MistakeToRM(failure);
			int failure_seq = failure_msg.getSeq_num();
			String failure_campus = "DVL";			//TODO -- failure_msg.getCampus()
			
			RM.UpdateFailureCnt(failure_campus, failure_seq);
			if(RM.getFailureCnt(failure_campus) == 3) {
				// Software failure
				impl_no = (impl_no+1)%3;
				//Send UDP to replica to make it right;
			}

		}
	}
	
	
}
