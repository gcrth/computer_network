from code001 import *
f=open('config003','r')
line=f.readline()
UDPPort=line.split('=')[1][:-1]

line=f.readline()
FilterError=line.split('=')[1][:-1]

line=f.readline()
FilterLost=line.split('=')[1][:-1]

f.close()

import socket

class Recv:
    def init(self):
        self.HOST = socket.gethostname()  
        self.PORT=int(UDPPort,10)
        self.address=(self.HOST,self.PORT)
        self.sendAddress=(self.HOST,self.PORT+1)
        # timeout = 20    
        # socket.setdefaulttimeout(timeout)
        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.s.bind((self.HOST,self.PORT))
    def recv(self):
        sn=0
        while True:
            print('frame expected ',sn)
            package,_=self.s.recvfrom(1024)
            if self.check(package,sn):
                print('right frame sn ',sn)
                print(self.unpack(package))
                sn=1-sn
                self.s.sendto(bytes([sn]),self.sendAddress)    
                print('ack send ',sn)
                print('---------------------------------------')
            else:
                print('wrong frame')
                self.s.sendto(bytes([sn]),self.sendAddress)
                print('ack send ',sn)
        
    def unpack(self,package):
        return package[1:-2]

    def check(self,package,sn):
        msg=int.from_bytes(package,'big')
        isSuccess,_=checkReceiveMassage(msg,len(package)*8)
        if not isSuccess:
            return False
        if package[0]!=sn:
            return False
        else:
            return True

if __name__ == "__main__":
    r= Recv()
    r.init()
    r.recv()
