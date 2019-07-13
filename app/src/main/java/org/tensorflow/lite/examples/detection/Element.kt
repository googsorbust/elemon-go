package org.tensorflow.lite.examples.detection

data class Element(
    val symbol: String,
    val number: Int,
    val weight: String,
    val name: String,
    val discovered: Boolean
)