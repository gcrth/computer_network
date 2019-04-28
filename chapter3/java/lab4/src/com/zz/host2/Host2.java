package com.zz.host2;
/**
*@author Zhang Zhen
*@time 2019年4月20日 下午11:25:29
*/

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.zz.crc.CCITT;
import com.zz.readfromconfig.ReadFromConfig;

public class Host2 {
	private static final int WINDOW_SIZE = 4;
	private static final int SEQ_BIT = 6;
	private static int MAX_NUM_DATA = 20;
	private static String CRC_ADD = "0000000000000000";
	private static volatile AtomicInteger frameCnt = new AtomicInteger(0);
	private static BlockingQueue<Integer> slideWindow = new ArrayBlockingQueue<>(WINDOW_SIZE);
	private static BlockingQueue<Long> timeCount = new ArrayBlockingQueue<>(MAX_NUM_DATA * 3);
	private static BlockingQueue<Integer> QueueOfAckNeedSend = new ArrayBlockingQueue<>(MAX_NUM_DATA * 3);
	private static volatile int ackExp = 0;
	private static volatile boolean timeOut = false;
	private volatile int port;
	private volatile InetAddress inetAddress;
	private double errorRate;
	private double lostRate;
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacketSend;
	private DatagramPacket datagramPacketRecv;
	private byte[] bufRecv;
	private static final long TIMEOUT = 1000;
	private static final int RECV_SIZE = 1024;
	@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
	private static final ArrayList<String> frames = new ArrayList() {
		{
			add("00001");
			add("00010");
			add("00100");
			add("01000");
			add("10000");
		}
	};

	public Host2() throws Exception {
		init();
	}

	public Host2 init() throws Exception {
		ArrayList<String> configInfo = ReadFromConfig.read(System.getProperty("user.dir") + "\\config\\config.txt");
		inetAddress = null;
		port = Integer.parseInt(configInfo.get(0)) + 1;
		errorRate = 0.1 / Double.parseDouble(configInfo.get(1));
		lostRate = 0.1 / Double.parseDouble(configInfo.get(2));
		bufRecv = new byte[RECV_SIZE];
		datagramSocket = new DatagramSocket(port);
		return this;
	}

	public String errorSimulation(String frameToSend) {
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

	public void send() throws IOException, Exception {
		int seqNeedSend = 0;
		String infoNeedSend = "";
		String ackNeedSend = "";
		String frameNeedSend = "";
		boolean finished = false;
		boolean lost = false;
		while (!finished) {
			if (inetAddress == null) {
				continue;
			}
			double randomRate = Math.random();
			ackNeedSend = (QueueOfAckNeedSend.isEmpty() || randomRate < lostRate) ? "x"
					: String.valueOf(QueueOfAckNeedSend.poll());
			if (frameCnt.get() < MAX_NUM_DATA) {
				lost = false;
				slideWindow.put(frameCnt.get() % (SEQ_BIT + 1));
				if (timeOut) {
					timeOut = false;
					slideWindow.clear();
					timeCount.clear();
					continue;
				}
				timeCount.add(System.currentTimeMillis());
				seqNeedSend = frameCnt.get() % (SEQ_BIT + 1);
				infoNeedSend = frames.get(frameCnt.get() % frames.size());
				frameNeedSend = String.valueOf(seqNeedSend) + ackNeedSend + infoNeedSend
						+ CCITT.calCCITT(infoNeedSend + CRC_ADD);

				if (randomRate < lostRate) {
					System.out.println("frame lose");
					lost = true;
				} else if (randomRate < errorRate) {
					System.out.println("frame error");
					frameNeedSend = errorSimulation(frameNeedSend);
				} else {
				}

				System.out.println("The frame being sent: " + seqNeedSend);
				frameCnt.incrementAndGet();
				System.out.println("Next frame to send: " + frameCnt.get() % (SEQ_BIT + 1));
			} else if (!QueueOfAckNeedSend.isEmpty()) {
				frameNeedSend = "s" + ackNeedSend + "0";
			} else {
				finished = true;
			}
			if (ackNeedSend != "x") {
				System.out.println("The ack being sent: " + ackNeedSend);
			}
			if (!finished && !lost) {
				datagramPacketSend = new DatagramPacket(frameNeedSend.getBytes(), frameNeedSend.length(), inetAddress,
						8888);
				datagramSocket.send(datagramPacketSend);
			}
			Thread.currentThread();
			Thread.sleep(100);
		}
	}

	public void recevie() throws IOException, Exception {
		int seqExp = 0;
		int seqRecevied = 0;
		int ackRecevied = 0;
		String infoRecevied = "";
		String frameRecevied = "";
		String crcCodeOfRecevied = "";
		while (true) {
			System.out.println("The frame expected: " + seqExp);
			if (!slideWindow.isEmpty()) {
				System.out.println("The ack expected: " + ackExp);
			}
			datagramPacketRecv = new DatagramPacket(bufRecv, RECV_SIZE);
			datagramSocket.receive(datagramPacketRecv);
			if (inetAddress == null) {
				inetAddress = datagramPacketRecv.getAddress();
			}
			frameRecevied = new String(datagramPacketRecv.getData(), 0, datagramPacketRecv.getLength());
			seqRecevied = frameRecevied.charAt(0) - '0';
			ackRecevied = frameRecevied.charAt(1) - '0';
			infoRecevied = frameRecevied.substring(2, frameRecevied.length());
			if (frameRecevied.charAt(1) != 'x') {
				if (ackExp == ackRecevied) {
					System.out.println("The ack of right frame recevied: " + ackRecevied);
					slideWindow.poll();
					timeCount.poll();
					ackExp = slideWindow.isEmpty() ? (ackExp + 1) % (SEQ_BIT + 1) : slideWindow.peek();
				}
			}
			if (frameRecevied.charAt(0) != 's') {
				crcCodeOfRecevied = CCITT.calCCITT(infoRecevied);
				if (!crcCodeOfRecevied.contains("1")) {
					if (seqExp == seqRecevied) {
						System.out.println("The seq of right frame recevied: " + seqRecevied);
						QueueOfAckNeedSend.add(seqExp);
						seqExp = (++seqExp) % (SEQ_BIT + 1);
					}
				}
			}
			datagramPacketRecv.setLength(RECV_SIZE);
			Thread.currentThread();
			Thread.sleep(100);
		}
	}

	public void timecount() throws InterruptedException {
		int temp = 0, temp2 = 0;
		while (true) {
			try {
				while (System.currentTimeMillis() - timeCount.peek() < TIMEOUT) {
				}
			} catch (NullPointerException e) {
				continue;
			}
			slideWindow.clear();
			timeCount.clear();
			temp = frameCnt.get() / (SEQ_BIT + 1);
			temp2 = (temp == 0) ? 0 : (temp - 1);
			frameCnt = new AtomicInteger(temp2 * (SEQ_BIT + 1) + ackExp);
			timeOut = true;
			//System.out.println("Time out! " + ackExp);
			Thread.currentThread();
			Thread.sleep(100);
		}
	}

	public static void main(String[] args) throws Exception {
		Host2 host2 = new Host2();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					host2.timecount();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					host2.recevie();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		host2.send();
	}
}
