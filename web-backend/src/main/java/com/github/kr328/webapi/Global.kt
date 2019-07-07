package com.github.kr328.webapi

object Global {
    val WEBAPI_DATA_PATH: String = System.getenv("WEBAPI_DATA_PATH") ?: throw RuntimeException("WEBAPI_DATA_PATH not set")
}