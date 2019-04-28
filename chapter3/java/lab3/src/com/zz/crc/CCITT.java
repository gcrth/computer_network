package com.zz.crc;

/**
 * @author Zhang Zhen
 * @time 2019年4月9日 下午2:36:39
 */

public class CCITT {

	private static final String GEN_X_STRING = "10001000000100001";

	public static String calCCITT(String string) {
		return div(string.toCharArray(), GEN_X_STRING.toCharArray());
	}

	public static String div(char[] stringToCharArray, char[] genXStringToCharArray) {
		int index = 0;
		int p;
		while (true) {
			index = 0;
			for (int i = 0; i < stringToCharArray.length; i++) {
				index = i;
				if (stringToCharArray[i] == '1')
					break;
			}
			if (genXStringToCharArray.length > stringToCharArray.length - index)
				break;
			p = index;
			for (int i = 0; i < genXStringToCharArray.length; i++) {
				stringToCharArray[p] = stringToCharArray[p] == genXStringToCharArray[i] ? '0' : '1';
				p++;
			}
		}
		String result = "";
		for (int i = stringToCharArray.length - genXStringToCharArray.length + 1; i < stringToCharArray.length; i++)
			result += stringToCharArray[i];
		return result;
	}

}
