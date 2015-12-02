#!/usr/bin/env python

import socket
import threading 
import signal
import sys
import os

class WebServer:
	def __init__(self, port, ip):
		self.port = port
		self.ip = ip

	def StartServer(self):
		tcpSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		tcpSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1) 
		tcpSocket.bind(("192.168.2.23",80))
		tcpSocket.listen(1)
		(clientFD, (ip, port)) = tcpSocket.accept()
		t = threading.Thread(target=self.handle,args = (clientFD, (ip,port)))
		t.start()

	def handle(self, clientFD, clientAddr):
		print "Connection Recieved form: " + "IP: " + str(clientAddr[0]) + "port:" + str(clientAddr[1])
        	try:
			data = clientFD.recv(2048)
			self.logfile(data)      
        		filename = data.split()[1]
			print "file name is:" + filename
			if filename[1:].find("/") == -1:
				self.displayFile(filename[1:],clientFD)
			else: 
				self.displayDir(filename[1:],clientFD)
        	except IOError:	
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
	def logfile(self, data):
		print data
		logfile = open("log.txt", "a")
		logfile.write("\n")
		logfile.write(data)
		logfile.close()
	
	def displayDir(self, dirname, clientFD):
		try:
			dir_list = os.listdir(dirname)
			listing = "<br>"
			for filename in dir_list:
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
                        	""")
		except OSError:
			print "in display error"
			self.displayDir(os.getcwd(),clientFD)
		
	def displayFile(self, filename, clientFD):
		try:
			print "in display file"
			f = open(filename)
                	outputData = f.read()
                	f.close()
                	clientFD.send(outputData)
                	clientFD.close()
		except IOError:
                	self.displayDir(os.getcwd(),clientFD) 

while True:
	server = WebServer("1000","127.0.0.1")
	try:
		server.StartServer()
	except KeyboardInterrupt:
		print "exit"
		sys.exit()
