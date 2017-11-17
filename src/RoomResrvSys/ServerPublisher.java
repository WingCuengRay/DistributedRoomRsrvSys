package RoomResrvSys;

import java.net.SocketException;

import javax.xml.ws.Endpoint;
public class ServerPublisher {

	public static void main(String[] args) throws SocketException {
		// TODO Auto-generated method stub
		Endpoint endpoint_DVL = Endpoint.publish("http://localhost:10030/RemoteServer", new ServerRemoteImpl("DVL", 25560));
		Endpoint endpoint_KKL = Endpoint.publish("http://localhost:10031/RemoteServer", new ServerRemoteImpl("KKL", 25561));
		Endpoint endpoint_WST = Endpoint.publish("http://localhost:10032/RemoteServer", new ServerRemoteImpl("WST", 25562));
		
		if(endpoint_DVL.isPublished() && endpoint_KKL.isPublished() && endpoint_WST.isPublished())
			System.out.println("true");
	}

}
