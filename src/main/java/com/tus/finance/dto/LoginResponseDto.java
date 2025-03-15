package com.tus.finance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDto {
	private long userId;
	
	private String token;
	
	private String role;
}
