package com.airbnb.chancery.github;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
public final class GithubAuthChecker {
    static final String HMAC_SHA1 = "HmacSHA1";
    final Mac mac;

    public GithubAuthChecker(String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        mac = Mac.getInstance(HMAC_SHA1);
        final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1);
        mac.init(signingKey);
    }

    /**
     * Checks a github signature against its payload
     * @param signature A X-Hub-Signature header value ("sha1=[...]")
     * @param payload The signed HTTP request body
     * @return Whether the signature is correct for the checker's secret
     */
    public boolean checkSignature(@Nullable String signature, @NotNull String payload) {
        if (signature == null || signature.length() != 45)
            return false;

        final char[] hash = Hex.encodeHex(this.mac.doFinal(payload.getBytes()));

        final StringBuilder builder = new StringBuilder("sha1=");
        builder.append(hash);
        final String expected = builder.toString();

        log.debug("Comparing {} and {}", expected, signature);
        return expected.equals(signature);
    }
}
