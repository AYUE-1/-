package com.petadoption.enums;

public enum CertStatusEnum {
    UNCERTIFIED("UNCERTIFIED", "未认证"),
    PENDING("PENDING", "审核中"),
    APPROVED("APPROVED", "已认证"),
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String desc;

    CertStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static String getDesc(String code) {
        for (CertStatusEnum e : values()) {
            if (e.code.equals(code)) return e.desc;
        }
        return code;
    }
}
