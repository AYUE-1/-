package com.petadoption.enums;

public enum RoleEnum {
    USER("USER", "普通用户"),
    SHELTER("SHELTER", "救助机构/宠物店"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String desc;

    RoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static String getDesc(String code) {
        for (RoleEnum e : values()) {
            if (e.code.equals(code)) return e.desc;
        }
        return code;
    }
}
