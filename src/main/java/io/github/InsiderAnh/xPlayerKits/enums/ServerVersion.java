package io.github.InsiderAnh.xPlayerKits.enums;

public enum ServerVersion {

    v1_8,
    v1_9,
    v1_10,
    v1_11,
    v1_12,
    v1_13,
    v1_14,
    v1_15,
    v1_16,
    v1_17,
    v1_18_R1,
    v1_18_R2,
    v1_19_R1,
    v1_19_R2,
    v1_19_R3,
    v1_20_R1,
    v1_20_R2,
    v1_20_R3,
    v1_20_R4,
    v1_20(false),
    v1_21_R1,
    v1_21(false),
    v1_22;

    private final boolean implemented;

    ServerVersion() {
        this(true);
    }

    ServerVersion(boolean implemented) {
        this.implemented = implemented;
    }

    public static ServerVersion get(String v) {
        v = v.replace('.', '_');

        ServerVersion lastImplemented = null;
        for (ServerVersion k : ServerVersion.values()) {
            if (k.implemented) {
                lastImplemented = k;
            }
            if (v.contains(k.name().substring(1))) {
                return lastImplemented;
            }
        }
        return null;
    }

    public boolean serverVersionGreaterEqualThan(ServerVersion version1) {
        return ordinal() >= version1.ordinal();
    }

}