package com.jeet.rest.responserecord.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        String uuid,
        String user,
        String email,
        String profilePicture,
        @JsonProperty("Error Details") JsonNode errorDetails) {
}
