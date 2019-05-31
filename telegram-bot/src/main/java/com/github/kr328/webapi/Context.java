package com.github.kr328.webapi;

import com.github.kr328.webapi.store.StoreManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Context {
    private StoreManager storeManager;
    private String groupLink;
}
