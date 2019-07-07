package com.github.kr328.webapi.web

import lombok.Data
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
data class Components(val surge2ShadowSocks: Surge2ShadowSocks = Surge2ShadowSocks(),
                      val clashPreprocess: ClashPreprocess = ClashPreprocess())
