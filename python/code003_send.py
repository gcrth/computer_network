from code001 import *
import random
f=open('config003','r')
line=f.readline()
UDPPort=line.split('=')[1][:-1]

line=f.readline()
FilterError=line.split('=')[1][:-1]

line=f.readline()
FilterLost=line.split('=')[1][:-1]

f.close()

errorRate=1/int(FilterError,10)
lostRate=1/int(FilterLost,10)


import socket

class Sender:
    def init(self):
        self.HOST = socket.gethostname()  
        self.PORT=int(UDPPort,10)
        self.address=(self.HOST,self.PORT+1)
        self.sendAddress=(self.HOST,self.PORT)
        timeout = 2    
        socket.setdefaulttimeout(timeout)
        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.s.bind(self.address)
    def send(self):
        sn=0
        for i,data in enumerate([b'package1', b'package2', b'package3',b'package4',b'package1', b'package2', b'package3',b'package4']):
            print('---------------------------------------')
            while True:
                try:
                    package=self.pack(data,sn)
                    print('frame to send ',sn,' data no ',i)
                    rand=random.random()
                    if rand<lostRate:
                        print('frame lose')                
                    elif rand<lostRate+errorRate:
                        print('frame error')
                        package=self.errorSimulation(package)
                        self.s.sendto(package,self.sendAddress)
                    else:
                        self.s.sendto(package,self.sendAddress)               
                    
                    ack=self.s.recv(100)
                    if self.isAck(ack,sn):
                        sn=1-sn
                        print('ack get ',sn)
                        break
                    else:
                        print('wrong sn ',sn,' no ',i)
                except socket.timeout:
                    print('timeout sn ',sn,' no ',i)

    def pack(self,data,sn):
        package=bytearray([sn])+data
        msg=int.from_bytes(package,'big')
        msg,msgl,_=getSendMassage(msg,len(package)*8)
        package=msg.to_bytes(msgl//8,'big')
        return package

    def errorSimulation(self,package):
        
        return package+b'0'
    
    def isAck(self,ack,sn):
        package=ack[0:1]
        if int.from_bytes(package,'big')==(1-sn):
            return True
        else:
            return False

if __name__ == "__main__":
    s=Sender()
    s.init()
    s.send()




#     # 发送数据:
#     s.sendto(data, (HOST, PORT))
#     # 接收数据:
#     print (s.recvfrom(1024)[0])
# s.close()  
