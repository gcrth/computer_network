package com.zz.bitfill;

/**
 * @author Zhang Zhen
 * @time 2019��4��18�� ����8:41:24
 */

public class BitFill {
	/**
	 * 
	 * @param target
	 * 		Ҫ���͵����ݣ�����Ҫ�����������������
	 * @return
	 * 		������������ݣ����յ�������
	 */
	public static String bitFill(String target) {
		return target.replaceAll("11111", "111110");
	}
}
