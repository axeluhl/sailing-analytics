import socket

UDP_IP = "195.227.44.85"
UDP_PORT = 2013

sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_DGRAM) # UDP
sock.bind((UDP_IP, UDP_PORT))

print "Listening on 195.227.44.85 port %s" % UDP_PORT
while True:
    data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
    print "received message:", data
