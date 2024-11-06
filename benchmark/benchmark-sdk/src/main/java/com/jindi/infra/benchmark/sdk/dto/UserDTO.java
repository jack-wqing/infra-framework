package com.jindi.infra.benchmark.sdk.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO implements Serializable {

	private Long id;
	private String username;
	private String password;
	private String realName;
	private String address;
	private String email;
	private String tag;
	private Boolean status;
	private Role role;
	private Long createTime;
	private Long updateTime;
	private List<LabelDTO> labelDTO;

	public static enum Role implements Serializable {
		COMMON, ADMIN, SUPER_ADMIN;
	}

	@Data
	public static class LabelDTO implements Serializable {
		private Integer no;
		private String name;
	}
}
