package com.github.kr328.webapi.api.subscriptions.subscription;

import com.github.kr328.webapi.api.subscriptions.proxy.Proxy;
import org.springframework.http.HttpHeaders;

public abstract class BaseSubscription {
    public Proxy[] parseFromRequest(HttpHeaders httpHeaders, String body) throws ParseException {
        throw new ParseException("Stub!");
    }

    public String buildForResponse(HttpHeaders httpHeaders, Proxy[] proxies) throws BuildException {
        throw new BuildException("Stub!");
    }

    public static class ParseException extends Exception {
        ParseException(String msg) {
            super(msg);
        }
    }

    public static class BuildException extends Exception {
        BuildException(String msg) {
            super(msg);
        }
    }
}
