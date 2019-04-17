from code001 import *
import socket
import random
import threading
import time

MAX_SEQ = 7
MAX_BUFSIZE = 7
timeForSend = 0.5
timeout = 5.0
dataLen = 4

f = open('config003', 'r')
line = f.readline()
UDPPort = line.split('=')[1][:-1]

line = f.readline()
FilterError = line.split('=')[1][:-1]

line = f.readline()
FilterLost = line.split('=')[1][:-1]

f.close()

errorRate = 1/int(FilterError, 10)
lostRate = 1/int(FilterLost, 10)


class host1:
    def init(self):
        self.recvCount = 0
        self.sendCount = 0
        self.HOST = socket.gethostname()
        self.PORT = int(UDPPort, 10)
        self.address = (self.HOST, self.PORT+1)
        self.sendAddress = (self.HOST, self.PORT)

        self.sForRecv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.sForRecv.bind(self.address)

        self.sForSend = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

        self.timer = threading.Timer(timeout, self.reportTimeout)
        self.recvEvent = threading.Event()
        self.recvEvent.clear()
        self.processEvent = threading.Event()
        self.processEvent.clear()
        self.timeoutEvent = threading.Event()
        self.timeoutEvent.clear()
        self.thread = threading.Thread(target=self.recv, args=())
        self.thread.start()

    def send(self):

        frameExpextedToRecv = 0

        sn = 0
        ack = 0

        i_data = 0

        bufferSize = 0
        buffer = [-1 for _ in range(MAX_BUFSIZE+1)]

        data = [b'package1', b'package2', b'package3', b'package4']
        self.timer.start()
        flag=0
        while True:
            if flag:
                break
            if self.recvCount >= dataLen and self.sendCount >= dataLen:
                count = 0
                while True and count < 20:
                    time.sleep(0.5)                    
                    if self.recvEvent.is_set() :
                        package = self.pack(b'', sn, frameExpextedToRecv)
                        print('ack to send ', frameExpextedToRecv)
                        self.sForSend.sendto(package, self.sendAddress)
                        count += 1
                break

            # print('---------------------------------------')
            elif self.recvEvent.is_set():
                print('---------------------------------------')
                self.recvEvent.clear()
                if self.check(self.recvBuf, frameExpextedToRecv):
                    self.timer.cancel()
                    self.timer = threading.Timer(timeout, self.reportTimeout)
                    self.timer.start()
                    print('right frame sn ', frameExpextedToRecv)
                    print(self.unpack(self.recvBuf))
                    self.sendCount += (self.recvBuf[-3] -
                                       ack+MAX_SEQ+1) % (MAX_SEQ+1)
                    ack = self.recvBuf[-3]
                    bufferSize = (sn-ack+MAX_SEQ+1) % (MAX_SEQ+1)
                    print('ack get ', ack)
                    frameExpextedToRecv = (frameExpextedToRecv+1) % (MAX_SEQ+1)
                    self.recvCount += 1

                elif self.checkCRC(self.recvBuf):
                    print('wrong sn')
                    self.sendCount += (self.recvBuf[-3] -
                                       ack+MAX_SEQ+1) % (MAX_SEQ+1)
                    ack = self.recvBuf[-3]
                    bufferSize = (sn-ack+MAX_SEQ+1) % (MAX_SEQ+1)
                    print('ack get ', ack)
                else:
                    print('wrong frame')
                self.processEvent.set()
            elif self.timeoutEvent.is_set():
                print('---------------------------------------')
                self.timeoutEvent.clear()
                self.timer.cancel()
                self.timer = threading.Timer(timeout, self.reportTimeout)
                self.timer.start()
                for i in range(bufferSize):
                    package = self.pack(
                        data[buffer[ack+i]], ack+i, frameExpextedToRecv)
                    print('frame to send ', ack+i, ' data no ', buffer[ack+i])
                    rand = random.random()
                    if rand < lostRate:
                        print('frame lose')
                    elif rand < lostRate+errorRate:
                        print('frame error')
                        package = self.errorSimulation(package)
                        self.sForSend.sendto(package, self.sendAddress)
                    else:
                        self.sForSend.sendto(package, self.sendAddress)
                    time.sleep(timeForSend)
            elif bufferSize < MAX_BUFSIZE and i_data < len(data):
                print('---------------------------------------')
                package = self.pack(data[i_data], sn, frameExpextedToRecv)
                print('frame to send ', sn, ' data no ', i_data)
                buffer[sn] = i_data
                sn = (sn+1) % (MAX_SEQ+1)
                bufferSize += 1
                i_data += 1
                rand = random.random()
                if rand < lostRate:
                    print('frame lose')
                elif rand < lostRate+errorRate:
                    print('frame error')
                    package = self.errorSimulation(package)
                    self.sForSend.sendto(package, self.sendAddress)
                else:
                    self.sForSend.sendto(package, self.sendAddress)
                time.sleep(timeForSend)
            if not self.recvEvent.is_set():
                self.processEvent.set()
        self.thread.join()

    def recv(self):  # 不读入，可能是同步问题
        while True:
            # print('recv')
            if self.recvCount == dataLen and self.sendCount == dataLen:
                break
            self.recvBuf, _ = self.sForRecv.recvfrom(1024)
            self.recvEvent.set()

            self.processEvent.wait()
            self.processEvent.clear()

    def pack(self, data, sn, ack):
        package = bytearray([sn])+data+bytearray([ack])
        msg = int.from_bytes(package, 'big')
        msg, msgl, _ = getSendMassage(msg, len(package)*8)
        package = msg.to_bytes(msgl//8, 'big')
        return package

    def errorSimulation(self, package):
        return package+b'0'

    def reportTimeout(self):
        self.timeoutEvent.set()

    def unpack(self, package):
        return package[1:-3]

    def checkCRC(self, package):
        msg = int.from_bytes(package, 'big')
        isSuccess, _ = checkReceiveMassage(msg, len(package)*8)
        if not isSuccess:
            return False
        else:
            return True

    def check(self, package, sn):
        msg = int.from_bytes(package, 'big')
        isSuccess, _ = checkReceiveMassage(msg, len(package)*8)
        if not isSuccess:
            return False
        if package[0] != sn:
            return False
        else:
            return True


if __name__ == "__main__":
    s = host1()
    s.init()
    s.send()
