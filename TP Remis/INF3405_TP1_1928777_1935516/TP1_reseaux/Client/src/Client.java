import java.io.*;
import java.net.Socket;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class Client {
	private static InputStreamReader input = new InputStreamReader(System.in);
	private static BufferedReader buffer = new BufferedReader(input);
	private static Socket socket;
	static private String username;
	static private String password;
	private static String account = "";

	public static void main(String[] args) throws Exception {
		try {
			// Getting server information
			String serverAddress = null;
			int port = -1;
			do serverAddress = getServerAddress();
			while (!validAddress(serverAddress));
			do port = getPort(); 
			while (!validPort(port));
			
			// Initializing socket
			try {	
				socket = new Socket(serverAddress, port);
			} catch (Exception e) {
				System.out.println("Le serveur est inaccessible: \n" + e);
			}
			Boolean quitting = false;
			do {
				// Getting and verifying user infos
				Boolean validUserInfo = false;
				while (!validUserInfo) validUserInfo = getUsersInfo();
				System.out.println("Profil identifié");
	
				Boolean disconnecting = false;
				do {
					// Getting and verifying image name and availability
					String fileName = null;
					do fileName = askForImageName();
					while (!verifyImageName(fileName));
					File imageToTransfer = new File(fileName);
		
					// Getting and verifying a new name for the filtered image
					Boolean validNewFileName = false;
					String newName;
					do {
						newName = getNewName();
						validNewFileName = verifyNewName(newName);
					} while (!validNewFileName);
		
					transferImageToServer(imageToTransfer, fileName);
					receiveFilteredImage(newName);
					disconnecting = askIfDisconnecting();
				} while (!disconnecting);
				quitting = askIfQuitting();
			} while (!quitting);			
		} catch (IOException e) {
			System.out.println("Erreur lors de l'exécution: " + e.getMessage());
		} finally {
			try {
				socket.close();
				System.out.println("-- Le socket est ferme - Le client se ferme. --");
			} catch (IOException e) {
				System.out.println("Le socket ne peut pas être fermé%n");
			}
		}
	}

	private static int verifyIfQuitting(String answer) throws IOException {
		if (answer.equals("O")) return 0;
		else if (answer.equals("N")) return 1;
		else return 2;
	}
	
	private static Boolean askIfDisconnecting() throws IOException {
		while(true) {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			System.out.print("Voulez-vous vous déconnecter? (O/N)\n");		
			String answer = buffer.readLine();
			if (verifyIfQuitting(answer) == 0) {
				out.writeUTF("disconnecting");
				return true;
			} 
			else if (verifyIfQuitting(answer) == 1) {
				out.writeUTF("not disconnecting");
				return false;
			}
			else {
				System.out.println("Ceci n'est pas un choix valide.\n\"O\" pour oui, \"N\" pour non");
			}
		}
		
	}

	private static Boolean askIfQuitting() throws IOException {
		while(true) {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			System.out.print("Voulez-vous quitter? (O/N)\n");		
			String answer = buffer.readLine();
			if (verifyIfQuitting(answer) == 0) {
				out.writeUTF("quitting");
				return true;
			} 
			else if (verifyIfQuitting(answer) == 1) {
				out.writeUTF("not quitting");
				return false;
			}
			else {
				System.out.println("Ceci n'est pas un choix valide.\n\"O\" pour oui, \"N\" pour non");
			}
		}
		
	}

	private static Boolean getUsersInfo() throws IOException {
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream in = new DataInputStream(socket.getInputStream());

		System.out.print("Saisir votre nom d'utilisateur: ");		
		username = buffer.readLine();
		System.out.print("Saisir votre mot de passe: ");		
		password = buffer.readLine();

		account = username + "," + password;
		out.writeUTF(account);
		String message = in.readUTF();
		System.out.println(message);
		if (!message.contains("Invalid")) {
			return true;
		} else return false;
	}

	private static String getServerAddress() {
		try {
			System.out.print("Entrer l'adresse du serveur: ");
			return buffer.readLine();
		} catch (IOException e) {
			System.out.println("Une erreur est survenu lors de la prise de l'adresse.");
			return null;
		}
	}
	
	// Shamelessly ripped off form https://www.techiedelight.com/validate-ip-address-java/
	private static boolean isValidInet4Address(String ip){
		try {
			return Inet4Address.getByName(ip).getHostAddress().equals(ip);
		}
		catch (UnknownHostException ex) {
			return false;
		}
	}
	
	private static boolean validAddress(String serverAddress){
		// Validate an IPv4 address
		if (isValidInet4Address(serverAddress)) {
			System.out.print("L'adresse IP soumise est valide\n");
		}
		else {
			System.out.print("L'adresse IP soumise n'est pas valide\n");
		}
		return isValidInet4Address(serverAddress);
	}
	
	private static int getPort() {
		try {
			System.out.print("Indiquer le port de connexion: ");
			return Integer.parseInt(buffer.readLine());
		} catch (IOException e) {
			System.out.println("An error has occured\n");
			return -1;
		}
	}

	private static boolean validPort(int port) {
		if (port >= 5000 && port <= 5050) {
			System.out.print("Le port soumis est valide\n");
		}
		else {
			System.out.print("Le port soumis n'est pas valide\n");
		}
		return (port >= 5000 && port <= 5050);
	}

	private static String askForImageName() {
		try {
			System.out.print("Entrer le nom de l'image à filtrer: ");
			return buffer.readLine();
		} catch (IOException e) {
			System.out.println("Erreur lors de la prise du nom d'image\n");
			return null;
		}
	}

	private static Boolean verifyImageName(String imageName) throws IOException {
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		File image = new File(imageName);
		if (!image.exists()) {
			System.out.println("L'image recherchée n'a pas été trouvée.\n");
			out.writeUTF("imageNotFound");
			return false;
		} else {
			System.out.println("L'image recherchée a été trouvée. La transmission au serveur commence.\n");
			out.writeUTF("imageFound");
			return true;
		}
	}

	private static void transferImageToServer(File image, String imageName) throws IOException {
		byte[] imageBytes = new byte[(int)image.length()];
		FileInputStream fileInputStream = new FileInputStream(image);
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		fileInputStream.read(imageBytes);
		output.writeUTF(imageName);
		output.writeInt((int)image.length());
		output.flush();
		output.write(imageBytes, 0, imageBytes.length);
		output.flush();
		fileInputStream.close();
	}

	private static boolean receiveFilteredImage(String filteredImageName) throws IOException{
		DataInputStream input = new DataInputStream(socket.getInputStream());
		File newImage = new File(filteredImageName);		
		FileOutputStream fileOutputStream = new FileOutputStream(newImage);

		int imageBytesLength = input.readInt();
		byte[] imageBytes = new byte[imageBytesLength];

		input.readFully(imageBytes);
		fileOutputStream.write(imageBytes, 0, imageBytes.length);
		fileOutputStream.close();
		
		System.out.println("Succes, nouvelle image recue du server a l'adresse: " + newImage.getAbsolutePath());
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		output.writeUTF("imageReceivedSToC");
		return true;
	}

	private static Boolean verifyNewName(String nameToVerify) throws IOException {
		String[] supportedFormats = new String[] {"jpg", "png", "gif"};
		if (!nameToVerify.contains(".")) {
			System.out.print("Aucune extension n'a été indiqué. Veuiller recommencer en ajoutant un extension");	
			return false;
		}
		String imageType = nameToVerify.split("\\.")[1];
		System.out.println(imageType);
		for (int i = 0; i < 3; i++) {
			if (imageType.equals(supportedFormats[i])) {
				System.out.println("Le nom soumis est d'un format supporté");
				return true;
			}
		}
		System.out.print("L'extension soumis n'est pas supporté. Veuiller recommencer en ajoutant un extension supporté");	
		return false;
	}

	private static String getNewName() throws IOException {
		BufferedReader buffer = new BufferedReader(input);
		System.out.print("Les extensions supportés sont .jpg / .png / .gif\n");	
		System.out.print("Indiquer le nouveau nom de votre image sobelisé: ");		
		String newImageName = buffer.readLine();
		return newImageName;
	}
}