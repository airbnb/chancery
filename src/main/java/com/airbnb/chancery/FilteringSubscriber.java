package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;

import javax.validation.constraints.NotNull;

public abstract class FilteringSubscriber {
    @Getter
    private final RefFilter filter;

    protected FilteringSubscriber(String filter) {
        this.filter = new RefFilter(filter);
    }

    protected abstract void handleCallback(@NotNull CallbackPayload callbackPayload)
            throws Exception;

    @Subscribe
    @AllowConcurrentEvents
    public void receiveCallback(@NotNull CallbackPayload callbackPayload)
            throws Exception {
        try {
            if (filter.matches(callbackPayload)) {
                /* TODO: metric! */
                handleCallback(callbackPayload);
            } else {
                /* TODO: metric! */
            }
        } catch (Exception e) {
            /* TODO: metric! */
            throw e;
        }
    }
}
