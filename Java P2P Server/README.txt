
Instructions on use:

1) You must import p2p.sql file in “MySQL File” folder within you mysql database. The MYSQL version is 5.7.10. 
	
	Also please ensure your mysql username is: root, and password is:password. 
	This is because the program uses these credentials to log into mysql

	I managed to import the sql file using this linux command: 
		
		“./mysql -u root -ppassword P2P < /Users/Desktop/p2p.sql”
		
		Please make sure you create a P2P database first before importing

2) You can begin to test the program by navigating to “Jar files” folder. You must run the following linux commands to begin testing: 

	a) Start orb listening - “orbd -ORBInitialPort 1050”
	b) start server - “java -jar server.jar -ORBInitialPort 1050 -ORBInitialHost localhost”
	c) start client1 (navigate to client folder) - “java -jar client.jar -ORBInitialPort 1050 -ORBInitialHost localhost”
	d) start client2 (navigate to client2 folder) - “java -jar client2.jar -ORBInitialPort 1050 -ORBInitialHost localhost”

	Input your commands when running client/client2 jar files. Instructions are displayed appropriately 

	And thats it! The program should be fully working if you follow these steps correctly. 




Attached files: 

1) I have attached all assignment 2 source files. This is seen is “Assignment2” folder. Feel free to drag and drop this into eclipse
   and compile into jar files if you wish
2) I added p2p.idl file I created. This is seen in “IDL” folder
3) I added test report. This is seen in “Test Report” Folder
4) Commented source files can be see in “Assignment2/src/P2PClient.java” and “Assignment2/src/P2PServer.java”



Thanks! Please contact me if you have any questions or concerns.

Meraj 

