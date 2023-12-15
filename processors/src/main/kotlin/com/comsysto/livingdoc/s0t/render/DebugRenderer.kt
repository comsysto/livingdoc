package com.comsysto.livingdoc.s0t.render

import com.comsysto.livingdoc.s0t.model.S0tModel
import com.google.gson.GsonBuilder
import org.slf4j.LoggerFactory

class DebugRenderer: OutputRenderer {
    private val log = LoggerFactory.getLogger(DebugRenderer::class.java.name)
    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun render(model: S0tModel) {
        log.debug("s0t model passed to renderers: {}", gson.toJson(model, S0tModel::class.java))
    }
}