package com.petadoption.enums;

public enum ArticleCategoryEnum {
    CAT("CAT", "猫咪"),
    DOG("DOG", "狗狗"),
    GENERAL("GENERAL", "综合"),
    CHECKLIST("CHECKLIST", "养前清单");

    private final String code;
    private final String desc;

    ArticleCategoryEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static String getDesc(String code) {
        for (ArticleCategoryEnum e : values()) {
            if (e.code.equals(code)) return e.desc;
        }
        return code;
    }
}
