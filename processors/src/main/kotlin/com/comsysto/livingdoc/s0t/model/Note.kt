package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNotes
import javax.lang.model.element.TypeElement

/**
 * Models a note.
 */
data class Note(val text: String, val position: Position) {

    companion object {
        fun allOf(typeElement: TypeElement): List<Note> = typeElement.getAnnotation(PlantUmlNotes::class.java)
                ?.let { notes -> notes.value.map { Note(it.body, Position.valueOf(it.position.name)) } }
                .orEmpty()
    }
}