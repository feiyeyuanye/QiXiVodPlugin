package com.jhr.qixiplugin.util

object Text {
    private val trimRegex = Regex("\\s+")
    fun String.trimAll() = trimRegex.replace(this, "")
}