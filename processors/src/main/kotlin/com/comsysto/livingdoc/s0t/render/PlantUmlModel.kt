package com.comsysto.livingdoc.s0t.render

import com.comsysto.livingdoc.s0t.model.S0tModel

data class PlantUmlModel(
        val templateDirectory: String,
        val outputDirectory: String,
        val s0tModel: S0tModel
)