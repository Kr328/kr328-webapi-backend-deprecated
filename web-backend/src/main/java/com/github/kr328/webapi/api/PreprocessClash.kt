package com.github.kr328.webapi.api

import com.github.kr328.webapi.api.model.ClashPreprocessor
import com.github.kr328.webapi.api.serializer.ClashPreprocessorSerializer
import java.net.URL

fun preprocessClashParse(data: String): Pair<ClashPreprocessor, Set<URL>> {
    val r = ClashPreprocessorSerializer().parse(data)
    return Pair(r, r.sources.map(ClashPreprocessor.ProxySource::url).map(::URL).toSet() +
            r.ruleSets.map(ClashPreprocessor.RuleSet::url).toSet())
}

fun preprocessClashProcess(data: ClashPreprocessor, urlData: Map<URL, String>): String {
    TODO()
}