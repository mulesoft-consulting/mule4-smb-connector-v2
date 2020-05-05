package org.mule.extension.smb.internal.connection;

public enum FileCopyMode {
    COPY("copy"),
    MOVE("move");

    private String label;

    FileCopyMode(String label) {
        this.label = label;
    }

    public String label() {
        return this.label;
    }
}
