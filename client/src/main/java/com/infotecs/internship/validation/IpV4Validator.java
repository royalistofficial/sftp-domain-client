package com.infotecs.internship.validation;

/**
 * Валидатор адресов формата IPv4 (четыре десятичных октета 0-255,
 * разделённых точками.
 */
public class IpV4Validator implements IpValidator {

    private static final int OCTET_COUNT = 4;
    private static final int MIN_OCTET_VALUE = 0;
    private static final int MAX_OCTET_VALUE = 255;

    @Override
    public boolean isValid(String ip) {
        if (ip == null) {
            return false;
        }

        String[] octets = ip.split("\\.", -1);
        if (octets.length != OCTET_COUNT) {
            return false;
        }

        for (String octet : octets) {
            if (!isValidOctet(octet)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidOctet(String octet) {
        if (octet.isEmpty() || octet.length() > 3) {
            return false;
        }
        for (int i = 0; i < octet.length(); i++) {
            if (!Character.isDigit(octet.charAt(i))) {
                return false;
            }
        }
        if (octet.length() > 1 && octet.charAt(0) == '0') {
            return false;
        }

        int value = Integer.parseInt(octet);
        return value >= MIN_OCTET_VALUE && value <= MAX_OCTET_VALUE;
    }
}