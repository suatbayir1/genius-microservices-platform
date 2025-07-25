package com.iotiq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseDTO{
    private UUID uuid;
    private UUID authUuid;
    private String userName;
    private String email;
    private String name;
    private String phone;
    private String avatar;
}
