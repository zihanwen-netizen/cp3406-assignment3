package au.edu.jcu.assessment.studylab.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizRulesTest {
    @Test fun selectedAnswerIsComparedWithCorrectAnswer() {
        assertTrue(QuizRules.isCorrect("Room", "Room"))
        assertFalse(QuizRules.isCorrect("SQLite", "Room"))
    }

    @Test fun accuracyRoundsDownAndHandlesEmptySession() {
        assertEquals(66, QuizRules.accuracyPercent(2, 3))
        assertEquals(0, QuizRules.accuracyPercent(0, 0))
    }

    @Test fun accuracyDoesNotExceedValidAnswerRange() {
        assertEquals(100, QuizRules.accuracyPercent(9, 5))
        assertEquals(0, QuizRules.accuracyPercent(-1, 5))
    }

    @Test fun summaryCountsCorrectAnswersAndKeepsSessionLength() {
        assertEquals(SessionSummary(correct = 2, total = 3), QuizRules.summary(listOf(true, false, true)))
    }
}
