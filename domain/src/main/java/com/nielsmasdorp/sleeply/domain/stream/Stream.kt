package com.nielsmasdorp.sleeply.domain.stream

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
data class Stream(
    val id: String,
    val url: String,
    val title: String,
    val desc: String,
    val smallImgRes: Int
)