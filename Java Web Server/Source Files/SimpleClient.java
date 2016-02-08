/**********************************************************
 * Assignment:Proxy Server - SimpleClient.java
 * Author: Meraj Patel 
 * Student Number: 3248644
 * 
 * Description: SimpleClient is responsible for reading URL from a user and 
 * instantiating a server connection to SimpleProxyServer.This program will then 
 * proceed to send the url to the proxy server, then output contents retrieved from proxy server.
 * 
 * How to use: Execute to the program. You will be prompted to enter a URL. Make sure to include protocol. E.g http://localhost:4000/
 * 			   To exit, simple type exit. 
 * 
 * Honor Code: I pledge that this program represents my own program code.
*********************************************************/


//import libs
import java.lang.*;
import java.net.*;
import java.util.Scanner;
import java.io.*;

import javax.imageio.IIOException;


/*
 * Class Name: SimpleClient
 * 
 * Description: class used to read and send client request to SimpleProxyServer
 */
public class SimpleClient {

	private static Socket serverConn;

	/*
	 * Function Name: SimpleClient
	 * Description: SimpleClient class constructor. 
	 * Responsible for creating socket connection to SimpleProxyServer, and storing object in global variable, serverConn.
	 * 
	 * @param: String Host - host name of SimpleProxyServer
	 * @param: int port - port number of SimpleProxyServer
	 * @return: void
	 * 
	 * input - server to connect too details, such as host and port
	 * output - socket object assigned to serverConn
	 * 
	 * "Called by whom":
	 * 		main - to initialize server object  
	 */
	public SimpleClient(String host, int port){
		try {//try to instantiate socket connection to SimpleProxyServer
			System.out.println("Trying to connect to " + host + " " + port);
			serverConn = new Socket(host, port);
		}
		catch (IOException e) {//output message indicating SimpleProxyServer is not online
			System.out.println("Connection Refused - SimpleProxyServer is down");
			System.exit(1);
		}
		System.out.println("Made server connection.");
	}

	/*
	 * Function Name: main
	 * Description: main function is called first.
	 * Function is responsible for getting user input, instantiating a server object
	 * and calling other functions using object to serve user request. Initially, the function outputs instructions for use. Then enters a while 
	 * loop prompting the user for a URL, then calling sendCommands function to serve request. This will continue in the loop until the user types "exit". 
	 *
	 * @param: URL input by user
	 * @return: void
	 * 
	 * input: URL input by user
	 * Expected output: bye message indicating program termination
	 * 
	 * "Called by whom":
	 * 		first function called when program executed
	 * "Will call what" 
	 * 		SimpleClient - initialize constructor
	 * 		sendCommands - called to send url to newly created socket
	 */
	public static void main(String argv[]) {
		int proxyPort = 3000;
		String url = "";
		String proxyHost = "localhost";
		System.out.println("-----------------------------------------------------------------------------------------------");
		System.out.println("Welcome Client");
		System.out.println("This program is intended to serve web requests. Please input a valid URL to fetch a page off the internet or local server.");
		System.out.println("To exit, please type \"exit\"");
		System.out.println("-----------------------------------------------------------------------------------------------");
		while(!url.equals("exit\n")){
			
			try {//try statement used for gather user input
				System.out.println("Please input valid URL or type \"exit\" to quit program");
				Scanner inputReader = new Scanner(System.in);//create new scanner object for user input
				url = inputReader.nextLine();//read input
				url = url + "\n";//append \n to indicate line is complete
				System.out.println();
			}
			catch (NumberFormatException e) {//generate exception if faulty input
				System.out.println("Usage: java Simple Client [url]");
				System.exit(1);
			}
			if(!url.equals("exit\n")){//instantiate object if user enters commands excluding "exit" 
				SimpleClient proxyClient = new SimpleClient(proxyHost,proxyPort);
				proxyClient.sendCommands(url);
			}
		}
		System.out.println("Bye!");
		System.exit(1);
	}
	
	
	/*
	 * Function Name: sendCommands
	 * Description: function is responsible for writing URL to server socket (which connects to SimpleProxyServer), and reading/outputting results from 
	 * socket. To do this, BufferedReader objects are instantiated to read and write from socket (in, out).
	 * At the end, function closes read, write and server connection objects. 
	 * 
	 * @param: String url - url inputed by user
	 * @return: void
	 * 
	 * input: URL input by user
	 * Expected output: write to server socket - output to console
	 * 					read from server socket - output to console
	 * 
	 * "Called by whom":
	 * 		main
	 */
	public void sendCommands(String url) {
		try {//try to create reader, writer objects. Proceed to read and write 
			BufferedReader in = new BufferedReader (new InputStreamReader (serverConn.getInputStream()));//create object to read form serverConn
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(serverConn.getOutputStream()));//create object to write to serverConn
			String output;//string contains socket output
			try {//try to write
				out.write(url);//write URL
				out.flush();//flush
			}
			catch(IIOException e) {//catch error if unable to write
				System.out.println("Connection Refused. Shutting down SimpleClient");
				System.exit(1);
			}
			StringBuffer buffer = new StringBuffer();//create buffer to store serverConn output
			String inputLine;//string to read serverConn output
			while ((inputLine = in.readLine()) != null)
			{
				buffer.append(inputLine + "\n");
			}
			output = buffer.toString();//assign buffer to output
			System.out.println("Request ouput:");
			System.out.println();
			System.out.println(output);//print output from serverConn socket.
			System.out.println();
			in.close();//close reader
			out.close();//close writer
		} catch (IOException e) {//throw exception if unable to create objects or error occurs in read/write
			e.printStackTrace();//print exception error
		}
	}
	

	/*
	 * Function Name: finalize
	 * Description: function is called by java garbage collector when there are no more references to serverConn. 
	 * Doing this will simply close serverConn socket.  
	 * 
	 * @param: void
	 * @return: void
	 */
	public synchronized void finalize(){ 
		System.out.println("Closing down SimpleClient...");
		try {//attempt to close socket
			serverConn.close();
		}
		catch(IOException e){//print exception if failed
			System.out.println("SimpleClient: " + e);
			System.exit(1);
		}
	}
}
