package cn.yusiwen.spring.commons.file.core.enums;

public enum BucketAuthEnum {

    PRIVATE("private", "Only the owner can read/write"),
    PUBLIC_READ("public-read", "Public read, private write"),
    PUBLIC_READ_WRITE("public-read-write", "Public read/write");

    private final String code;
    private final String desc;

    BucketAuthEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
