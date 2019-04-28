package com.zz.sender;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Random;

import com.zz.crc.CCITT;
import com.zz.readfromconfig.ReadFromConfig;

/**
 * @author Zhang Zhen
 * @time 2019年4月19日 下午5:02:21
 */

public class UDPSender {
	private int port;
	private double errorRate;
	private double lostRate;
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacketSend;
	private DatagramPacket datagramPacketRecv;
	private InetAddress inetAddress;
	private byte[] bufRecv;
	private static final int TMEOUT = 500;
	private static final int RECV_SIZE = 1024;
	@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
	private static final ArrayList<String> fileBitToSend = new ArrayList() {
		{
			add("00000");
			add("00001");
			add("00010");
			add("00100");
			add("01000");
			add("10000");
		}
	};

	/**
	 * 
	 * @throws Exception
	 */
	public UDPSender() throws Exception {
		ArrayList<String> configInfo = ReadFromConfig.Read(System.getProperty("user.dir") + "\\config\\config.txt");
		port = Integer.parseInt(configInfo.get(0));
		errorRate = 1.0 / Double.parseDouble(configInfo.get(1));
		errorRate = 1.0 / Double.parseDouble(configInfo.get(2));
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public UDPSender init() throws Exception {
		bufRecv = new byte[RECV_SIZE];
		datagramSocket = new DatagramSocket();
		datagramSocket.setSoTimeout(TMEOUT);
		inetAddress = InetAddress.getLocalHost();
		return this;
	}

	/**
	 * 
	 * @param frameToSend
	 * @return
	 */
	public String changeOneBit(String frameToSend) {
		StringBuilder stringBuilder = new StringBuilder(frameToSend);
		int reverseIndex = 2 + new Random().nextInt(6 - 2 + 1);
		char x = frameToSend.charAt(reverseIndex);
		if (x == '1')
			x = '0';
		else
			x = '1';
		stringBuilder.setCharAt(reverseIndex, x);
		frameToSend = stringBuilder.toString();
		return frameToSend;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public void sendAndRecv() throws IOException {
		int sn = 0;
		for (int seq = 0; seq < fileBitToSend.size(); seq++) {
			System.out.println("-------------------------");
			while (true) {
				try {
					String info = fileBitToSend.get(seq);
					// 发送的帧 = seq(1位) + sn(1位) + info(?位) + CRC_Code(16位);
					String frameToSend = String.valueOf(seq) + String.valueOf(sn) + info
							+ CCITT.calCCITT(info + "0000000000000000");
					double randomRate = Math.random();
					System.out.println("frame to send " + sn + " data no " + seq);
					if (randomRate < errorRate) {
						System.out.println("frame error");
						frameToSend = changeOneBit(frameToSend);
						datagramPacketSend = new DatagramPacket(frameToSend.getBytes(), frameToSend.length(),
								inetAddress, port);
						datagramSocket.send(datagramPacketSend);
					} else if (randomRate < lostRate + errorRate) {
						System.out.println("frame lose");
					} else {
						datagramPacketSend = new DatagramPacket(frameToSend.getBytes(), frameToSend.length(),
								inetAddress, port);
						datagramSocket.send(datagramPacketSend);
					}
					datagramPacketRecv = new DatagramPacket(bufRecv, RECV_SIZE);
					datagramSocket.receive(datagramPacketRecv);
					String ack = new String(datagramPacketRecv.getData(), 0, datagramPacketRecv.getLength());
					if (Integer.parseInt(ack) == (sn + 1) % 2) {
						sn = (sn + 1) % 2;
						System.out.println("ack get " + sn);
						break;
					} else {
						System.out.println("wrong sn " + sn + " no " + seq);
					}
					datagramPacketRecv.setLength(RECV_SIZE);
				} catch (SocketTimeoutException e) {
					System.out.println("time out sn " + sn + " no " + seq);
				}
			}
		}
		datagramSocket.close();

	}

	public static void main(String[] args) throws Exception {
		UDPSender sender = new UDPSender();
		sender.init().sendAndRecv();
	}
}
