package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import lombok.NonNull;

import java.util.regex.Pattern;

public class UpdateFilter {
    @NonNull
    private final Pattern pattern;

    UpdateFilter(String repoRefPattern) {
        pattern = Pattern.compile(repoRefPattern);
    }

    public boolean shouldUpdate(CallbackPayload payload) {
        return pattern.matcher(format(payload)).matches();
    }

    private String format(CallbackPayload payload) {
        final StringBuilder sb = new StringBuilder();
        sb.append(payload.getRepository().getUrl());
        sb.append(":");
        sb.append(payload.getRef());
        return sb.toString();
    }
}
