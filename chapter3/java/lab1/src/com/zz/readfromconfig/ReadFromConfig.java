package com.zz.readfromconfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Zhang Zhen
 * @time 2019年4月14日 下午12:24:36
 */
public class ReadFromConfig {

	public static String[] Read(String path) throws IOException {
		String[] all = new String[3];
		BufferedReader in = new BufferedReader(new FileReader(path));
		String string;
		int cnt = 0;
		while ((string = in.readLine()) != null) {
			all[cnt++] = string;
		}
		in.close();
		return all;
	}
}
