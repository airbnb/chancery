package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class StupidDeserializationTest {
    @Test
    public final void testDeserialization() throws IOException {
        final InputStream stream = ClassLoader.getSystemResourceAsStream("example.json");
        final ObjectMapper mapper = new ObjectMapper();
        final CallbackPayload payload = mapper.readValue(stream, CallbackPayload.class);
        log.info("Payload: {}", payload);
    }
}
