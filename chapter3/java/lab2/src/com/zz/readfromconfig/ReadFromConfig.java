package com.zz.readfromconfig;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Zhang Zhen
 * @time 2019年4月14日 下午12:24:36
 */
public class ReadFromConfig {

	public static ArrayList<String> Read(String path) throws IOException {
		ArrayList<String> info = new ArrayList<>();
		BufferedReader in = new BufferedReader(new FileReader(path));
		String string;
		while ((string = in.readLine()) != null) {
			info.add(string);
		}
		in.close();
		return info;
	}
}
