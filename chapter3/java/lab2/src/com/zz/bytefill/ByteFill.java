package com.zz.bytefill;

/**
 * @author Zhang Zhen
 * @time 2019年4月18日 下午9:13:24
 */
public class ByteFill {
	public static String byteFill(String target, String flag, String esc) {
		return target.replaceAll(esc, esc + esc).replaceAll(flag, esc + flag);
	}

}
