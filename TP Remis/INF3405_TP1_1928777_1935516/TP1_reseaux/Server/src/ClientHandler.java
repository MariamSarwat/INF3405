import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class ClientHandler extends Thread{
	private Socket socket;
	
	static private DataOutputStream output;
	static private DataInputStream input;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		try {
			output = new DataOutputStream(socket.getOutputStream());
			input = new DataInputStream(socket.getInputStream());
			Boolean quitting = false;
			do {
				Boolean validUserInfo = false;
				while (!validUserInfo) validUserInfo = connectUser();
				Boolean disconnecting = false;
				do {
					Boolean imageReceived = false;
					do imageReceived = isImageReceived();
					while (!imageReceived);
					while(!imageSavedOnClient());
					if (input.readUTF().equals("disconnecting")) disconnecting = true;
				} while (!disconnecting);
				if (input.readUTF().equals("quitting")) quitting = true;
			} while (!quitting);
		}
		catch (IOException e) {
			System.out.println("Can't reach the ClientHandler");
		}
		finally {
			try {
				socket.close();
				System.out.println("-- Socket's closed - ClientHandler is shutting down --");
			} catch (IOException e) {
				System.out.println("Can't close the socket%n");
			}
		}
	}
	
	private boolean imageSavedOnClient() throws IOException{
		String answer = input.readUTF();
		if (answer.equals("imageReceivedSToC")) {
			return true;
		} 
		return false;
	}

	private boolean isImageReceived() throws IOException{
		String answer = input.readUTF();
		if (answer.equals("imageFound")) {
			System.out.println("Image to process received from client");
			receiveImage();
			return true;
		} else {
			System.out.println("File to process is not valid, waiting for the next proposition");
			return false;
		}
	}
	
	private Boolean connectUser() throws IOException{
		String accountInfo = input.readUTF();
		if(findUser("users.txt", accountInfo)){
			output.writeUTF("Connexion réussie! \n" + "Bonjour " + accountInfo.split(",")[0] + "!");
			System.out.println(accountInfo.split(",")[0] + " is connected");
			return true;
		}
		else {
			output.writeUTF("Invalid");	
			System.out.println("Incorrect password");
			return false;
		}
	}

	private boolean findUser(String fileName, String account) throws IOException{
		boolean newUser = true;
    	File file = new File(fileName);

		if(file.exists()){			
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()){
				String currentUser = scanner.nextLine();
				if(currentUser.split(",")[0].equals(account.split(",")[0])){		
					newUser = false;
					return (currentUser.split(",")[1].equals(account.split(",")[1]));
				}
			}
			scanner.close();
			
			if(newUser) {
				writeInFile(fileName, account);
				return true;
			}
		} else {
			writeInFile(fileName, account);
			return true;
		}
    	return false;
	}

	private static void writeInFile(String fileName, String str) throws IOException{  
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
	    writer.append(str + "\n");
	    writer.close();
	}

	private void receiveImage() throws IOException {
		String imageName = input.readUTF();
		File image = new File("lastImageFiltered.jpg");		
		FileOutputStream fileOutputStream = new FileOutputStream(image);

		int imageBytesLength = input.readInt();
		byte[] imageBytes = new byte[imageBytesLength];
		input.readFully(imageBytes);
		fileOutputStream.write(imageBytes, 0, imageBytes.length);
		fileOutputStream.close();
		System.out.println("finished receiving image");

		File filteredImage = new File("lastImageFiltered.jpg");
		BufferedImage buffy = ImageIO.read(image);
		buffy = Sobel.process(buffy);
		sendFilteredImage(filteredImage, buffy );
		
	}

	private void sendFilteredImage(File filteredImage, BufferedImage buffy) throws IOException {
		ImageIO.write(buffy, "jpg", filteredImage);
		byte[] imageBytes = new byte[(int)filteredImage.length()];
		FileInputStream fileInputStream = new FileInputStream(filteredImage);

		fileInputStream.read(imageBytes);
		fileInputStream.close();
		output.writeInt((int)filteredImage.length());
		output.flush();

		output.write(imageBytes, 0, imageBytes.length);
		output.flush();
		System.out.println("finished sending image");
	}
}