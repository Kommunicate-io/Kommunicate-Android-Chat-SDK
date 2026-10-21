package io.kommunicate.ui.kommunicate.models;

public class KmCountryCode {
    private final String isoCode;
    private final String name;
    private final String dialCode;
    private final String flag;
    private final int minNumberLength;
    private final int maxNumberLength;

    public KmCountryCode(String isoCode, String name, String dialCode, int minNumberLength, int maxNumberLength) {
        this.isoCode = isoCode;
        this.name = name;
        this.dialCode = "+" + dialCode;
        this.flag = new String(Character.toChars(Character.codePointAt(isoCode, 0) - 'A' + 0x1F1E6))
                + new String(Character.toChars(Character.codePointAt(isoCode, 1) - 'A' + 0x1F1E6));
        this.minNumberLength = minNumberLength;
        this.maxNumberLength = maxNumberLength;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getName() {
        return name;
    }

    public String getDialCode() {
        return dialCode;
    }

    public String getFlag() {
        return flag;
    }

    public String getNumberValidationRegex() {
        return "^\\d{" + minNumberLength + "," + maxNumberLength + "}$";
    }

    public String getInternationalNumber(String number) {
        return getDialCode() + number;
    }
}
