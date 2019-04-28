package com.zz.crc;

/**
 * @author Zhang Zhen
 * @time 2019年4月9日 下午2:36:39
 */

public class CCITT {

	private String GenXString;

	public CCITT(String GenXString) {
		this.GenXString = GenXString;
	}

	public String Cal_CCITT(String string) {
		return Div(string.toCharArray(), GenXString.toCharArray());
	}

	public String Div(char[] stringToCharArray, char[] genXStringToCharArray) {
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
