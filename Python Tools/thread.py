#!/usr/bin/env python 

import thread
import time


def worker_thread(id):
	count = 1

	print "Thread ID %d now alive"%id	

	while True :
		print "Thread with ID %d has counter value %d"%(id, count)
		time.sleep(2) 
		count = count + 1

for i in range(5) :
	thread.start_new_thread(worker_thread, (i,))

print "Main thread going for infinite loop"
while True:
	pass
