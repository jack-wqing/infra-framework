package com.jindi.infra.tools.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptUtils {

	private static final byte[] KEY = Base64.getDecoder().decode("y9gIcVeCO2+9kL5SNttmxQ==");

	private static SymmetricCrypto defaultAES = new SymmetricCrypto(SymmetricAlgorithm.AES, KEY);

	/**
	 * AES加密
	 *
	 * @param value
	 * @return
	 */
	public static String encrypt(String value) {
		try {
			return defaultAES.encryptHex(value);
		} catch (Exception e) {
			log.error("encrypt value = {}", value, e);
		}
		return value;
	}

	/**
	 * AES解密
	 *
	 * @param value
	 * @return
	 */
	public static String decrypt(String value) {
		try {
			return defaultAES.decryptStr(value, StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("decrypt = {}", value, e);
		}
		return value;
	}

	/**
	 * AES加密
	 *
	 * @param value
	 * @return
	 */
	public static String encrypt(String key, String value) {
		try {
			SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, Base64.getDecoder().decode(key));
			return aes.encryptHex(value);
		} catch (Exception e) {
			log.error("encrypt value = {}", value, e);
		}
		return value;
	}

	/**
	 * AES解密
	 *
	 * @param value
	 * @return
	 */
	public static String decrypt(String key, String value) {
		try {
			SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, Base64.getDecoder().decode(key));
			return aes.decryptStr(value, StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("decrypt = {}", value, e);
		}
		return value;
	}

	/**
	 * 生成密钥的工具类
	 *
	 * @return
	 */
	public static String generateKey() {
		byte[] bytes = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
		return Base64.getEncoder().encodeToString(bytes);
	}
}