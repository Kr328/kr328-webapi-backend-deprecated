package com.github.kr328.webapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {
    private String username;
    private long userId;
    private long messageId;
    private String secret;
}
