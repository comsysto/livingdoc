package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.S0tProcessor.Companion.environment
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNote
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNotes
import org.apache.commons.lang3.StringUtils.substring
import org.apache.commons.lang3.StringUtils.substringBeforeLast
import org.apache.commons.text.WordUtils
import javax.lang.model.element.TypeElement

/**
 * Models a note.
 */
data class NoteModel(val text: String, val position: Position, override val diagramIds: Set<String> = setOf()) : ExplicitlyFilterable<NoteModel> {

    override fun filter(diagramId: String?, types: Map<TypeName.ComplexTypeName, TypeModel>) = this

    companion object {

        fun allOf(typeElement: TypeElement): List<NoteModel> = typeElement.getAnnotation(PlantUmlNotes::class.java)
            ?.let { notes -> notes.value.mapNotNull { toNoteModel(it, typeElement) } }
            ?: typeElement.getAnnotation(PlantUmlNote::class.java)
                ?.let { listOfNotNull(toNoteModel(it, typeElement)) }
                .orEmpty()

        private fun toNoteModel(note: PlantUmlNote, typeElement: TypeElement): NoteModel? {
            return body(note, typeElement)?.let {
                NoteModel(
                    WordUtils.wrap(it, note.maxLineLength, "\n", note.wrapLongwords, note.wrapOn),
                    Position.valueOf(note.position.name)
                )
            }
        }

        private fun body(note: PlantUmlNote, typeElement: TypeElement): String? =
            note.value.ifBlank {
                environment().processingEnvironment.elementUtils.getDocComment(typeElement)
                    ?.let { bodyFrom(it, note.maxLengthFromJavaDoc) }
            }
        /**
         * Generates a note body from the specified String, abbreviating it to
         * the maximum number of sentences (terminated by a '.') that doesn't
         * exceed maxLength.
         *
         * @param s the string.
         * @param maxLength the maximum number of characters to use.
         */
        private fun bodyFrom(s: String, maxLength: Int): String = substringBeforeLast(substring(s, 0, maxLength), ".") + "."
    }
}