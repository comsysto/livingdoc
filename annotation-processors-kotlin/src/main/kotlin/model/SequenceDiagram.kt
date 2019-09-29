package com.comsysto.livingdoc.kotlin.annotation.processors.model

import model.ExecutablePart

class SequenceDiagram(

        /**
         * the diagram title.
         */
        title: String? = null,

        /**
         * Any PlantUml files to be included.
         */
        includeFiles: List<String> = listOf(),

        val parts: List<ExecutablePart>

) : Diagram(title, includeFiles)