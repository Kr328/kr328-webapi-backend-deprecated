package com.github.kr328.webapi.tools;

import com.github.kr328.webapi.core.subscriptions.subscription.ShadowsocksDSubscription;
import com.github.kr328.webapi.core.subscriptions.subscription.SurgeSubscription;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class Subscriptions {
    private SurgeSubscription surgeSubscription = new SurgeSubscription();
    private ShadowsocksDSubscription shadowsocksDSubscription = new ShadowsocksDSubscription();
}
