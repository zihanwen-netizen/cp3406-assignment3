package au.edu.jcu.assessment.studylab.data.remote

import java.nio.charset.StandardCharsets
import java.util.Base64

/** Decodes the Open Trivia DB base64 response without interpreting answer text as HTML. */
object TriviaContentDecoder {
    fun decode(encoded: String): String =
        String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8)
}
