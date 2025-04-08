package com.jindi.infra.grpc;

import lombok.Data;

/**
 * 安全配置
 */
@Data
public class SecurityProperties {

	private String certChain = "/certificate.pem";

	private String privateKey = "/cert.key";

	private Boolean enable = Boolean.FALSE;
}
