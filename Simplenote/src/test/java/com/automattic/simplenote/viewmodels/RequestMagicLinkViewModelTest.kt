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

const val email = "test@email.com"

@ExperimentalCoroutinesApi
class RequestMagicLinkViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutinesTestRule = CoroutineTestRule(UnconfinedTestDispatcher())

    private val repository: MagicLinkRepository = Mockito.mock(MagicLinkRepository::class.java)
    private val viewModel = RequestMagicLinkViewModel(
        repository,
        coroutinesTestRule.testDispatcher
    )

    @Test
    fun instantiateViewModel() = runTest {
        assertEquals(MagicLinkRequestUiState.Waiting, viewModel.magicLinkRequestUiState.value)
    }

    @Test
    fun firingPostRequestShouldLeadToSuccess() = runTest {
        Mockito.`when`(
            repository.requestLogin(email)
        ).thenReturn(MagicLinkResponseResult.MagicLinkRequestSuccess(code = 200))

        val states = mutableListOf<MagicLinkRequestUiState>()
        viewModel.magicLinkRequestUiState.observeForever {
            states.add(it)
        }
        viewModel.requestLogin(email)
        assertEquals(MagicLinkRequestUiState.Waiting::class.java, states[0]::class.java)
        assertEquals(MagicLinkRequestUiState.Loading::class.java, states[1]::class.java)
        assertEquals(MagicLinkRequestUiState.Success(username = email), states[2])
    }

    @Test
    fun firingPostRequestShouldLeadToError() = runTest {
        Mockito.`when`(
            repository.requestLogin(email)
        ).thenReturn(MagicLinkResponseResult.MagicLinkError(code = 400))

        val states = mutableListOf<MagicLinkRequestUiState>()
        viewModel.magicLinkRequestUiState.observeForever {
            states.add(it)
        }
        viewModel.requestLogin(email)
        assertEquals(MagicLinkRequestUiState.Waiting::class.java, states[0]::class.java)
        assertEquals(MagicLinkRequestUiState.Loading::class.java, states[1]::class.java)
        assertEquals(MagicLinkRequestUiState.Error::class.java, states[2]::class.java)
    }
}
