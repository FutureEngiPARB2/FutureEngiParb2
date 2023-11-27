#Import packages
import socket

class ServerSocket:
    """
    This represents a Server Socket Connection.
    """
    MSGLEN = 512 #length of Message
    def __init__(self, sock=None):
        if sock is None:
            self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            host = socket.gethostname()
            #host = '127.0.0.1'
            IPAddr = socket.gethostbyname(host)
            #print(IPAddr)
            port = 8080
            print("IP-Address --> "+ IPAddr +" port no. --> " + str(port))
            self.sock.bind((host,port))

            self.sock.listen(1)

        else:
            self.sock = sock

    def accept(self, timeout=60):
        (conn, addr) = self.sock.accept()
        print("Connected with ",addr)
        self.server_socket = conn

    def send(self, msg):
        sent = self.server_socket.send(msg.encode("utf-8"))

    def receive(self):
        chunks = []
        while True:
            
            chunk = self.server_socket.recv(1)

            chunks.append(chunk)
            if chunk == b"\n" or chunk == b"":
                break
        return b"".join(chunks).decode("utf-8")

    def close(self):
        try:
            self.sock.close()
            self.server_socket.close()
        except:
            print("Could not close all sockets connection")


class DriveValue:
    """
    This represents a drive value for either left or right control. Valid values are between -1.0 and 1.0
    """

    MAX = 1.0
    MIN = -1.0
    DELTA = 0.05

    value = 0.0

    def reset(self):
        self.value = 0.0
        return self.value
 
    def incr(self, by_value=0):
        self.value = min(
            self.MAX, self.value + (by_value if by_value != 0 else self.DELTA)
        )
        return round(self.value, 3)

    def decr(self, by_value=0):
        self.value = max(
            self.MIN, self.value - (by_value if by_value != 0 else self.DELTA)
        )
        return round(self.value, 3)

    def max(self):
        self.value = self.MAX
        return self.value

    def min(self):
        self.value = self.MIN
        return self.value

    def write(self, value):
        self.value = value
        return self.value

    def read(self):
        return round(self.value, 3)
