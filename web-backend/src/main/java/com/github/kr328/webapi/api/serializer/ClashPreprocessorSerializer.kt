package com.github.kr328.webapi.api.serializer

import com.charleskorn.kaml.Yaml
import com.github.kr328.webapi.api.model.ClashPreprocessor

class ClashPreprocessorSerializer {
    fun parse(string: String): ClashPreprocessor = Yaml.default.parse(ClashPreprocessor.serializer(), string)
    fun stringify(obj: ClashPreprocessor) = Yaml.default.stringify(ClashPreprocessor.serializer(), obj)
}