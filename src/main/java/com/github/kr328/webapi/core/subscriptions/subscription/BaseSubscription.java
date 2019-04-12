package com.github.kr328.webapi.core.subscriptions.subscription;

import com.github.kr328.webapi.core.subscriptions.proxy.Proxy;
import org.springframework.http.HttpHeaders;

public abstract class BaseSubscription {
    public Proxy[] parseFromRequest(HttpHeaders httpHeaders ,String body) throws ParseException {
        throw new ParseException("Stub!");
    }

    public String buildForResponse(HttpHeaders httpHeaders ,Proxy[] proxies) throws BuildException {
        throw new BuildException("Stub!");
    }

    public static class ParseException extends Exception {
        public ParseException(String msg) {
            super(msg);
        }
    }

    public static class BuildException extends Exception {
        public BuildException(String msg) {
            super(msg);
        }
    }
}
