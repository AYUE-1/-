package com.petadoption.enums;

public enum ReportStatusEnum {
    PENDING("PENDING", "待处理"),
    HANDLED("HANDLED", "已处理"),
    IGNORED("IGNORED", "已忽略");

    private final String code;
    private final String desc;

    ReportStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static String getDesc(String code) {
        for (ReportStatusEnum e : values()) {
            if (e.code.equals(code)) return e.desc;
        }
        return code;
    }
}
