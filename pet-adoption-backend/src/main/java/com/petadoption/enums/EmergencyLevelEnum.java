package com.petadoption.enums;

public enum EmergencyLevelEnum {
    NORMAL("NORMAL", "普通"),
    URGENT("URGENT", "紧急"),
    CRITICAL("CRITICAL", "危急");

    private final String code;
    private final String desc;

    EmergencyLevelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static String getDesc(String code) {
        for (EmergencyLevelEnum e : values()) {
            if (e.code.equals(code)) return e.desc;
        }
        return code;
    }
}
