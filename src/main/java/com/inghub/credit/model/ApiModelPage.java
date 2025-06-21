package com.inghub.credit.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiModelPage(@JsonProperty("pageNumber") int pageNumber, @JsonProperty("pageSize") int pageSize,
                           @JsonProperty("totalNumberOfRecords") long totalNumberOfRecords,
                           @JsonProperty("totalNumberOfPages") int totalNumberOfPages,
                           @JsonProperty("hasNextPage") boolean hasNextPage,
                           @JsonProperty("hasPreviousPage") boolean hasPreviousPage) {
}