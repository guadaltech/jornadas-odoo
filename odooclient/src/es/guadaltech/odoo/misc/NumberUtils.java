package es.guadaltech.odoo.misc;

import java.io.UnsupportedEncodingException;

import android.util.Base64;

public class NumberUtils {

	public static boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isDouble(String text) {
		try {
			Double.parseDouble(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static String decode(String toDecode) {
		byte[] byteDecode = Base64.decode(toDecode, Base64.DEFAULT);
		String s = null;
		try {
			s = new String(byteDecode, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}

	public static String encode(String toEncode) {
		String s = null;
		try {
			byte[] byteEncode = toEncode.getBytes("UTF-8");
			s = Base64.encodeToString(byteEncode, Base64.DEFAULT);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}

}
