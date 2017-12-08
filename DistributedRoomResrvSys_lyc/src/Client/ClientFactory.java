package Client;


public class ClientFactory {
	public static Client createClient(String user_id){
		String identity = user_id.split("(?<=\\D)(?=\\d)")[0];
		if(identity.length()==4 && identity.charAt(3)=='S')
			return new StudentClient();
		else if(identity.length()==4 && identity.charAt(3)=='A')
			return new AdminClient();
		
		return null;
		
	}
}
