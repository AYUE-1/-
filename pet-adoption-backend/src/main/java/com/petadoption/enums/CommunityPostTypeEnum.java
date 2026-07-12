package com.petadoption.enums;

public enum CommunityPostTypeEnum {
    SHARING("SHARING", "领养晒图"),
    LOST_FOUND("LOST_FOUND", "失宠寻宠");

    private final String code;
    private final String desc;

    CommunityPostTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static String getDesc(String code) {
        for (CommunityPostTypeEnum e : values()) {
            if (e.code.equals(code)) return e.desc;
        }
        return code;
    }
}
