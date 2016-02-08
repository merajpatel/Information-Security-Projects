#!/usr/bin/env python
import socket
import struct
import binascii


HOST = socket.gethostbyname(socket.gethostname())
rawSocket = socket.socket(socket.AF_INET, socket.SOCK_RAW, socket.IPPROTO_IP)
rawSocket.bind((HOST,0))

print "starting"
print HOST
pkt = rawSocket.recvfrom(65565)

ethernetHeader = pkt[0][0:14]#ethernet header is 14 bytes constantly

eth_hdr = struct.unpack("!6s6s2s", ethernetHeader)#! -> network byte order (aka big endian)


binascii.hexlify(eth_hdr[0])#destination mac address
binascii.hexlify(eth_hdr[1])#source mac address
binascii.hexlify(eth_hdr[2])#ether type (for IP) What is encapsulted in ethernet header 

ipHeader = pkt[0][14:34] #20 bytes
ip_hdr = struct.unpack("!12s4s4s", ipHeader)#first 12 are misc info. 4 is destination. 4 is source 

print "Source ip address: " + socket.inet_ntoa(ip_hdr[1]) #print source ip
print "Destination ip address: " + socket.inet_ntoa(ip_hdr[2]) #print destination ip


#exercise is to unpack tcp header, find destination and port number

tcpHeader = pkt[0][34:54]
tcp_hdr = struct.pack("!HH16s",tcpHeader)

#exercise: 1) create a packet sniffer using raw sockets which can parse TCP packets
#	   2) create a sniffer which only uses filter to only print details of HTTP packet (TCP, port 80). Also dump data 




