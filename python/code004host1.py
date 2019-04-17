from code001 import *
import socket
import random
import threading

MAX_SEQ=7
MAX_BUFSIZE=7

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

class host1:
    def init(self):
        self.HOST = socket.gethostname()  
        self.PORT=int(UDPPort,10)
        self.address=(self.HOST,self.PORT+1)
        self.sendAddress=(self.HOST,self.PORT)

        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.s.bind(self.address)
        self.timer=threading.Timer(2,self.reportTimeout)
        self.recvEvent=threading.Event()
        self.recvEvent.clear()
        self.processEvent=threading.Event()
        self.processEvent.clear()
        self.timeoutEvent=threading.Event()
        self.timeoutEvent.clear()
        threading.Thread(self.recv)

    def send(self):
        frameExpextedToRecv=0

        sn=0
        ack=0

        i_data=0

        bufferSize=0
        buffer=[-1 for _ in range(MAX_BUFSIZE)]

        data =[b'package1', b'package2', b'package3',b'package4',b'package1', b'package2', b'package3',b'package4']

        while True:
            print('---------------------------------------')
            if self.recvEvent.is_set():
                if self.check(self.recvBuf,frameExpextedToRecv):
                    print('right frame sn ',sn)
                    print(self.unpack(self.recvBuf))
                    ack=self.recvBuf[-3]
                    bufferSize=(sn-ack+MAX_SEQ+1)%(MAX_SEQ+1)
                    print('ack get ',ack)
                else:
                    print('wrong frame')                
            elif self.timeoutEvent.is_set():
                for i in range(bufferSize):
                    package=self.pack(data[buffer[ack+1]],sn,frameExpextedToRecv)
                    print('frame to send ',sn,' data no ',buffer[ack+1])
                    rand=random.random()
                    if rand<lostRate:
                        print('frame lose')                
                    elif rand<lostRate+errorRate:
                        print('frame error')
                        package=self.errorSimulation(package)
                        self.s.sendto(package,self.sendAddress)
                    else:
                        self.s.sendto(package,self.sendAddress) 
            elif bufferSize<MAX_BUFSIZE:
                package=self.pack(data[i_data],sn,frameExpextedToRecv)
                print('frame to send ',sn,' data no ',i_data)
                buffer[sn]=i_data
                sn=(sn+1)%(MAX_SEQ+1)
                bufferSize+=1
                i_data+=1
                rand=random.random()
                if rand<lostRate:
                    print('frame lose')                
                elif rand<lostRate+errorRate:
                    print('frame error')
                    package=self.errorSimulation(package)
                    self.s.sendto(package,self.sendAddress)
                else:
                    self.s.sendto(package,self.sendAddress)               
            
    def recv(self):
        while True:
            self.recvBuf=self.s.recv()
            self.recvEvent.set()
            self.timer.cancel()
            self.processEvent.wait()
            self.processEvent.clear()
            
    def pack(self,data,sn,ack):
        package=bytearray([sn])+data+bytearray([ack])
        msg=int.from_bytes(package,'big')
        msg,msgl,_=getSendMassage(msg,len(package)*8)
        package=msg.to_bytes(msgl//8,'big')
        return package

    def errorSimulation(self,package):
        return package+b'0'
    
    def reportTimeout(self):
        self.timeoutEvent.set()

    def unpack(self,package):
        return package[1:-3]

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
    s=host1()
    s.init()
    s.send()

