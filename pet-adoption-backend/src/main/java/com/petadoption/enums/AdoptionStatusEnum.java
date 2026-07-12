package com.petadoption.enums;

import java.util.Map;
import java.util.Set;

public enum AdoptionStatusEnum {
    PENDING("PENDING", "待管理员审核"),
    ADMIN_APPROVED("ADMIN_APPROVED", "管理员已通过，待发布者确认"),
    APPROVED("APPROVED", "发布者已确认，领养达成"),
    REJECTED("REJECTED", "管理员拒绝"),
    OWNER_REJECTED("OWNER_REJECTED", "发布者拒绝"),
    CANCELLED("CANCELLED", "已取消"),
    COMPLETED("COMPLETED", "领养完成");

    private final String code;
    private final String desc;

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
        "PENDING", Set.of("ADMIN_APPROVED", "REJECTED", "CANCELLED"),
        "ADMIN_APPROVED", Set.of("APPROVED", "OWNER_REJECTED", "CANCELLED"),
        "APPROVED", Set.of("COMPLETED", "CANCELLED")
    );

    AdoptionStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static boolean isValidTransition(String from, String to) {
        return VALID_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static String getDesc(String code) {
        for (AdoptionStatusEnum e : values()) {
            if (e.code.equals(code)) return e.desc;
        }
        return code;
    }
}
