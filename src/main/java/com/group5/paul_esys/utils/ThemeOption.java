package com.group5.paul_esys.utils;

public class ThemeOption {

    private final String displayName;
    private final String className;

    public ThemeOption(String displayName, String className) {
        this.displayName = displayName;
        this.className = className;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
