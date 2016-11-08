package com.rjxx.comm.utils;

import java.security.MessageDigest;

public class MD5Util {
	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	/** * 把inputString加密 
	 * @throws Exception */
	public static String generatePassword(String inputString) throws Exception {
		return encodeByMD5(inputString);
	}

	/**
	 * 验证输入的密码是否正确
	 * 
	 * @param password
	 *            加密后的密码
	 * @param inputString
	 *            输入的字符串
	 * @return 验证结果，TRUE:正确 FALSE:错误
	 * @throws Exception 
	 */
	public static boolean validatePassword(String password, String inputString) throws Exception {
		if (password.equals(encodeByMD5(inputString))) {
			return true;
		} else {
			return false;
		}
	}

	/** 对字符串进行MD5加密 
	 * @throws Exception */
	private static String encodeByMD5(String originString) throws Exception {
		if (originString != null) {
			try {
				// 创建具有指定算法名称的信息摘要
				MessageDigest md = MessageDigest.getInstance("MD5");
				// 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
				byte[] results = md.digest(originString.getBytes());
				// 将得到的字节数组变成字符串返回
				String resultString = byteArrayToHexString(results);
				return resultString.toUpperCase();
			} catch (Exception ex) {
				throw ex;
			}
		}
		return null;
	}

	/**
	 * 转换字节数组为十六进制字符串
	 * 
	 * @param 字节数组
	 * @return 十六进制字符串
	 */
	private static String byteArrayToHexString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}

	/** 将一个字节转化成十六进制形式的字符串 */
	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}
    
    public static void main(String[] args) throws Exception {
    	MD5Util md5 = new MD5Util();
		String pwd1 = "12345678";
		String pwd2 = "";
		System.out.println("未加密之前：" + pwd1);
		pwd2 = md5.generatePassword(pwd1);
		System.out.println("加密之后：" + pwd2);
		if (md5.validatePassword(pwd2, pwd1)) {
			System.out.println("密码正确！");
		} else {
			System.out.println("密码错误!");
		}
	}
}
