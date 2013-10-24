import socket, sys

if len(sys.argv) <= 2:
    print "Please provide ip and mode (TCP, UDP)"
    sys.exit(1)

UDP_IP = sys.argv[1]
UDP_PORT = 2013
MODE = sys.argv[2]
MESSAGE = "Hello, World!"

print "Target IP:", UDP_IP
print "Target port:", UDP_PORT
print "Message:", MESSAGE
print "Mode: ", MODE

if MODE == 'UDP':
    sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_DGRAM) # UDP
    sock.sendto(MESSAGE, (UDP_IP, UDP_PORT))

else:
    sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_STREAM) # TCP
    sock.connect((UDP_IP, UDP_PORT))
    sock.send(MESSAGE)
    data = sock.recv(1024)
    sock.close()
