package com.github.kr328.webapi.api.subscriptions.proxy.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProviderProxyData extends ProxyData {
    private String name;

    private long trafficUsed = -1;
    private long trafficTotal = -1;

    private Date expires;
}
