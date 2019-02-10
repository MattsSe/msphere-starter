package io.github.mattsse.msphere

import com.siemens.mindsphere.sdk.core.RestClientConfig


class Template {

    init {
        @SuppressWarnings("unused")
        val config = RestClientConfig.builder()
            .connectionTimeoutInSeconds(100)
            .proxyHost("host")
            .proxyPort(8080)
            .build()
    }
}