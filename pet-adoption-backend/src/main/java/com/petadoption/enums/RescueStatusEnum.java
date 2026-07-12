package com.petadoption.enums;

public enum RescueStatusEnum {
    PENDING("PENDING", "待救助"),
    PROCESSING("PROCESSING", "处理中"),
    RESCUED("RESCUED", "已救助"),
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String desc;

    private static final java.util.Map<String, java.util.Set<String>> VALID_TRANSITIONS = java.util.Map.of(
        "PENDING", java.util.Set.of("PROCESSING", "CLOSED"),
        "PROCESSING", java.util.Set.of("RESCUED", "CLOSED"),
        "RESCUED", java.util.Set.of("CLOSED")
    );

    RescueStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static boolean isValidTransition(String from, String to) {
        return VALID_TRANSITIONS.getOrDefault(from, java.util.Set.of()).contains(to);
    }

    public static String getDesc(String code) {
        for (RescueStatusEnum e : values()) {
            if (e.code.equals(code)) return e.desc;
        }
        return code;
    }
}
