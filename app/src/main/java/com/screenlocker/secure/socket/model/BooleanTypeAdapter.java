package com.screenlocker.secure.socket.model;

/**
 * @author Muhammad Nadeem
 * @Date 7/24/2019.
 */

import java.lang.reflect.Type;

import com.google.gson.*;

public  class BooleanTypeAdapter implements JsonDeserializer<Boolean> {
    public Boolean deserialize(JsonElement json, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException {
        int code = json.getAsInt();
        return (code == 1);
    }
}