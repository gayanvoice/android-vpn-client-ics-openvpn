package com.buzz.vpn;

import android.util.Base64;

public class EncryptData {
    public String encrypt(String input) {
        // This is base64 encoding, which is not an encryption
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    public String decrypt(String input) {
        return new String(Base64.decode(input, Base64.DEFAULT));
    }

}
