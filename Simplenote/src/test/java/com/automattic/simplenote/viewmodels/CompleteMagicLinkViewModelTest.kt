package com.automattic.simplenote.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.automattic.simplenote.CoroutineTestRule
import com.automattic.simplenote.repositories.MagicLinkRepository
import com.automattic.simplenote.repositories.MagicLinkResponseResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito

import org.junit.Assert.assertEquals
import org.junit.Rule

const val AUTH_KEY_TEST = "auth_key_test"
const val AUTH_CODE_TEST = "auth_code_test"

@ExperimentalCoroutinesApi
class CompleteMagicLinkViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutinesTestRule = CoroutineTestRule(UnconfinedTestDispatcher())

    private val repository: MagicLinkRepository = Mockito.mock(MagicLinkRepository::class.java)
    private val viewModel = CompleteMagicLinkViewModel(
        repository,
        coroutinesTestRule.testDispatcher
    )

    @Test
    fun instantiateViewModel() = runTest {
        assertEquals(MagicLinkUiState.Waiting, viewModel.magicLinkUiState.value)
    }

    @Test
    fun firingPostRequestShouldLeadToSuccess() = runTest {
        Mockito.`when`(
            repository.completeLogin(AUTH_KEY_TEST, AUTH_CODE_TEST)
        ).thenReturn(MagicLinkResponseResult.MagicLinkCompleteSuccess(AUTH_KEY_TEST, AUTH_CODE_TEST))

        val states = mutableListOf<MagicLinkUiState>()
        viewModel.magicLinkUiState.observeForever {
            states.add(it)
        }
        viewModel.completeLogin(AUTH_KEY_TEST, AUTH_CODE_TEST)

        assertEquals(MagicLinkUiState.Waiting::class.java, states[0]::class.java)
        assertEquals(MagicLinkUiState.Loading::class.java, states[1]::class.java)
        assertEquals(MagicLinkUiState.Success::class.java, states[2]::class.java)
    }

    @Test
    fun firingPostRequestShouldLeadToError() = runTest {
        Mockito.`when`(
            repository.completeLogin(AUTH_KEY_TEST, AUTH_CODE_TEST)
        ).thenReturn(MagicLinkResponseResult.MagicLinkError(code = 400))

        val states = mutableListOf<MagicLinkUiState>()
        viewModel.magicLinkUiState.observeForever {
            states.add(it)
        }
        viewModel.completeLogin(AUTH_KEY_TEST, AUTH_CODE_TEST)
        assertEquals(MagicLinkUiState.Waiting::class.java, states[0]::class.java)
        assertEquals(MagicLinkUiState.Loading::class.java, states[1]::class.java)
        assertEquals(MagicLinkUiState.Error::class.java, states[2]::class.java)
    }
}
