package com.yourname.mmoitemseditor.util;

public final class Utils {

    private Utils() {}

    public static Object smartConvert(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        if (trimmed.equalsIgnoreCase("true")) return true;
        if (trimmed.equalsIgnoreCase("false")) return false;
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) { /* Not an integer */ }
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) { /* Not a double */ }
        return text;
    }
}
