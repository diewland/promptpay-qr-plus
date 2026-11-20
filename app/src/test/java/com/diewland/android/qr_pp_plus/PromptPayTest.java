package com.diewland.android.qr_pp_plus;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PromptPayTest {

    @Test
    public void crc16_matchesCcittFalseCheckValue() {
        assertEquals("29B1", CRC16.checksum("123456789"));
    }

    @Test
    public void phone_noAmount() {
        assertEquals("00020101021129370016A0000006770101110113006681234567853037645802TH6304823E",
                PromptPay.payload("0812345678", ""));
    }

    @Test
    public void phone_withAmount() {
        assertEquals("00020101021129370016A0000006770101110113006681234567853037645406250.505802TH630482CB",
                PromptPay.payload("0812345678", "250.50"));
    }

    @Test
    public void nationalId() {
        assertEquals("00020101021129370016A00000067701011102131234567890123530376454031005802TH63044508",
                PromptPay.payload("1234567890123", "100"));
    }

    @Test
    public void eWallet() {
        assertEquals("00020101021129390016A0000006770101110315123456789012345530376454039.55802TH6304B8CA",
                PromptPay.payload("123456789012345", "9.5"));
    }

    @Test
    public void invalidLength_returnsNull() {
        assertNull(PromptPay.payload("081234567", ""));
        assertNull(PromptPay.AccountType.of(""));
    }
}
