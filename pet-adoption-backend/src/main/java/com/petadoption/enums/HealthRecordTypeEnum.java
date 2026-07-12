package com.petadoption.enums;

public enum HealthRecordTypeEnum {
    VACCINE("VACCINE", "疫苗"),
    DEWORM("DEWORM", "驱虫"),
    MEDICAL("MEDICAL", "就诊"),
    DIARY("DIARY", "成长日记");

    private final String code;
    private final String desc;

    HealthRecordTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static String getDesc(String code) {
        for (HealthRecordTypeEnum e : values()) {
            if (e.code.equals(code)) return e.desc;
        }
        return code;
    }
}
