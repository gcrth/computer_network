package com.zz.main;

import java.io.IOException;

import com.zz.crc.CCITT;
import com.zz.readfromconfig.ReadFromConfig;

/**
 * @author Zhang Zhen
 * @time 2019��4��9�� ����5:08:48
 */
public class Main {
	private static String InfoString1;
	private static String InfoString2;
	private static String BinaryOfCCITT;
	private static int length;

	public Main() throws IOException {
		init();
	}

	// Initial
	public static void init() throws IOException {
		// Read args from Config.txt
		String[] arguments = ReadFromConfig.Read(System.getProperty("user.dir") + "\\config\\config.txt");

		InfoString1 = arguments[0];
		System.out.println("�����͵�������Ϣ�����Ʊ��ش�InforString1: " + InfoString1);

		BinaryOfCCITT = arguments[1];
		length = BinaryOfCCITT.length();
		System.out.println("CRC-CCITT��Ӧ�Ķ����Ʊ��ش�: " + BinaryOfCCITT);

		InfoString2 = arguments[2];
	}

	public static void cal() throws Exception {
		// Calculate Crc_Code of InforString1
		String Add = "";
		for (int i = 0; i < length - 1; i++) {
			Add += "0";
		}
		CCITT ccitt1 = new CCITT(BinaryOfCCITT);
		String CRC_Code1 = ccitt1.Cal_CCITT(InfoString1 + Add);
		System.out.println("ѭ������У����CRC-Code1: " + CRC_Code1);

		// Get CheckSumFrame
		String CheckSumFrame = InfoString1 + CRC_Code1;
		System.out.println("��У��͵ķ���֡: " + CheckSumFrame);
	}

	public static void cal2() throws Exception {
		// Calculate Crc_Code of InforString2
		String Add = "";
		for (int i = 0; i < length - 1; i++) {
			Add += "0";
		}
		CCITT ccitt2 = new CCITT(BinaryOfCCITT);
		String CRC_Code2 = ccitt2.Cal_CCITT(InfoString2 + Add);
		System.out.println("ѭ������У����CRC-Code2: " + CRC_Code2);
	}

	public static void main(String[] args) throws Exception {
		init();
		// Calculate Crc_Code of InforString1
		cal();
		System.out.println("���յ�������Ϣ�����Ʊ��ش�InforString2: " + InfoString2);
		// Calculate Crc_Code of InforString2
		cal2();
	}

}
