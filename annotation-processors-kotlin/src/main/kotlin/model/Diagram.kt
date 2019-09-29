package com.comsysto.livingdoc.kotlin.annotation.processors.model

open class Diagram(

        /**
         * the diagram title.
         */
        val title: String? = null,

        /**
         * Any PlantUml files to be included.
         */
        val includeFiles: List<String> = listOf()
)