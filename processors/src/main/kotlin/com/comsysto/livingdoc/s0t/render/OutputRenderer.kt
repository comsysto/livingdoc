package com.comsysto.livingdoc.s0t.render

import com.comsysto.livingdoc.s0t.model.S0tModel

interface OutputRenderer {

    fun render(model: S0tModel)
}