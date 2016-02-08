import socket
import thread

class server(object):
	
	def __init__(self,host,port):
		self.addr = (host, port)

	def worker_thread(self, client, client_addr):
                print "Recieved connection from: ", client_addr[0]
                client.send("Welcome to Echo Server\n")
                data = "dummy"
                while len(data):
                        data = client.recv(2048)#argument refers to length to accept
                        print "Client Sent: " + data
                        client.send(data)
                print "\Bye!"
                client.close()
		
	
	def start_echo(self): 
		
		tcpSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)#this will create tcp socket given IPv4 addresssing
		tcpSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)# allow socket resure if server terminates. Without this port will stall when terminiated
		tcpSocket.bind(("127.0.0.1",8000)) 
		tcpSocket.listen(1)#argument is number of concurent clients socket can handle 
		while True:
			(client, (ip, port)) = tcpSocket.accept() #tuple is needed. client refers to descriptor. Second is tuple of ip and client 	
			thread.start_new_thread(self.worker_thread,(client, (ip, port))) 		
		tcpSocket.close()

if __name__ == "__main__":
	print "\nStarting Echo Server\n"
	server("127.0.0.1", 8080).start_echo()	
