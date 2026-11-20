package com.diewland.android.qr_pp_plus;

import java.util.Locale;

/** Builds PromptPay (EMVCo) QR payload strings. Pure Java, no Android dependencies. */
public final class PromptPay {

    /** Account type, inferred purely from the ID's length. */
    public enum AccountType {
        PHONE(10), NATIONAL_ID(13), EWALLET(15);

        final int length;

        AccountType(int length) {
            this.length = length;
        }

        /** @return the type for this ID, or null if its length matches none. */
        public static AccountType of(String accId) {
            for (AccountType t : values()) {
                if (t.length == accId.length()) return t;
            }
            return null;
        }
    }

    private PromptPay() {}

    /**
     * @param accId  phone number (10 digits), national ID (13) or e-Wallet ID (15)
     * @param amount amount as typed, or "" for none
     * @return the full payload including CRC, or null if accId is not a valid length
     */
    public static String payload(String accId, String amount) {
        AccountType type = AccountType.of(accId);
        if (type == null) return null;

        String account;
        switch (type) {
            case PHONE:       account = "01130066" + accId.substring(1); break; // drop leading 0, add country code 66
            case NATIONAL_ID: account = "0213" + accId; break;
            default:          account = "0315" + accId; break;
        }
        String merchant = "0016A000000677010111" + account;
        String amountField = amount.isEmpty() ? "" : String.format(Locale.ROOT, "54%02d%s", amount.length(), amount);

        String payload = "000201010211"
                + "29" + merchant.length() + merchant
                + "5303764"   // currency: THB
                + amountField
                + "5802TH"    // country
                + "6304";     // CRC tag; checksum covers everything up to here
        return payload + CRC16.checksum(payload);
    }
}
