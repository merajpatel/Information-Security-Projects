/**********************************************************
 * Assignment:Proxy Server - SimpleWebServer.java
 * Author: Meraj Patel 
 * Student Number: 3248644
 * 
 * Description: SimpleWebServer is responsible for listening for client URL request, fetching file given url and
 * sending this file back to the client. The program beings by instantiating a ServerSocket object on localhost port 4000.
 * Afterwards, the program will listen and serve client request.
 * 
 * How to use: Execute to the program. The server will be listening on localhost port 4000 for client request. No further action is required
 * to exit the program, use CTRL-C
 * 
 * Honor Code: I pledge that this program represents my own program code.
 *********************************************************/


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.PatternSyntaxException;
import java.io.FileReader;

/*
 * Class Name: SimpleWebServer
 * 
 * Description: class used to create server socket that listens for client requests and serves them requested content.
 */
public class SimpleWebServer {
	protected int portNo; // Port to listen to for clients   
	protected ServerSocket clientConnect; 
	private  boolean keepOn = true;

	/*
	 * Function Name: SimpleWebServer
	 * Description: SimpleWebServer class constructor. 
	 * Responsible for creating server on localhost on port 4000. 
	 * 
	 * @param: int port - port number of SimpleWebServer
	 * @return: void
	 * 
	 * input - server to create given port number
	 * output - socket object assigned to clientConnect
	 * 
	 * "Called by whom":
	 * 		main - to initialize server object  
	 * 
	 */
	public SimpleWebServer(int port) throws IllegalArgumentException {
		System.out.println("Connecting server socket to port...");

		try{ //try creating ServeSocket object 
			clientConnect = new ServerSocket(port);
		}
		catch(IOException e) {//catch and output error if fails
			System.out.println("Failed to connect to port" + port);
			System.exit(1);
		}
		this.portNo = port;
	}

	/*
	 * Function Name: main
	 * Description: main function is called first.
	 * Function is responsible instantiating SimpleWebServer object and listen for client requests. 
	 * @param: String argv[] - input parameters during program execution
	 * @return: void
	 * 
	 * input: none
	 * output: instructions on how to use
	 * 
	 * "Called by whom":
	 * 		first function called when program executed
	 * "Will call what" 
	 * 		SimpleWebServer - initialize constructor
	 * 		listen - called to listen for clients
	 * 
	 * 
	 */
	public static void main(String args[]){

		System.out.println("-----------------------------------------------------------------------------------------------");
		System.out.println("Welcome to SimpleWebServer");
		System.out.println("This program is intended to serve as a web server for incoming client requests");
		System.out.println("To exit, please press CTRL-C or CMD-C");
		System.out.println("-----------------------------------------------------------------------------------------------");

		int port = 4000;
		SimpleWebServer webServer = new SimpleWebServer(port);
		webServer.listen();
	}

	/*
	 * Function Name: listen 
	 * Description: function listen for client request. If a request is received, function is called to serve client.
	 * Hook is added to handle CTRL-C shutdown. 
	 * @return: void
	 * 
	 * 
	 * input:none
	 * output: messages denoting "socket closed" 
	 * 
	 * "Called by whom":
	 * 		main
	 * "Will call what" 
	 * 		manageClient - serve client request
	 * 
	 * 
	 */
	public void listen(){

		try {//try accepting client connection, and serve request
			Runtime.getRuntime().addShutdownHook(new ShutDownSimpleServer());//add hook to handle shutdown
			while(keepOn) {
				Socket clientConn = clientConnect.accept();
				manageClient(clientConn);	
			}
		}//catch error indicating socket is closed
		catch(IOException e){
			System.out.println("Socket Closed");	
		}
	}

	/*
	 * Class Name: ShutDownSimpleServer
	 * 
	 * Description: class is created to handle shutdown of CRTL-C event. This class closes server object (clientConnect)
	 * so program can terminate properly. 
	 */
	public class ShutDownSimpleServer extends Thread {
		public void run() {
			System.out.println("Shutting down SimpleWebServer...");
			keepOn = false;
			try {//try to close server object
				clientConnect.close();
			} catch (IOException e) {//catch and print exception if failed
				e.printStackTrace();
			}
		}
	}

	/*
	 * Function Name: manageClient
	 * Description: Responsible for reading client request from socket. Fetching content
	 * and finally writing this url content back to client.
	 * 
	 * @param: Socket client - socket to communicate with client
	 * @return: void
	 * 
	 * input: client socket 
	 * Expected output: write to client socket - output to console
	 * 					read from client socket - output to console
	 * 
	 * "Called by whom":
	 * 		listen 
	 * "Will call what" 
	 * 		manageReq - fetching content (html pages) given client request
	 * 
	 */
	public void manageClient(Socket client){
		System.out.println();
		System.out.println("Servicing client");
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));//create object to read from client
			PrintWriter out = new PrintWriter(client.getOutputStream());//create object to write to client
			String req = "";
			try{//try to read from client
				StringBuffer buffer = new StringBuffer();
				while (true) {//loop used to read individual char from client socket, and append to buffer to unify request.
					int ch = reader.read();
					if ((ch < 0) || (ch == '\n')) {//break if new line is found
						break;
					}
					buffer.append((char) ch);//append to buffer
				}
				req = buffer.toString();//assign request to local variable
				String response;//create local variable to store response
				response = manageReq(req);//call function to get response given request. Store response in local variable
				if (response.equals("404")){//check if 404
					System.out.println("Returning 404");
					out.println("HTTP/1.1 404 Not Found\r\n");//write 404  
					out.flush();
					//client.close();
				}
				else if (response.equals("403")){//check if 403
					System.out.println("Returning 403");//write 403
					out.println("HTTP/1.1 403 Bad Request\r\n");
					out.flush();
					//client.close();
				}
				else{//else response if good
					System.out.println("Writing data to client from server ");
					out.println("HTTP/1.1 200 OK\r\n");                            // Return status code for OK (200)                                             // Writes the FILE contents to the client
					out.print(response);//write response to client
					System.out.println("Finished write to server");
					out.flush();
				}
				//client.close();//close client socket
			}//output exception if failed
			catch (IOException e){
				out.flush();
			}
			System.out.println("closing sockets");
			reader.close();//close read object
			out.close();//close write object
			client.close();//close client socket
		}catch (IOException e){
			System.out.println("Socket Use Error");
			System.exit(1);
		}
	}

	/*
	 * Function Name: manageReq
	 * Description: Responsible for fetching content (html pages) given client request.
	 * Will return content in form of String so can write back to client socket.
	 * 
	 * @param: String req - req to fetch
	 * @return: String reqOutput - contains requested content
	 * 
	 * "Called by whom":
	 * 		manageClient 
	 * "Will call what" 
	 * 		readFile -  reading file from server directory
	 * 
	 * 
	 */
	public String manageReq(String req){
		String[] splitArray = null;
		String reqOutput = null;
		try {
			splitArray = req.split("\\s+");//split client request to distinguish protocol, file path and other information
		} catch (PatternSyntaxException ex) {
			System.out.println(ex);        	
		}
		if (splitArray[1].equals("/")){//check if file path contains only "/". If so, server index.html
			reqOutput = readFile("index.html");//send index.html
		}
		else if(splitArray[1].charAt(splitArray[1].length() - 1) != '/'){//check if file path contains a file 
			reqOutput = readFile(splitArray[1].substring(1));//fetch file
		}
		else{//send error code else wise
			reqOutput = "403";
		}
		return reqOutput;
	}

	/*
	 * Function Name: readFile
	 * Description: Responsible for reading file from server directory
	 * 
	 * @param: String filepath - filepath of file to read
	 * @return: String fileOutput - contains file content
	 * 
	 * "Called by whom":
	 * 		manageReq 
	 * 
	 * 
	 */
	public String readFile(String filepath){
		StringBuffer fileOutput = new StringBuffer();
		try {//try to read file
			String sCurrentLine;
			BufferedReader br = new BufferedReader(new FileReader(filepath));//create reader object
			while ((sCurrentLine = br.readLine()) != null) {//keep reading until not null
				fileOutput.append(sCurrentLine);//read line of file and append to buffer
			}
		}//append 404 error indicating file not found 
		catch (IOException e) {
			fileOutput.append("404");
		}
		return fileOutput.toString();
	}
}
