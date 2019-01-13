package com.github.kr328.webapi.utils;

public class Proxy {
    public Provider provider;

    public String id;
    public String remark;
    public String host;
    public String port;
    public String password;
    public String method;

    public Plugin plugin;

    public interface Plugin {
        String getType();
        String getArguments();
    }

    public static class SimpleObfsPlugin implements Plugin {
        public String arguments = "";

        @Override
        public String getType() {
            return "simple-obfs";
        }

        @Override
        public String getArguments() {
            return arguments;
        }
    }

    public static class Provider {
        public String name;
    }
}
