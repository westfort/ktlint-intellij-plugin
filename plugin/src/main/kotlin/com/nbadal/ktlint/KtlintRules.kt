package com.nbadal.ktlint

import com.pinterest.ktlint.core.RuleSetProviderV2
import java.io.File
import java.net.URLClassLoader
import java.util.ServiceLoader

object KtlintRules {
    fun find(paths: List<String>, experimental: Boolean, skipErrors: Boolean) = ServiceLoader
        .load(
            RuleSetProviderV2::class.java,
            URLClassLoader(
                externalRulesetArray(paths),
                RuleSetProviderV2::class.java.classLoader,
            ),
        )
        .mapNotNull {
            try {
                it.getRuleProviders()
            } catch (err: Throwable) {
                if (!skipErrors) throw err
                null
            }
        }
        .flatten()
        .associateBy {
            val key = it.createNewRuleInstance().id
            if (key == "standard") "\u0000$key" else key
        }
        .filterKeys { experimental || it != "experimental" }
        .toSortedMap()
        .map { it.value }
        .toSet()

    private fun externalRulesetArray(paths: List<String>) = paths
        .map { it.replaceFirst(Regex("^~"), System.getProperty("user.home")) }
        .map { File(it) }
        .filter { it.exists() }
        .map { it.toURI().toURL() }
        .toTypedArray()
}
