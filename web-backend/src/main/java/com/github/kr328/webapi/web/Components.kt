package com.github.kr328.webapi.web

import org.springframework.stereotype.Component

@Component
data class Components(val surge2ShadowSocks: Surge2ShadowSocks = Surge2ShadowSocks(),
                      val clashPreprocessor: ClashPreprocessor = ClashPreprocessor())
