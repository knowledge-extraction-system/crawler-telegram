package kz.kaznu.telegram.client.models.enums;

public enum TdApiErrorCodeEnum {

    SUPERGROUP_NOT_FOUND(6),
    CHANNEL_PRIVATE(400);

    private int code;

    TdApiErrorCodeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
