#!/usr/bin/env python

#import these libraries
import socket
import threading 
import signal
import sys
import os

#create WebServer class
class WebServer:

	def StartServer(self):#class constructor
		tcpSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)#AF_INET refers to IPV4 addressing
								             #SOCK_STREAM refers to TCP two way connection
		tcpSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)#SOL_SOCKET is used to set socket level options
									       #SO_REUSEADDR infers reusability of socket
		tcpSocket.bind(("192.168.2.23",80))#Replace IP with your LAN IP
		tcpSocket.listen(1)
		(clientFD, (ip, port)) = tcpSocket.accept()#got client
		t = threading.Thread(target=self.handle,args = (clientFD, (ip,port)))#make thread to server client
		t.start()#start thread

	def handle(self, clientFD, clientAddr):
		print "Connection Recieved form: " + "IP: " + str(clientAddr[0]) + "port:" + str(clientAddr[1])
        	try:
			data = clientFD.recv(2048)#Read client request
        		filename = data.split()[1]#Split request, save only resource request. Discard metadata
			print "file name is:" + filename
			if filename[1:].find("/") == -1:#find out if file or directory
				self.displayFile(filename[1:],clientFD)
			else: 
				self.displayDir(filename[1:],clientFD)
        	except IOError:	#write back invalid URL if error caught
			clientFD.send("""<html>
                        <head>
                        <title>404 Bad Request</title>
                        </head>
                        <body>
                        404 Bad Request
                        </body>
                        </html>
                        """)
                        clientFD.close()
	
	def displayDir(self, dirname, clientFD):
		try:
			dir_list = os.listdir(dirname)#save file names from dir
			listing = "<br>"
			for filename in dir_list:#Format file names to be in HTML format
				listing = listing + "<a href="+filename+" download>" + filename + "</a><br>"
			clientFD.send("""<html>
                        	<head>
                       		<title>Directory Downloads</title>
                        	</head>
				<h1> Directory Downloads</h1>
                        	<body>"""
                        	+listing+
                        	""" </body>
                        	</html>
                        	""")#Write file names to client
		except OSError:
			print "in display error"
			self.displayDir(os.getcwd(),clientFD)#display current dir if error
		
	def displayFile(self, filename, clientFD):
		try:
			print "in display file"
			f = open(filename)#open file client requested
                	outputData = f.read()#save content
                	f.close()
                	clientFD.send(outputData)#write content
                	clientFD.close()
		except IOError:
                	self.displayDir(os.getcwd(),clientFD)#display current dir if error 

while True:
	server = WebServer()
	try:
		server.StartServer()
	except KeyboardInterrupt:
		print "exit"
		sys.exit()
