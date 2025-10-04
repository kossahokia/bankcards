// AssignRoleRequest.java
package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleRequest {
    @NotNull
    private Long userId;

    @NotBlank
    private String roleName;
}
