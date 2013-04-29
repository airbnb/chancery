package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

public abstract class FilteringSubscriber {
    @Getter
    private final RefFilter filter;
    private final Meter exceptionMeter, filteredOutMeter;
    private final Timer handledTimer;

    protected FilteringSubscriber(String filter) {
        this.filter = new RefFilter(filter);
        exceptionMeter = Metrics.newMeter(getClass(),
                "triggered-exception", "callbacks",
                TimeUnit.HOURS);
        filteredOutMeter = Metrics.newMeter(getClass(),
                "filtered-out", "callbacks",
                TimeUnit.HOURS);
        handledTimer = Metrics.newTimer(getClass(),
                "handled-callbacks",
                TimeUnit.SECONDS, TimeUnit.SECONDS);
    }

    protected abstract void handleCallback(@NotNull CallbackPayload callbackPayload)
            throws Exception;

    @Subscribe
    @AllowConcurrentEvents
    public void receiveCallback(@NotNull CallbackPayload callbackPayload)
            throws Exception {
        try {
            if (!filter.matches(callbackPayload)) {
                filteredOutMeter.mark();
            } else {
                final TimerContext time = handledTimer.time();
                try {
                    handleCallback(callbackPayload);
                } catch (Exception e) {
                    throw e;
                } finally {
                    time.stop();
                }
            }
        } catch (Exception e) {
            exceptionMeter.mark();
            throw e;
        }
    }
}
