package com.elewise.ldmobile.model;

public enum ProcessType {
    INBOX("IMP"), OUTBOX("EXP");

    public String getType() {
        return type;
    }

    private String type;

    ProcessType(String type) {
        this.type = type;
    }
}
