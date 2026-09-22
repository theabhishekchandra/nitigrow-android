package com.websbaba.nitigrow.core.util

private val WHITESPACE_RUN = Regex("\\s+")

/** Trims and folds every run of whitespace (including newlines) into a single space, for one-line previews. */
fun String.collapseWhitespace(): String = trim().replace(WHITESPACE_RUN, " ")
