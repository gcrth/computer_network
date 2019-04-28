package com.zz.bitfill;

/**
 * @author Zhang Zhen
 * @time 2019年4月18日 下午8:41:24
 */

public class BitFill {
	/**
	 * 
	 * @param target
	 * 		要发送的数据，即需要进行零比特填充的数据
	 * @return
	 * 		比特填充后的数据，即收到的数据
	 */
	public static String bitFill(String target) {
		return target.replaceAll("11111", "111110");
	}
}
