package com.zz.main;

import java.util.ArrayList;

import com.zz.bitfill.BitFill;
import com.zz.bytefill.ByteFill;
import com.zz.readfromconfig.ReadFromConfig;

/**
 * @author Zhang Zhen
 * @time 2019年4月18日 下午8:40:47
 */
public class Main {

	private static String infoString1;
	private static String flag1;

	private static String infoString2;
	private static String flag2;
	private static String esc;

	public static void init1(ArrayList<String> info) {
		infoString1 = info.get(0);
		flag1 = info.get(1);
	}

	public static void init2(ArrayList<String> info) {
		infoString2 = info.get(0);
		flag2 = info.get(1);
		esc = info.get(2);
	}

	public static void main(String[] args) throws Exception {
		// bitFill
		init1(ReadFromConfig.Read(System.getProperty("user.dir") + "\\config\\bitFillConfig.txt"));
		// printInfo
		System.out.println("------bitFill------");
		System.out.println("帧数据信息为： " + infoString1);
		System.out.println("帧起始和结束标志： " + flag1);
		System.out.println("比特填充后的发送帧： " + BitFill.bitFill(infoString1));
		System.out.println("比特删除后的发送帧： " + infoString1);

		// byteFill
		init2(ReadFromConfig.Read(System.getProperty("user.dir") + "\\config\\byteFillConfig.txt"));
		// printInfo
		System.out.println("------byteFill------");
		System.out.println("帧数据信息为： " + infoString2);
		System.out.println("帧起始和结束标志： " + flag2);
		System.out.println("字节填充后的发送帧： " + ByteFill.byteFill(infoString2, flag2, esc));
		System.out.println("字节删除后的发送帧： " + infoString2);

	}

}
