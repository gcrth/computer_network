f=open('config001','r')
line=f.readline()
InfoString1=line.split('=')[1][:-1]
line=f.readline()
GenXString=line.split('=')[1][:-1]
line=f.readline()
InfoString2=line.split('=')[1][:-1]
f.close()

len1=len(InfoString1)
len2=len(InfoString2)
genLen=len(GenXString)

infoValue1=int(InfoString1,2)
genValue=int(GenXString,2)
infoValue2=int(InfoString2,2)

def getRemainder(value,valueL,gen,genL):
    pass
    res=value>>(valueL-genL)
    for i in range(valueL-genL+1):
        if(res>>(genL-1)):
            res^=gen
        if i!=valueL-genL:
            res<<=1
            res&=(~(1<<17))
            res|=((value&(1<<(valueL-genL-1-i)))>>(valueL-genL-1-i))
    return res


def addTail(value,genL):
    res=value
    return res<<(genL-1)

def getSendMassage(info,infoL,gen=genValue,genL=genLen):
    massage=addTail(info,genL)
    massageL=infoL+(genL-1)
    remainder=getRemainder(massage,massageL,gen,genL)
    massage^=remainder
    return massage,massageL,remainder

def checkReceiveMassage(massage,massageL,gen=genValue,genL=genLen):
    remainder=getRemainder(massage,massageL,gen,genL)
    if remainder==0:
        return True,remainder
    else:
        return False,remainder

if __name__ == "__main__":

    print('InfoString1')
    print(InfoString1)
    print('GenXString')
    print(GenXString)
    massage,msgL,remainder=getSendMassage(infoValue1,len1,genValue,genLen)
    print('CRC_Code')
    print(format(remainder, 'b'))
    print('frame')
    for i in range(msgL-len(format(massage, 'b'))):
        print('0',end='')
    print(format(massage, 'b'))
    print('------------------------------------------------')
    print('InfoString2')
    print(InfoString2)
    print('GenXString')
    print(GenXString)
    isSuccess,remainder=checkReceiveMassage(infoValue2,len2,genValue,genLen)
    print('remainder')
    print(format(remainder,'b'))
    print('isSuccess')
    print(isSuccess)
