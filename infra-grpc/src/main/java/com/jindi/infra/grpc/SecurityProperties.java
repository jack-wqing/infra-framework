package com.jindi.infra.grpc;

import lombok.Data;

@Data
public class SecurityProperties {

	private String certChain = "/certificate.pem";

	private String privateKey = "/cert.key";

	private Boolean enable = Boolean.FALSE;
}
