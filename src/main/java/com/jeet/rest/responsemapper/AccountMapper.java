package com.jeet.rest.responsemapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jeet.api.Logins;
import com.jeet.rest.responserecord.account.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

@ApplicationScoped
public class AccountMapper {

    public JsonNode parseErrorDetails(Object attribute) {
        if (attribute == null) {
            return null;
        }

        JSONObject parsed = new JSONObject(attribute.toString());
        return toObjectNode(parsed);
    }

    public UserResponse toUserResponse(Logins login, JsonNode errorDetails) {
        return new UserResponse(
                login.getUuid(),
                login.getUser(),
                login.getEmail(),
                login.getProfilePicture(),
                errorDetails);
    }

    private ObjectNode toObjectNode(JSONObject source) {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        Iterator<String> names = source.keys();
        while (names.hasNext()) {
            String name = names.next();
            objectNode.set(name, toJsonNode(source.get(name)));
        }
        return objectNode;
    }

    private JsonNode toJsonNode(Object value) {
        if (value == null || value == JSONObject.NULL) {
            return NullNode.instance;
        }
        if (value instanceof JSONObject object) {
            return toObjectNode(object);
        }
        if (value instanceof JSONArray array) {
            ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
            for (int index = 0; index < array.length(); index++) {
                arrayNode.add(toJsonNode(array.get(index)));
            }
            return arrayNode;
        }
        if (value instanceof String string) {
            return JsonNodeFactory.instance.textNode(string);
        }
        if (value instanceof Boolean bool) {
            return JsonNodeFactory.instance.booleanNode(bool);
        }
        if (value instanceof Integer number) {
            return JsonNodeFactory.instance.numberNode(number);
        }
        if (value instanceof Long number) {
            return JsonNodeFactory.instance.numberNode(number);
        }
        if (value instanceof BigInteger number) {
            return JsonNodeFactory.instance.numberNode(number);
        }
        if (value instanceof BigDecimal number) {
            return JsonNodeFactory.instance.numberNode(number);
        }
        if (value instanceof Double number) {
            return JsonNodeFactory.instance.numberNode(number);
        }
        if (value instanceof Float number) {
            return JsonNodeFactory.instance.numberNode(number);
        }
        if (value instanceof Number number) {
            return JsonNodeFactory.instance.numberNode(new BigDecimal(number.toString()));
        }
        return JsonNodeFactory.instance.textNode(value.toString());
    }
}
