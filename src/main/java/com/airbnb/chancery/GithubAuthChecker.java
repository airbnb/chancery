package com.airbnb.chancery;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Slf4j
public class GithubAuthChecker {
    static final String HMAC_SHA1 = "HmacSHA1";
    final Mac mac;

    public GithubAuthChecker(String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        mac = Mac.getInstance(HMAC_SHA1);
        final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1);
        mac.init(signingKey);
    }

    boolean checkSignature(@Nullable String mac, @NotNull String payload) {
        if (mac == null || mac.length() != 40)
            return false;

        final char[] correct = Hex.encodeHex(this.mac.doFinal(payload.getBytes()));

        log.debug("Comparing {} and {}", correct, mac);
        return Arrays.equals(correct, mac.toCharArray());
    }
}
