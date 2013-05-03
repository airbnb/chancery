package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class RefFilter {
    @NonNull
    private final Pattern pattern;

    RefFilter(String repoRefPattern) {
        pattern = Pattern.compile(repoRefPattern);
    }

    public boolean matches(CallbackPayload payload) {
        final boolean matches = pattern.matcher(format(payload)).matches();
        log.debug("{} matched against {}, {}", payload, pattern, matches);
        return matches;
        return matches;
    }

    private String format(CallbackPayload payload) {
        final StringBuilder sb = new StringBuilder();
        sb.append(payload.getRepository().getUrl());
        sb.append(":");
        sb.append(payload.getRef());
        return sb.toString();
    }
}
