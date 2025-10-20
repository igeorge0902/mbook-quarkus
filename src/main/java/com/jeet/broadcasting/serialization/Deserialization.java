package com.jeet.broadcasting.serialization;

import com.jeet.broadcasting.eventModel.AddMovie;
import org.apache.commons.codec.binary.Base64;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class Deserialization {

    public AddMovie deserializeAddMovie(String message) {
        try {
            byte[] data = Base64.decodeBase64(message);
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            Object obj = in.readObject();
            in.close();
            return (AddMovie) obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}