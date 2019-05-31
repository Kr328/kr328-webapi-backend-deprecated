package com.github.kr328.webapi.web;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
public class ApiControllers {
    @Autowired
    private Surge2ShadowSocks surge2ShadowSocks;
    @Autowired
    private ClashPreprocess clashPreprocess;
}
