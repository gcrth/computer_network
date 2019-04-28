package com.zz.receive;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import com.zz.crc.CCITT;
import com.zz.readfromconfig.ReadFromConfig;

/**
 * @author Zhang Zhen
 * @time 2019年4月19日 下午5:03:10
 */
public class UDPRecevie {

	private int port;
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacketSend;
	private DatagramPacket datagramPacketRecv;
	private byte[] bufRecv;

	private static final int RECV_SIZE = 1024;

	public UDPRecevie() throws Exception {
		ArrayList<String> configInfo = ReadFromConfig.Read(System.getProperty("user.dir") + "\\config\\config.txt");
		port = Integer.parseInt(configInfo.get(0));
	}

	public UDPRecevie init() throws Exception {
		bufRecv = new byte[RECV_SIZE];
		datagramSocket = new DatagramSocket(port);
		return this;
	}

	public void sendAndRecv() throws IOException {
		int sn = 0;
		while (true) {
			System.out.println("-------------------------");
			System.out.println("frame expected: " + sn);
			datagramPacketRecv = new DatagramPacket(bufRecv, RECV_SIZE);
			datagramSocket.receive(datagramPacketRecv);
			InetAddress inetAddress = datagramPacketRecv.getAddress();
			int port = datagramPacketRecv.getPort();
			String frameRecevie = new String(datagramPacketRecv.getData(), 0, datagramPacketRecv.getLength());
			int seq = frameRecevie.charAt(0) - '0';
			int snRecv = frameRecevie.charAt(1) - '0';
			String info = frameRecevie.substring(2, frameRecevie.length());
			String crcCode = CCITT.calCCITT(info);
			if (crcCode.contains("1") || snRecv != sn) {
				System.out.println("wrong frame");
				System.out.println("ack send " + sn);
				String ack = String.valueOf(sn);
				datagramPacketSend = new DatagramPacket(ack.getBytes(), ack.length(), inetAddress, port);
				datagramSocket.send(datagramPacketSend);
			} else {
				System.out.println("right frame sn " + sn + " no " + seq);
				System.out.println("info " + info.substring(0, info.length() - 16));
				sn = (sn + 1) % 2;
				String ack = String.valueOf(sn);
				datagramPacketSend = new DatagramPacket(ack.getBytes(), ack.length(), inetAddress, port);
				datagramSocket.send(datagramPacketSend);
				System.out.println("ack send " + sn);
			}
			datagramPacketRecv.setLength(RECV_SIZE);
		}
	}

	public static void main(String[] args) throws Exception {
		UDPRecevie recevie = new UDPRecevie();
		recevie.init().sendAndRecv();
	}
}
