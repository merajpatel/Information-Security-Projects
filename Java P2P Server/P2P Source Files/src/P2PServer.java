/**********************************************************
 * Assignment:P2P Server - P2PServer.java
 * Author: Meraj Patel 
 * Student Number: 3248644
 * 
 * Description: P2PServer is responsible for serving incoming client request from P2PClient.java program. This program is
 * responsible for communicating with mysql server if the client wants too add, delete or download files. All appropiate actions
 * will be dealt with as seen in the functions below.
 * 
 * How to use: Execute to the program with proper parameters: 
 * 			eg: java -jar server.jar -ORBInitialPort 1050 -ORBInitialHost localhost
 * 			No further action is required
 * 
 * Honor Code: I pledge that this program represents my own program code.
 *********************************************************/
import java.awt.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.omg.CORBA_2_3.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CORBA.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import com.mysql.jdbc.PreparedStatement;

import P2P.ClientRequest;
import P2P.ClientRequestHelper;
import P2P.ClientRequestPOA;

public class P2PServer extends ClientRequestPOA{
	private org.omg.CORBA.ORB orb;
	private java.sql.Statement stmt;
	private Connection conn;

	/*
	 * Function Name: P2PServer
	 * Description: P2PServer class constructor. 
	 * Responsible for connecting with mysql database for filename, host ip, host port storage
	 * 
	 * @param: none
	 * @return: void
	 * 
	 * input - none
	 * output - none
	 * 
	 * 
	 * "Called by whom":
	 * 		main - to initialize server object  
	 */
	public P2PServer() { 
		try {
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());//create new database device manager
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/P2P", "root", "password") ;//connect to database given username and password
			stmt = conn.createStatement();//create statement for mysql querying and statement execution
		} catch (Exception ex) {}//catch exception
	}
	
	/*
	 * Function Name: setORB
	 * Description: setting orb
	 * 
	 * @param: org.omg.CORBA.ORB orb
	 * @return: void
	 * 
	 * input - org.omg.CORBA.ORB orb
	 * output - none
	 * 
	 * 
	 * "Called by whom":
	 * 		main - to initialize server object  
	 */
	public void setORB(org.omg.CORBA.ORB orb) {
		this.orb = orb; 
	}
	
	/*
	 * Function Name: addFile
	 * Description: add file components (name, ip, port) to mysql database
	 * 
	 * @param: fileName: name of file
	 * 		   fileIp: ip address of client
	 * 		   filePort: port number of client
	 * @return: fileResults - message indicating success or failure of file upload
	 * 
	 * 
	 * "Called by whom":
	 * 		P2PClient 
	 *  "Will call what" 
	 *  	searchFile - search files on current mysql server, to check if filename is already on there
	 */
	public String addFile(String fileName, String fileIP, String filePort){
		String fileResults;//hold result of file upload (success, or failure)
		String fileNameCheck;//check if file exists on server
		int queryResults;//results for query 
		try {
			fileNameCheck = searchFile(fileName);//perform filename check
			if (fileNameCheck.equals("No file found")){//if file is not on server, add it
				//add file
				queryResults = stmt.executeUpdate("INSERT INTO files (fileName, fileIP, filePort) VALUES " + "(" +'"'+ fileName + '"'+ "," +'"'+ fileIP +'"'+ "," +'"'+filePort +'"'+")");
				if (queryResults > 0){//check if successful
					fileResults = "Added Successfully";//add successful message
				}
				else {
					fileResults = "Failed - Error with file name or path";//add unsuccessful message
				}
			}
			else  {
				fileResults = "Filename already used. please use different name";//add unsuccessful message
			}
		} catch (SQLException e) {//catch exception
			fileResults = "Error inserting file";//add unsuccessful message
			e.printStackTrace();
		}
		return fileResults;//return file result to client
	}

	/*
	 * Function Name: shutdown
	 * Description: add file components (name, ip, port) to mysql database
	 * 
	 * @param: none
	 * @return: none
	 * 
	 * 
	 * "Called by whom":
	 * 		P2PClient 
	 */
	public void shutdown() {
		orb.shutdown(false);//shutdown orb connection
	}

	/*
	 * Function Name: removeFile
	 * Description: remove file components (name, ip, port) to mysql database
	 * 
	 * @param: fileName: name of file
	 * 		   fileIp: ip address of client
	 * 		   filePort: port number of client
	 * @return: fileResults - message indicating success or failure of removal
	 * 
	 * 
	 * "Called by whom":
	 * 		P2PClient 
	 *  
	 */
	public String removeFile(String fileName, String fileIP, String filePort) {
		String fileResults;//remove file result
		try {//try file removal
			//execute file removal query
			int queryResults = stmt.executeUpdate("DELETE FROM files where fileName = " +  '"' + fileName + '"' + "AND fileIP = " +  '"' + fileIP + '"' + "AND filePort = " +  '"' + filePort + '"');
			if (queryResults > 0){//check if query is successful
				fileResults = "Delete Successful";//add successful message
			}
			else {
				fileResults = "Delete Failed - Error with file name";//add unsuccessful message
			}
		} catch (SQLException e) {//catch exception with file removal
			fileResults = "Error querying file";//add unsuccessful message
			e.printStackTrace();
		}
		return fileResults;//return file result
	}

	/*
	 * Function Name: searchFile
	 * Description: search for file on mysql database
	 * 
	 * @param: fileName: name of file
	 * 		   
	 * @return: fileResults - message indicating success or failure of search
	 * 
	 * "Called by whom":
	 * 		P2PClient 
	 * 		addFile
	 * 		downloadFile
	 *  
	 */
	public String searchFile(String fileName) {
		ResultSet queryResults;//save query results of search 
		String fileResults;//save success or failure message of search
		try {//try executing search
			queryResults = stmt.executeQuery("SELECT * FROM files WHERE fileName = " + '"' + fileName + '"');//execute search query
			if (!queryResults.next()){//check if query contains no result
				fileResults = "No file found";//save no file found message
			}
			else {
				fileResults = "File found";//save file found message into fileResults
			}
		} catch (SQLException e) {//catch exception
			fileResults = "Error querying file";//save error message in fileResults
			e.printStackTrace();
		}
		return fileResults;//return fileResults
	}

	/*
	 * Function Name: viewAllFiles
	 * Description: view all files on mysql database
	 * 
	 * @param: none
	 * @return: fileName - names of all files on server
	 * 
	 * "Called by whom":
	 * 		P2PClient 
	 *  
	 */
	public String viewAllFiles() {
		ResultSet results;//save results
		String fileName = "\nAvailble Files to Download: ";//fileName variable to hold file names
		try {//try executing query
			results = stmt.executeQuery("SELECT fileName FROM files");//execute view all files query
			if(!results.next()){//check if results is empty
				fileName = fileName + "\nNo Availble Files";//save no availble files if result is empty
			}
			else{
				do{
					fileName = fileName + "\n" + results.getString("fileName");//save file name in fileName string
				}while (results.next());//keep saving file name until results reaches null
			}
		} catch (SQLException e) {//catch exception
			e.printStackTrace();//stack trace
		}
		return fileName;//return fileName results to P2PClient
	}

	/*
	 * Function Name: downloadFile
	 * Description: download file listed on mysql server. Provide hostIP and hostPort so client can download file
	 * 
	 * @param: fileName - name of file to download
	 * @return: fileResults - hostIP and hostPort of client who hold file available to download
	 * 
	 * "Called by whom":
	 * 		P2PClient 
	 *  "Will call what" 
	 *  	searchFile - search files on current mysql server, to check if filename is already on there
	 */
	public String downloadFile(String fileName) {
		ResultSet queryResults;//save query results
		String fileResults = "";//String used to save hostIP and hostPort of filename wanted to download
		String fileNameCheck;//String to check if file is on server
		String fileIP = null;//save fileIP of client holding file
		String filePort = null;//save filePort of client holding file
		try {
			fileNameCheck = searchFile(fileName);//perform filename check
			if (fileNameCheck.equals("No file found")){//check if file is on server
				fileResults = fileNameCheck;//output no file found to client
			}
			else {//else execute query
				//execute query for finding hostip and hostport
				queryResults = stmt.executeQuery("SELECT fileIP, filePort FROM files WHERE fileName = " +  '"' + fileName + '"');
				while(queryResults.next()){//while loop to iterate through results
					fileIP = queryResults.getString("fileIP");//save ip to fileIP
					filePort = queryResults.getString("filePort");//save port ot filePort
				}
				//System.out.println(queryResults.getString("fileIP"));
				fileResults = fileIP + "," + filePort;//save file ip and file port to file results
			}
		} catch (SQLException e) {//catch exception
			fileResults = "Error querying file";//save error
			e.printStackTrace();
		}
		return fileResults;//return results of download file action
	}

	/*
	 * Function Name: main
	 * Description: start P2P server so client can initiate requests 
	 * 
	 * @param: argv - input arguments for server
	 * 					eg: java -jar server.jar -ORBInitialPort 1050 -ORBInitialHost localhost
	 * @return: void
	 * 
	 * "Called by whom":
	 * 		init
	 */
	public static void main(String argv[]) {
		try {
			// create and initialize the ORB
			org.omg.CORBA.ORB orb = ORB.init(argv, null);      
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			P2PServer addobj = new P2PServer();
			addobj.setORB(orb); 

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(addobj);
			ClientRequest href = ClientRequestHelper.narrow(ref);

			org.omg.CORBA.Object objRef =  orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			NameComponent path[] = ncRef.to_name( "P2PServer" );
			ncRef.rebind(path, href);

			System.out.println("Server ready and waiting ...");

			// wait for invocations from clients
			for (;;){
				orb.run();
			}
		}
		catch (Exception e) {
			System.err.println(e);
			e.printStackTrace(System.out);
		}
	}

	/*
	 * Function Name: clearUploads
	 * Description: clear client uploads on mysql server
	 * 
	 * @param: ip - ip of client
	 * 		   port - port number of client
	 * @return: fileResults - success or failure message of clearing client uploads
	 * 
	 * "Called by whom":
	 * 		P2PServer
	 */
	public String clearUploads(String ip, String port) {
		int queryResults;//variable to save query results
		String fileResults;//save success or failure message of query execution
		try {//try query execution
			//Initiate query execution and save results
			queryResults = stmt.executeUpdate("DELETE FROM files WHERE fileIP = " +  '"' + ip + '"' + "AND filePort = " +  '"' + port + '"');
			if (queryResults > 0){//check if successful
				fileResults = "Delete Successful";//add successful message
			}
			else {
				fileResults = "Delete Failed";//else add unsuccessful message
			}
		} catch (SQLException e) {//catch exception
			fileResults = "Failed removing files";//add unsuccessful message
			e.printStackTrace();
		}
		return fileResults;//return results of clearing uploads (success or unsuccessful) 
	}
}