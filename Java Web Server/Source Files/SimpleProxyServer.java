/**********************************************************
 * Assignment:Proxy Server - SimpleProxyServer.java
 * Author: Meraj Patel 
 * Student Number: 3248644
 * 
 * Description: SimpleProxyServer is responsible for listening for client URL request, fetching content given url and
 * sending this content back to the client. The program beings by instantiating a ServerSocket object on localhost port 3000.
 * Afterwards, the program will listen and serve client request. Each request is processed in a thread so this allows the server
 * to receive and send client request simultaneously.
 * 
 * How to use: Execute to the program. The server will be listening on localhost port 3000 for client request. No further action is required.
 * To exit the program, use CTRL-C
 * 
 * Honor Code: I pledge that this program represents my own program code.
 *********************************************************/


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

/*
 * Class Name: SimpleProxyServer
 * 
 * Description: class used to create SocketServer object to act as proxy. Listen and serve client request. 
 */
public class SimpleProxyServer {
	protected int portNo; 
	protected ServerSocket clientConnect;
	boolean listening = true;
	private boolean keepOn = true;

	/*
	 * Function Name: SimpleProxyServer
	 * Description: SimpleProxyServer class constructor. 
	 * Responsible for creating server on localhost on port 3000. 
	 * 
	 * @param: int port - port number of SimpleProxyServer
	 * @return: void
	 * 
	 * input - server to create given port number
	 * output - socket object assigned to clientConnect
	 * 
	 * 
	 * "Called by whom":
	 * 		main - to initialize server object  
	 */
	public SimpleProxyServer(int port) {
		System.out.println("Connecting server socket to port...");

		try{//try creating ServeSocket object 
			clientConnect = new ServerSocket(port);
		}
		catch(IOException e) {//catch and output error if fails
			System.out.println("Failed to connect to port " + port);
			System.exit((1));
		}
		this.portNo = port;
	}

	/*
	 * Function Name: main
	 * Description: main function is called first.
	 * Function is responsible instantiating SimpleProxyServer object and call listen function, to listen for 
	 * client requests. 
	 * 
	 * @param: String argv[] - input parameters during program execution
	 * @return: void
	 * 
	 * 
	 * input: none
	 * output: instructions on how to use
	 * 
	 * "Called by whom":
	 * 		first function called when program executed
	 * "Will call what" 
	 * 		SimpleProxyServer - initialize constructor
	 * 		listen - called to listen for clients
	 * 
	 */
	public static void main(String argv[]){

		System.out.println("-----------------------------------------------------------------------------------------------");
		System.out.println("Welcome to SimpleProxyServer");
		System.out.println("This program is intended to serve as a proxy of incoming client requests");
		System.out.println("To exit, please press CTRL-C or CMD-C");
		System.out.println("-----------------------------------------------------------------------------------------------");

		int port = 3000;
		SimpleProxyServer server = new SimpleProxyServer(port);//call constructor
		System.out.println("Proxy Server running on port " + port + "...");
		server.listen();//function call to listen for client request
	}

	/*
	 * Class Name: RunWhenShuttingDown
	 * 
	 * Description: class is created to handle shutdown of CRTL-C event. This class closes server object (clientConnect)
	 * so program can terminate properly. 
	 */
	public class RunWhenShuttingDown extends Thread {

		/*
		 * Function Name: run
		 * Description: function called when thread of RunWhenShuttingDown class is instantiated. 
		 * this function will close server object connection and terminate thread.
		 * @return: void
		 */
		public void run() {
			System.out.println("Shutting down SimpleProxyServer...");
			keepOn = false;
			try {//try to close server object
				clientConnect.close();
			} catch (IOException e) {//catch and print exception if failed
				e.printStackTrace();
			}
		}
	}

	/*
	 * Function Name: listen 
	 * Description: function listen for client request. If a request is received, a new thread is created
	 * to serve actual client request. Hook is added to handle CTRL-C shutdown. 
	 * @return: void
	 * 
	 * input:none
	 * output: messages denoting "waiting for new client" or "socket closed" 
	 * 
	 * "Called by whom":
	 * 		main
	 * "Will call what" 
	 * 		ClientThread - start new thread to server client request
	 * 
	 * 
	 * 
	 */
	public void listen(){
		Runtime.getRuntime().addShutdownHook(new RunWhenShuttingDown());//add hook to handle shutdown
		System.out.println("Waiting for clients...");
		System.out.println();

		while(keepOn) {//keep running until shutdown hook is called
			try{//try creating new thread
				new ClientThread(clientConnect.accept()).start();//create new thread, and handle client request
			}catch (IOException e){//catch error indicating socket is closed
				System.out.println("Socket Closed");
				System.exit(1);
			}
		}
	}

	/*
	 * Function Name: finalize
	 * Description: function is called by java garbage collector when there are no more references to clientConnect. 
	 * Doing this will simply close clientConnect socket.  
	 * 
	 * @return: void
	 */
	public synchronized void finalize(){
		System.out.println("Shutting down SimpleProxyServer running on port" + portNo);
		try {//attempt to close socket
			clientConnect.close();
		}
		catch(IOException e){//print exception if failed
			System.out.println("SimpleProxyServer: " + e);
			System.exit(1);
		}
	}
}

/*
 * Class Name: ClientThread
 * 
 * Description: class is created to handle client request. In terms of fetching URL content and returning it to client. 
 * Class extends Thread to allow threading.
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
	 * Description: Responsible for reading client url request from socket. Calling function to fetc url content
	 * and finally writing this url content back to client.
	 * 
	 * @param: Socket socket - socket to communicate with client
	 * @return: void
	 * 
	 * input: none
	 * Expected output: write url content to client socket - output to console
	 * 					read url from client socket - output to console
	 * 
	 * "Called by whom":
	 * 		listen 
	 * 
	 * "Will call what" 
	 * 		fetchURL - Responsible for fetching content using url given by client
	 * 
	 */
	public void run(){
		String url = "";//Variable to store url 
		String urlOutput = "";//store url content

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
				url = buffer.toString();//assign url read from socket to local variable "url" 
				//StringBuilder urlOutputSB = new StringBuilder();
				//urlOutputSB = fetchURL(clientConn, url).toString();
				urlOutput = fetchURL(url).toString();//fetch URL content and store in local varaible
				System.out.println();
				System.out.println("Got request");
				System.out.println("Request Output:");
				System.out.println();
				System.out.println(urlOutput);//output url content
				if (urlOutput != null){
					System.out.println("Writing data to client from server ");
					out.write(urlOutput.getBytes());//write url content to client
					out.flush();
				}
				System.out.println();
			}
			catch (IOException e){//output to client invalid url if exception is caught
				System.out.println("Error reading data");
				urlOutput = "Invalid URL";
				out.write(urlOutput.getBytes());//write "Invalid URL" to client
				out.flush();
			}
			reader.close();//close read object
			out.close();//close write object
			clientConn.close();//close server object
		}
		catch (IOException e){//catch error if failed to create read/write object
			System.out.println("Socket Use Error");
			System.exit(1);
		}
	}

	/*
	 * Function Name: fetchURL
	 * Description: Responsible for fetching content using url given by client.
	 * This is done by creating a URLConnection, creating a connection and reading content. 
	 * 
	 * @param: String url - url to fetch
	 * @return: StringBuilder urlOutput - contains url output content
	 * 
	 * input: URL
	 * Expected output: url content
	 * 
	 * "Called by whom":
	 * 		run
	 * 
	 * "Will call what" 
	 * 		readContent - fetching content given client url
	 * 
	 * 
	 */
	public StringBuilder fetchURL(String url) throws IOException{
		URL clientURL;//client url
		StringBuilder urlOutput = new StringBuilder();//hold url content output
		try {//attempt to read url content
			clientURL = new URL(url);//create url object 
			URLConnection yc = clientURL.openConnection();//create open connection object to read client url

			if (yc instanceof HttpURLConnection){//check if connect is HTTP based. Proceed if so.
				int status = ((HttpURLConnection) yc).getResponseCode();//get response code from url connection
				if (status == 200){//check if response is OK
					urlOutput = readContent(yc);//store output if response if OK
				}
				else if(status == 404){//check if 404
					urlOutput.append("404 Not Found");
				}
				else if(status == 403){//check if 403
					urlOutput.append("403 Bad Request");
				}
			}
			else{//Proceed if request is not HTTP based
				System.out.println("NOT HTTP REQUEST");
				urlOutput = readContent(yc);//read url content and store in local variable urlOutput
			}

		} catch (MalformedURLException e) {//catch exception if url cannot be connected to
			urlOutput.append("Invalid URL");
		}
		return urlOutput;//return url content
	}

	/*
	 * Function Name: readContent
	 * Description: Responsible for reading url content
	 * @param: URLConnection yc - URLConnection to read from
	 * @return: StringBuilder urlOutput - contains url output content
	 * 
	 * input: URLConnection object
	 * Expected output: url content
	 * 
	 * "Called by whom":
	 * 		fetchURL 
	 * 
	 */
	public StringBuilder readContent(URLConnection yc){
		StringBuilder urlOutput = new StringBuilder();
		try{//try creating read object
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));//create new reader object given URLConnection 
			String inputLine;
			while ((inputLine = in.readLine()) != null)//keep reading content until null is found
			{
				urlOutput.append(inputLine + "\n");//append to buffer
			}
		}catch(IOException e){//catch and print excpetion if failed to create read object 
			System.out.println("Exception: " + e );
		}
		return urlOutput;//return url content
	}
}
