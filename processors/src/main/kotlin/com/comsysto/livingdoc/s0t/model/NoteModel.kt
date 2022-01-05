package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNote
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNotes
import javax.lang.model.element.TypeElement

/**
 * Models a note.
 */
data class NoteModel(val text: String, val position: Position) {

    companion object {
        fun allOf(typeElement: TypeElement): List<NoteModel> = typeElement.getAnnotation(PlantUmlNotes::class.java)
            ?.let { notes -> notes.value.map { NoteModel(it.value, Position.valueOf(it.position.name)) } }
            ?: typeElement.getAnnotation(PlantUmlNote::class.java)
                ?.let { listOf(NoteModel(it.value, Position.valueOf(it.position.name))) }
                .orEmpty()
    }
}