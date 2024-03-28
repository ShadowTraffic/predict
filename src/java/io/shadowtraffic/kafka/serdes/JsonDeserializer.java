package io.shadowtraffic.kafka.serdes;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.header.Headers;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

import java.util.Map;

public class JsonDeserializer<Object> implements Deserializer<Object> {

    private IFn require;
    private IFn fromJson;

    @Override
    public void configure(Map<String, ?> config, boolean isKey) {
        require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("io.shadowtraffic.predict.base"));

        fromJson = Clojure.var("io.shadowtraffic.predict.base", "deserialize-json");
    }

    @Override
    public Object deserialize(String topic, byte[] data) {
        return (Object) fromJson.invoke(data);
    }

    @Override
    public Object deserialize(String topic, Headers headers, byte[] data) {
        return (Object) fromJson.invoke(data);
    }

    @Override
    public void close() {
        // No resources to release
    }
}
