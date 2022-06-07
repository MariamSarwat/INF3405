import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.io.*;

public class Server {
	private static ServerSocket listener;
	private static InputStreamReader input = new InputStreamReader(System.in);
	private static BufferedReader buffer = new BufferedReader(input);
	public static void main(String[] args) throws Exception {
		String serverAddress = null;
		int port = -1;

		do {
			serverAddress = getServerAddress();
		} while (!validAddress(serverAddress));

		do {
			port = getPort();
		} while (!validPort(port));
		
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIp = InetAddress.getByName(serverAddress);
		listener.bind(new InetSocketAddress(serverIp, port));
		System.out.format("Curently running on %s: %d %n", serverAddress, port);
		
		try {
			while(true) new ClientHandler(listener.accept()).start();
		} catch (IOException e){
			System.out.println("An error has occured\n");
		}
		finally {
			System.out.println("Closing sockets.");
			listener.close();
		}
	}
	
	private static String getServerAddress() {
		try {
			System.out.print("Enter the server's address: ");
			return buffer.readLine();
		} catch (IOException e) {
			System.out.println("An error has occured\n");
			return null;
		}
	}
	
	private static int getPort() {
		try {
			System.out.print("Enter the server's port: ");
			return Integer.parseInt(buffer.readLine());
		} catch (IOException e) {
			System.out.println("An error has occured\n");
			return -1;
		}
	}

	// From https://www.techiedelight.com/validate-ip-address-java/
	private static boolean isValidInet4Address(String ip){
		try {
			return Inet4Address.getByName(ip).getHostAddress().equals(ip);
		}
		catch (UnknownHostException ex) {
			return false;
		}
	}
	
	// From https://www.techiedelight.com/validate-ip-address-java/
	private static boolean validAddress(String serverAddress){
		// Validate an IPv4 address
		if (isValidInet4Address(serverAddress)) {
			System.out.print("The submitted IP address is valid\n");
		}
		else {
			System.out.print("The submitted IP address isn't valid\n");
		}
		return isValidInet4Address(serverAddress);
	}

	private static boolean validPort(int port) {
		if (port >= 5000 && port <= 5050) {
			System.out.print("The submitted port is valid\n");
		}
		else {
			System.out.print("The submitted port isn't valid\n");
		}
		return (port >= 5000 && port <= 5050);
	}
}
