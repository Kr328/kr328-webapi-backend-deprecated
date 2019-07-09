package com.github.kr328.webapi.api.model

data class ProxyGroup(val type: Type, val proxies: List<String>) {
    interface Type {
        fun getTypeName(): String
    }

    class TypeUnknown(val type: String) : Type {
        override fun getTypeName(): String = type
    }

    class TypeSelect : Type {
        override fun getTypeName(): String = "select"
    }

    class TypeUrlTest(val url: String, val interval: Long?, val tolerate: Long?, val timeout: Long?): Type {
        override fun getTypeName(): String = "url-test"
    }

    class TypeFallback(val url: String, val interval: Long?): Type {
        override fun getTypeName(): String = "fallback"
    }
}