package com.os95.app.features

import com.os95.app.features.onboarding.OnboardingStep
import com.os95.app.features.onboarding.OnboardingUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStepTransitionTest {

    @Test
    fun testOnboardingStepOrderAndCount() {
        val steps = OnboardingStep.values()
        assertEquals(7, steps.size)
        assertEquals(OnboardingStep.WELCOME, steps[0])
        assertEquals(OnboardingStep.PROFILE, steps[1])
        assertEquals(OnboardingStep.EXAM_TARGET, steps[2])
        assertEquals(OnboardingStep.SUBJECTS, steps[3])
        assertEquals(OnboardingStep.DAILY_HABIT, steps[4])
        assertEquals(OnboardingStep.APPEARANCE, steps[5])
        assertEquals(OnboardingStep.SUMMARY, steps[6])
    }

    @Test
    fun testDefaultStateValues() {
        val state = OnboardingUiState()
        assertEquals(OnboardingStep.WELCOME, state.step)
        assertEquals(95.0f, state.targetScore, 0.01f)
        assertTrue(state.selectedSubjects.contains("Mathematics"))
    }
}
