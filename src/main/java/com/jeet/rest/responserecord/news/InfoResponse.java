package com.jeet.rest.responserecord.news;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InfoResponse(@JsonProperty("Info") String info) {
}
