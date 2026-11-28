package au.edu.jcu.assessment.studylab.data.remote

import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Test

class TriviaContentDecoderTest {
    @Test
    fun decodesUtf8WithoutTreatingAngleBracketTextAsMarkup() {
        val original = "What does List<String> represent? <a> is a type parameter. Café"
        val encoded = Base64.getEncoder().encodeToString(original.toByteArray(Charsets.UTF_8))

        assertEquals(original, TriviaContentDecoder.decode(encoded))
    }
}
