package com.product.server.koi_control_application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum UserRoleEnum {
    ROLE_MEMBER(1),
    ROLE_ADMIN(2),
    ROLE_CONTRIBUTOR(3),
    ROLE_STAFF(4);

    private final int value;
}