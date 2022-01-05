package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNote
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNotes
import org.apache.commons.text.WordUtils
import javax.lang.model.element.TypeElement

/**
 * Models a note.
 */
data class NoteModel(val text: String, val position: Position) {

    companion object {
        fun allOf(typeElement: TypeElement): List<NoteModel> = typeElement.getAnnotation(PlantUmlNotes::class.java)
            ?.let { notes -> notes.value.map { toNoteModel(it) } }
            ?: typeElement.getAnnotation(PlantUmlNote::class.java)
                ?.let { listOf(toNoteModel(it)) }
                .orEmpty()

        private fun toNoteModel(it: PlantUmlNote) =
            NoteModel(WordUtils.wrap(it.value, it.maxLineLength, "\n", it.wrapLongwords, it.wrapOn), Position.valueOf(it.position.name))
    }
}