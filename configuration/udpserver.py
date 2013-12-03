import socket
import sys

if len(sys.argv) <= 2:
    print "Please provide an IP, port and mode (UDP, TCP)"
    sys.exit(1)

UDP_IP = sys.argv[1]
UDP_PORT = sys.argv[2]
MODE = sys.argv[3]

print "Listening on %s port %s (%s)" % (UDP_IP, UDP_PORT, MODE)
if MODE == 'UDP':
    sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_DGRAM) # UDP
    sock.bind((UDP_IP, UDP_PORT))

    while True:
        data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
        print "received message:", data
else:
    sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_STREAM) # TCP
    sock.bind((UDP_IP, UDP_PORT))
    sock.listen(1)
    conn, addr = sock.accept()
    while True:
        data = conn.recv(1024)
        if not data:
            break
        conn.send(data) # echo
        print "received message:", data

    conn.close()

