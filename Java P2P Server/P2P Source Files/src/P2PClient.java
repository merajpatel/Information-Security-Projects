/**********************************************************
 * Assignment:P2P Server - P2PClient.java
 * Author: Meraj Patel 
 * Student Number: 3248644
 * 
 * Description: P2PClient is responsible for serving client request in conjunction with P2PServer. This class allows
 * clients to upload,delete and download files from P2P server. It also serves other clients in term of download requests.
 * 
 * How to use: Execute to the program with proper parameters: 
 * 			eg: java -jar client.jar -ORBInitialPort 1050 -ORBInitialHost localhost
 * 			Afterwards, following instructions appropriately to upload, delete or download files from P2P server.
 * 
 * Honor Code: I pledge that this program represents my own program code.
 *********************************************************/


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Iterator;

import javax.imageio.IIOException;

import org.omg.CORBA_2_3.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CORBA.*;

import P2P.ClientRequest;
import P2P.ClientRequestHelper;

/*
 * Class Name: P2PClient
 * 
 * Description: class used to manage client request to P2P server
 */
class P2PClient {

	private static ClientRequest request;//client request fd
	private static  String hostIP;//save host ip
	private static String hostPort;//save host port number
	static ServerSocket clientFileUpload;//socket for client file upload
	ArrayList <String> uploadedFiles = new ArrayList<String>();//list to keep track of uploaded files

	/*
	 * Function Name: P2PClient
	 * Description: P2PClient class constructor. 
	 * Responsible for creating server on hostip and host port number 
	 * 
	 * @param: none
	 * @return: void
	 * 
	 * input - host ip and host port
	 * output - none
	 * 
	 * 
	 * "Called by whom":
	 * 		main - to initialize server object  
	 */
	public P2PClient() { 
		try {//try to instantiate socket connection to SimpleProxyServer
			clientFileUpload = new ServerSocket(Integer.parseInt(hostPort));//instantiate server socket
		}
		catch (IOException e) {//output message indicating SimpleProxyServer is not online
			System.out.println("Failed to connect to port " +hostPort );
			System.exit((1));
		}

	}

	/*
	 * Function Name: main
	 * Description: Responsible for instantiating orb to allow client request to server. provides client with instructions on use
	 * of program. Listens for client instrucitons via switch case
	 * 
	 * @param: argv:  -ORBInitialPort 1050
	 * 				  -ORBInitialHost localhost
	 * @return: void
	 * 
	 *  user input: host port
	 * 				userChoice
	 * Expected output: Instructions on use
	 * 
	 * "Called by whom":
	 * 		init 
	 * 
	 * "Will call what" 
	 * 		uploadFile - Responsible for uploading client files to server (file name, host ip, host port)
	 * 		viewUploadedFiles - allows client to view all files uploaded on server
	 *      removeFile - allows client to remove file on server
	 *      searchFile - allows client to search for file on server
	 *      viewAllFilesServer - allows client to view all files availble to download on server
	 *      downloadFile - allows client to download file on server
	 */
	public static void main(String argv[]) {
		try {
			// Create an ORB
			ORB orb = (ORB) ORB.init(argv, null);//instantiate orb
			org.omg.CORBA.Object objRef =   orb.resolve_initial_references("NameService");//resolve object reference to NameService
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			request = (ClientRequest) ClientRequestHelper.narrow(ncRef.resolve_str("P2PServer"));//assign client request

			InetAddress address = InetAddress.getLocalHost();//get current ip address 
			hostIP = address.getHostAddress() ;//assign up
			int portNumber;
			Scanner inputReader = new Scanner(System.in);//create new scanner object for user input
			//output instructions
			System.out.println("-----------------------------------------------------------------------------------------------");
			System.out.println("Welcome Client");
			System.out.println("This program is intended to intiate client request for uploading or downloading files on P2P server");
			System.out.println();
			System.out.println("Please enter a port number to bind too:");
			while(true){
				try{
					portNumber = inputReader.nextInt();//get port number
					if (portNumber < 65535 && portNumber > 0){//make sure port number is in range
						break;
					}
					else{//else output message indicating invalid port number and exit
						System.out.println("Invalid port number");
						System.exit(1);
					}
				}//catch exception if client does not enter an integer
				catch (Exception e){
					System.out.println("Invalid port number");
					System.exit(1);
				}
			}


			hostPort = Integer.toString(portNumber);//assign port number
			String userChoice = "";//instantiate userChoice variable
			outputInstructions();//output instructions
			P2PClient uploadServer = new P2PClient();	


			new Thread() {//start listening thread for download request
				public void run() {
					listen();//start listening
				}  
			}.start();//start thread
			
			Runtime.getRuntime().addShutdownHook(new Thread() {//method for CTRL-C action
			    public void run() { 
			    	clearUploads();//clear client uploads if CTRL-C is inputted
			    }
			 });

			do{
				try {//try statement used for gather user input
					System.out.println("Please input option number (Enter 7 for instructions)");
					userChoice = inputReader.next();//read input
					switch(userChoice){//switch case for instructions
					case "0": 
						break;
					case "1": uploadServer.uploadFile(); System.out.println();System.out.println("-----------------------------------------------------------------------------------------------");
					break;
					case "2": uploadServer.viewUploadedFiles(); System.out.println();System.out.println("-----------------------------------------------------------------------------------------------");
					break;
					case "3": uploadServer.removeFile(); System.out.println();System.out.println("-----------------------------------------------------------------------------------------------");
					break;
					case "4": uploadServer.searchFile(); System.out.println();System.out.println("-----------------------------------------------------------------------------------------------");
					break;
					case "5": uploadServer.viewAllFilesServer(); System.out.println();System.out.println("-----------------------------------------------------------------------------------------------");
					break;
					case "6":  uploadServer.downloadFile(); System.out.println();System.out.println("-----------------------------------------------------------------------------------------------");
					break;
					case "7": System.out.println();outputInstructions();
					break;
					default: 
						System.out.println();
						System.out.println("Invalid option, please try again");
						System.out.println();
						System.out.println("-----------------------------------------------------------------------------------------------");
					}


				}
				catch (NumberFormatException e) {//generate exception if faulty input
					System.out.println("Usage: java Simple Client [url]");
					uploadServer.shutDownServer();//shutdown 
					System.exit(1);
				}
			}while(!userChoice.equals("0"));
			uploadServer.clearUploads();//clear all client uploads on server
			System.out.println("Bye!");
			uploadServer.shutDownServer();//shutdown server sockets
			System.exit(1);

		}//catch exception
		catch (Exception e) {
			System.out.println(e) ;
			e.printStackTrace(System.out);
		}
	}

	/*
	 * Function Name: main
	 * Description: outputs instructions on use of program. Called by main function
	 * 
	 * @param: void
	 * @return: void
	 * 
	 * input: none
	 * Expected output: Instructions on use
	 * 
	 * "Called by whom":
	 * 		main 
	 * 
	 */
	public static void outputInstructions(){
		System.out.println("-----------------------------------------------------------------------------------------------");
		System.out.println("Please select one of the following options by inputing its number");
		System.out.println("1. Upload File");
		System.out.println("2. View your Uploaded Files");
		System.out.println("3. Remove your File");
		System.out.println("4. Search File Availble to Download");
		System.out.println("5. View All Files Availble to Download");
		System.out.println("6. Download File");
		System.out.println("To exit, please type 0");
		System.out.println();
	}

	/*
	 * Function Name: viewAllFilesServer
	 * Description: outputs all files available to download on server
	 * 
	 * @param: void
	 * @return: void
	 * 
	 * input: none
	 * Expected output: outputs all files available to download on server
	 * 
	 * "Called by whom":
	 * 		main 
	 * 
	 */
	public void viewAllFilesServer(){
		System.out.println(request.viewAllFiles());//insiate request to view files on server
	}
	
	/*
	 * Function Name: searchFile
	 * Description: searches file on server. outputs result
	 * 
	 * @param: void
	 * @return: void
	 * 
	 * input: user input - file name to search 
	 * Expected output: result indicating if file is on server
	 * 
	 * "Called by whom":
	 * 		main 
	 * 
	 */
	public void searchFile(){
		String fileName;//file name to be searched
		System.out.println();
		System.out.println("Please enter file name to check on server");//instructions
		Scanner inputReader = new Scanner(System.in);//create new scanner object for file check
		fileName = inputReader.nextLine();//read input
		System.out.println();
		System.out.println(request.searchFile(fileName));//output file check result
	}

	/*
	 * Function Name: uploadFile
	 * Description: searches file on server. outputs result
	 * 
	 * @param: void
	 * @return: void
	 * 
	 * input: user input - file name to upload
	 * Expected output: result indicating if file successfully uploaded or error.
	 * 
	 * "Called by whom":
	 * 		main 
	 * "Will call what" 
	 * 		checkForFile - checks if file exists and is in current directory
	 */
	public void uploadFile(){
		try{
			String fileName;//file name to upload
			String result;//result of upload
			boolean check = false;//check to see if file to upload is in directory
			System.out.println();
			System.out.println("Please enter file name");
			Scanner inputReader = new Scanner(System.in);//create new scanner object for user input
			fileName = inputReader.nextLine();//read input
			check = checkForFile(fileName);
			if(check == true){//check if file exists in current directory
				result = request.addFile(fileName,hostIP, hostPort);//attempt to add file
				if (result.equals("Filename already used. please use different name")){//check if file is already on server
					System.out.println();
					System.out.println(result);//output add file results
					System.out.println();
				}
				else{
					System.out.println();
					System.out.println(result);//output add file results 
					uploadedFiles.add(fileName);//add to uploadedFiles list
				}
			}
			else {
				System.out.println();
				System.out.println("Cannot find file, please try again");//output error message 
			}
		}catch (Exception e){//catch exception if server is down
			System.out.println();
			System.out.println("Server Down");
		}
	}

	/*
	 * Function Name: viewUploadedFiles
	 * Description: view all client uploaded files
	 * 
	 * @param: void
	 * @return: void
	 * 
	 * input: none
	 * Expected output: uploaded file names
	 * 
	 * "Called by whom":
	 * 		main 
	 * 
	 */
	public void viewUploadedFiles(){
		if (uploadedFiles.size() == 0){//check if there are no files uploaded from client
			System.out.println();
			System.out.println("No files uploaded");
		}
		else{
			System.out.println("Uploaded files:");
			for (String name : uploadedFiles) {//loop through uploaded files array 
				System.out.println();
				System.out.println(name);//display uploaded files
			}  
		}
	}

	/*
	 * Function Name: viewUploadedFiles
	 * Description: view all client uploaded files
	 * 
	 * @param: String fileName - file name to check if exists in directory
	 * @return: boolean indicating if file exists
	 * 
	 * input: none
	 * Expected output: boolean indicating if file exists
	 * 
	 * "Called by whom":
	 * 		uploadFile 
	 * 
	 */
	public boolean checkForFile(String fileName){
		File varTmpDir = new File(fileName);//instantiate file variable
		boolean exists = varTmpDir.exists();//check if file exists in current directory 
		return exists;
	}

	/*
	 * Function Name: removeFile
	 * Description: view all client uploaded files
	 * 
	 * @param: String fileName - file name to check if exists in directory
	 * @return: boolean indicating if file exists
	 * 
	 * input: none
	 * Expected output: boolean indicating if file exists
	 * 
	 * "Called by whom":
	 * 		main 
	 * 
	 */
	public void removeFile(){
		String fileName;
		System.out.println();
		System.out.println("Please enter file name to remove on server");
		Scanner inputReader = new Scanner(System.in);//create new scanner object for user input
		fileName = inputReader.nextLine();//read input
		boolean success = false;

		for( Iterator<String> it = uploadedFiles.iterator(); it.hasNext();)//iterate through uploaded files list
		{
			String name = it.next();//get name of current file in list
			if (name.equals(fileName)){//check if file to remove name matches
				success = true;
				it.remove();//remove file from list
				System.out.println();
				System.out.println(request.removeFile(fileName, hostIP, hostPort));//output results from removing file from server
			}
		}  
		if (success == false){//check if file name to remove is not contained with in list
			System.out.println();
			System.out.println("Improper file name. Please try again");//output error message
		}

	}
	
	/*
	 * Function Name: downloadFile
	 * Description: allows client to download file seen on server from host given host IP and port
	 * 
	 * @param: none
	 * @return: none
	 * 
	 * input: user input - filename to download. 
	 * Expected output: result indicating if file was successfully download or if an error has occurred
	 * 
	 * "Called by whom":
	 * 		main 
	 * 
	 */
	public void downloadFile(){
		String fileName;//filename to download
		System.out.println();
		System.out.println("Please enter file name");
		Scanner inputReader = new Scanner(System.in);//create new scanner object for user input
		fileName = inputReader.nextLine();//read input
		String fileCheck = request.searchFile(fileName);//search for file on server
		if (fileCheck.equals("File found")){//execute if file is found
			String[] fileResults = (request.downloadFile(fileName)).split(",");//split results into IP and Port
			String clientIP = fileResults[0];//save ip to download from
			String clientPort = fileResults[1];//save port to download from
			if (clientIP.equals("Error querying file")){//check if download request contains error
				System.out.println();
				System.out.println("File not found on server");//output message saying file not found
			}
			else{
				try {//try to create reader, writer objects. Proceed to read and write 
					Socket serverConn = new Socket(clientIP, Integer.parseInt(clientPort));
					BufferedReader in = new BufferedReader (new InputStreamReader (serverConn.getInputStream()));//create object to read form serverConn
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(serverConn.getOutputStream()));//create object to write to serverConn
					String output;//string contains socket output
					try {//try to write
						//fileName = fileName + '\n';
						out.write(fileName+ '\n');//write file name
						out.flush();//flush
					}
					catch(IIOException e) {//catch error if unable to write
						System.out.println("Connection Refused");
						System.exit(1);
					}
					StringBuffer buffer = new StringBuffer();//create buffer to store  output
					String inputLine;//string to read  output

					while ((inputLine = in.readLine()) != null)//keep reading until reaches end of file
					{
						buffer.append(inputLine + "\n");//save contents of file being read
					}
					output = buffer.toString();//assign buffer to output
					System.out.println();
					System.out.println("Downloaded file: " + fileName);//output file that was downloaded
					BufferedWriter writer = new BufferedWriter(new FileWriter(String.valueOf(fileName),true));//create new file witihn direcotroy give filename to download
					writer.write(output);//print contnet of file read to new file created
					writer.close();//close write
					in.close();//close reader
					out.close();//close writer
				} catch (IOException e) {//throw exception if unable to create objects or error occurs in read/write
					System.out.println("Connection Refused");
				}
			}
		}
		else{
			System.out.println();
			System.out.println("File not availble to download");
		}
	}

	/*
	 * Function Name: clearUplaods
	 * Description: clear all client files uploaded on server
	 *
	 * @param: none
	 * @return: none
	 * 
	 * input: none
	 * Expected output: none
	 * 
	 * "Called by whom":
	 * 		main 
	 * 
	 */
	public static void clearUploads(){
		request.clearUploads(hostIP, hostPort);//clear all uploaded files on server
	}

	/*
	 * Function Name: listen
	 * Description: listens for incoming client connection request for download
	 *
	 * @param: none
	 * @return: none
	 * 
	 * input: none
	 * Expected output: none
	 * 
	 * "Called by whom":
	 * 		main 
	 * 
	 */
	public static void listen(){
		while(true){
			try{//try creating new thread
				new ClientThread(clientFileUpload.accept()).start();//create new thread, and handle client request
			}catch (IOException e){}//catch error indicating socket is closed
		}	
	}
	
	/*
	 * Function Name: shutDownServer
	 * Description: shutdown server socket
	 *
	 * @param: none
	 * @return: none
	 * 
	 * input: none
	 * Expected output: none
	 * 
	 * "Called by whom":
	 * 		main 
	 * 
	 */
	public void shutDownServer(){
		try {//try to close server object
			clientFileUpload.close();
		} catch (IOException e) {//catch and print exception if failed
			e.printStackTrace();
		}
	}

}

/*
 * Class Name: ClientThread
 * 
 * Description: class used to server cient request for download
 */
class ClientThread extends Thread {
	private Socket clientConn;

	/*
	 * Function Name: ClientThread
	 * Description: ClientThread class constructor. 
	 * Responsible for assigning socket connection to class variable
	 * 
	 * @param: Socket socket - socket to communicate with client
	 * @return: void
	 * 
	 */
	ClientThread(Socket socket){
		this.clientConn = socket;
	}

	/*
	 * Function Name: run
	 * Description: Responsible for reading and server request file to download
	 * 
	 * @param: none
	 * @return: void
	 * 
	 * input: none
	 * Expected output: write file content to client socket 
	 * 					read file name from client socket
	 * 
	 * "Called by whom":
	 * 		listen 
	 * 
	 * "Will call what" 
	 * 		readFile - Responsible for reading file to write
	 * 
	 */
	public void run(){
		String fileName = "";//Variable to store url 
		String fileContent = "";//store url content

		try{//try creating read and write object for client
			BufferedReader reader = new BufferedReader(new InputStreamReader(clientConn.getInputStream()));//create object to read from client
			OutputStream out = clientConn.getOutputStream();//create object to write to client

			try{//Try to read/write from/to client
				StringBuffer buffer = new StringBuffer();
				while (true) {//loop used to read individual char from client socket, and appent to buffer to unify request.
					int ch = reader.read();
					if ((ch < 0) || (ch == '\n')) {//break if new line is found
						break;
					}
					buffer.append((char) ch);//append to buffer
				}

				fileName = buffer.toString();//assign url read from socket to local variable "url" 
				fileContent = readFile(fileName);
				if (fileContent != null){
					out.write(fileContent.getBytes());//write url content to client
					out.flush();
				}
			}
			catch (IOException e){//output to client invalid url if exception is caught
				fileContent = "Invalid File Name";
				out.write(fileContent.getBytes());//write "Invalid URL" to client
				out.flush();
			}
			reader.close();//close read object
			out.close();//close write object
			clientConn.close();//close server object
		}
		catch (IOException e){//catch error if failed to create read/write object
			System.exit(1);
		}
	}
	/*
	 * Function Name: readFile
	 * Description: Responsible for reading reading file to write to client
	 * 
	 * @param: fileName - file name to be read and written to client
	 * @return: fileOutput - contents of file read, or error message
	 * 
	 * input: none
	 * Expected output: none
	 * 
	 * "Called by whom":
	 * 		run 
	 */
	public String readFile(String fileName){
		StringBuffer fileOutput = new StringBuffer();
		try {//try to read file
			String sCurrentLine;
			BufferedReader br = new BufferedReader(new FileReader(fileName));//create reader object
			while ((sCurrentLine = br.readLine()) != null) {//keep reading until not null
				fileOutput.append(sCurrentLine);//read line of file and append to buffer
			}
		}
		catch (IOException e) {
			fileOutput.append("Error reading file");//append error message if file cannot be read
		}
		return fileOutput.toString();//convert file output to string
	}
}
