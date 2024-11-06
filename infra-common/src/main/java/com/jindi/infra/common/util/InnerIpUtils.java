package com.jindi.infra.common.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InnerIpUtils {

	private static AtomicReference<String> cached = new AtomicReference<>();
	private static InetAddress inet = getInet();

	private static InetAddress getInet() {
		InetAddress inet = null;
		try {
			inet = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			log.error("getLocalAddress error", e);
		}
		return inet;
	}

	public static String getCachedIP() {
		String ip = cached.get();
		if (StringUtils.isNotBlank(ip)) {
			return ip;
		}
		return getIP();
	}

	public static String getIP() {
		String ip = getInnerIP();
		if (StringUtils.isNotBlank(ip)) {
			cached.set(ip);
		}
		return ip;
	}

	/**
	 * 获得本机IP地址
	 */
	private static String getInnerIP() {
		// 和运维同学沟通 华为云获取本机ip不再依赖hostname配置
		// if (InnerOSUtils.isLinux()) {
		// if (inet != null) {
		// return inet.getHostAddress();
		// }
		// }
		try {
			InetAddress candidateAddress = null; // 遍历所有的网络接口
			Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface face = (NetworkInterface) interfaces.nextElement(); // 在所有的接口下再遍历IP
				Enumeration inetAddrs = face.getInetAddresses();
				while (inetAddrs.hasMoreElements()) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) { // 排除loopback类型地址
						if (inetAddr.isSiteLocalAddress()) { // 如果是site-local地址，就是它了
							return inetAddr.getHostAddress();
						} else if (candidateAddress == null) { // site-local类型的地址未被发现，先记录候选地址
							candidateAddress = inetAddr;
						}
					}
				}
			}
			if (candidateAddress != null) {
				return candidateAddress.getHostAddress();
			}
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost(); // 如果没有发现 non-loopback地址.只能用最次选的方案
			return jdkSuppliedAddress.getHostAddress();
		} catch (SocketException | UnknownHostException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	/**
	 * 获得主机名
	 */
	public static String getHostName() {
		if (inet != null) {
			return inet.getHostName();
		}
		return null;
	}
}
