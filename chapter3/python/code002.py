import math

flag=0

f=open('config002','r')
line=f.readline()
InfoString1=line.split('=')[1][:-1]

line=f.readline()
FlagString1=line.split('=')[1][:-1]

line=f.readline()
InfoString2=line.split('=')[1][:-1]

line=f.readline()
FlagString2=line.split('=')[1][:-1]

line=f.readline()
EscString=line.split('=')[1][:-1]

f.close()

len1=len(InfoString1)
len2=len(InfoString2)



print('InfoString1')
print(InfoString1)
print('FlagString1')
print(FlagString1)

InfoStringAfterFill=''

count=0
for i in InfoString1:
    if(i=='0'):
        count=0
        InfoStringAfterFill+='0'
    else:
        count+=1
        if count==5:
            count=0
            InfoStringAfterFill+='1'
            InfoStringAfterFill+='0'
        else:
            InfoStringAfterFill+='1'

print('InfoStringAfterFill')
print(InfoStringAfterFill)

InfoString=''
for i in range(len(InfoStringAfterFill)):
    if flag==1:
        flag=0
        continue
    if(InfoStringAfterFill[i]=='0'):
        count=0
        InfoString+='0'
    else:
        count+=1
        if count==5:
            count=0
            InfoString+='1'
            if(InfoStringAfterFill[i+1]=='0'):
                flag=1
        else:
            InfoString+='1'
print('InfoString')
print(InfoString)

print('------------------------------------------------')

print('InfoString2')
print(InfoString2)
print('FlagString2')
print(FlagString2)
print('EscString')
print(EscString)

InfoStringbyte=bytearray()
for i in range(math.ceil(len2/2)):
    s=InfoString2[i*2:i*2+2][::-1]
    InfoStringbyte+=bytes([int(s,16)])

flagByte=bytearray([int(FlagString2[::-1],16)])
escByte=bytearray([int(EscString[::-1],16)])

InfoStringAfterFillbyte=bytearray()
for i in range(len(InfoStringbyte)):
    if InfoStringbyte[i]==flagByte[0]:
        InfoStringAfterFillbyte+=escByte[0:1]
        InfoStringAfterFillbyte+=flagByte[0:1]
    elif InfoStringbyte[i]==escByte[0]:
        InfoStringAfterFillbyte+=escByte[0:1]
        InfoStringAfterFillbyte+=escByte[0:1]
    else:
        InfoStringAfterFillbyte+=InfoStringbyte[i:i+1]

InfoStringAfterFill=''
for i in range(len(InfoStringAfterFillbyte)):
    InfoStringAfterFill+='{:02X}'.format(InfoStringAfterFillbyte[i])[::-1]

print('InfoStringAfterFill')
print(InfoStringAfterFill)

InfoStringbyte=bytearray()
for i in range(len(InfoStringAfterFillbyte)):
    if flag==1:
        flag=0
        continue
    if InfoStringAfterFillbyte[i]==escByte[0]:
        InfoStringbyte+=InfoStringAfterFillbyte[i+1:i+2]
        flag=1
    elif InfoStringAfterFillbyte[i]==flagByte[0]:
        print('error')
    else:
        InfoStringbyte+=InfoStringAfterFillbyte[i:i+1]

InfoString=''
for i in range(len(InfoStringbyte)):
    InfoString+='{:02X}'.format(InfoStringbyte[i])[::-1]

print('InfoString')
print(InfoString)

