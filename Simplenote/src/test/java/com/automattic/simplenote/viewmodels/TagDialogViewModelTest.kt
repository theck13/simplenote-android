package com.automattic.simplenote.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.automattic.simplenote.CoroutineTestRule
import com.automattic.simplenote.R
import com.automattic.simplenote.models.Note
import com.automattic.simplenote.models.Tag
import com.automattic.simplenote.repositories.SimperiumCollaboratorsRepository
import com.automattic.simplenote.repositories.TagsRepository
import com.automattic.simplenote.usecases.ValidateTagUseCase
import com.automattic.simplenote.utils.getLocalRandomStringOfLen
import com.simperium.client.Bucket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TagDialogViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()
    @get:Rule
    val coroutinesTestRule = CoroutineTestRule(UnconfinedTestDispatcher())

    private val fakeTagsRepository = mock(TagsRepository::class.java)
    private val notesBucket = mock(Bucket::class.java) as Bucket<Note>
    private val collaboratorsRepository = SimperiumCollaboratorsRepository(
        notesBucket,
        coroutinesTestRule.testDispatcher
    )
    private val validateTagUseCase = ValidateTagUseCase(fakeTagsRepository, collaboratorsRepository)
    private val viewModel = TagDialogViewModel(fakeTagsRepository, validateTagUseCase)
    private val tagName = "tag1"
    private val tag = Tag(tagName).apply {
        name = tagName
        index = 0
    }

    @Before
    fun setup() {
        viewModel.start(tag)
    }

    @Test
    fun startShouldSetupUiState() {
        assertNull(viewModel.uiState.value?.errorMsg)
        assertEquals(viewModel.uiState.value?.tagName, tagName)
        assertEquals(viewModel.uiState.value?.oldTag, tag)
    }

    @Test
    fun closeShouldTriggerEventClose() {
        viewModel.close()

        assertEquals(viewModel.event.value, TagDialogEvent.CloseEvent)
    }

    @Test
    fun validateEmptyTag() {
        viewModel.updateUiState("")

        assertEquals(viewModel.uiState.value?.errorMsg, R.string.tag_error_empty)
    }

    @Test
    fun validateSpaceTag() {
        viewModel.updateUiState("tag 5")

        assertEquals(viewModel.uiState.value?.errorMsg, R.string.tag_error_spaces)
    }

    @Test
    fun validateTooLongTag() {
        val randomLongTag = getLocalRandomStringOfLen(279)

        viewModel.updateUiState(randomLongTag)

        assertEquals(viewModel.uiState.value?.errorMsg, R.string.tag_error_length)
    }

    @Test
    fun validateTagIsCollaborator() {
        val tagName = "tag1@email.com"
        whenever(fakeTagsRepository.isTagValid(tagName)).thenReturn(true)
        whenever(fakeTagsRepository.isTagMissing(tagName)).thenReturn(true)

        viewModel.updateUiState(tagName)

        assertEquals(viewModel.uiState.value?.errorMsg, R.string.tag_error_collaborator)
    }

    @Test
    fun validateValidTag() {
        val hewTagName = "tag2"
        whenever(fakeTagsRepository.isTagValid(hewTagName)).thenReturn(true)
        whenever(fakeTagsRepository.isTagConflict(hewTagName, tagName)).thenReturn(false)

        viewModel.updateUiState(hewTagName)

        assertNull(viewModel.uiState.value?.errorMsg)
    }

    @Test
    fun editTagWithSameName() {
        viewModel.updateUiState(tagName)

        viewModel.renameTagIfValid()

        assertTrue(viewModel.event.value is TagDialogEvent.FinishEvent)
    }

    @Test
    fun editTagWithNewName() {
        val newTagName = "tag2"
        viewModel.updateUiState(newTagName)
        whenever(fakeTagsRepository.isTagConflict(newTagName, tagName)).thenReturn(false)
        whenever(fakeTagsRepository.renameTag(newTagName, tag)).thenReturn(true)

        viewModel.renameTagIfValid()

        assertEquals(viewModel.uiState.value?.tagName, newTagName)
        assertTrue(viewModel.event.value is TagDialogEvent.FinishEvent)
    }

    @Test
    fun editTagWithConflict() {
        val newTagName = "tag2"
        viewModel.updateUiState(newTagName)
        whenever(fakeTagsRepository.isTagConflict(newTagName, tagName)).thenReturn(true)
        whenever(fakeTagsRepository.getCanonicalTagName(newTagName)).thenReturn(newTagName)

        viewModel.renameTagIfValid()

        assertEquals(viewModel.uiState.value?.tagName, newTagName)
        assertTrue(viewModel.event.value is TagDialogEvent.ConflictEvent)
        assertEquals((viewModel.event.value as TagDialogEvent.ConflictEvent).canonicalTagName, newTagName)
        assertEquals((viewModel.event.value as TagDialogEvent.ConflictEvent).oldTagName, tagName)
    }

    @Test
    fun editTagWithError() {
        val newTagName = "tag2"
        viewModel.updateUiState(newTagName)
        whenever(fakeTagsRepository.isTagConflict(newTagName, tagName)).thenReturn(false)
        whenever(fakeTagsRepository.renameTag(newTagName, tag)).thenReturn(false)

        viewModel.renameTagIfValid()

        assertEquals(viewModel.uiState.value?.tagName, newTagName)
        assertTrue(viewModel.event.value is TagDialogEvent.ShowErrorEvent)
    }

    @Test
    fun renameTagValid() {
        val newTagName = "tag2"
        viewModel.updateUiState(newTagName)
        whenever(fakeTagsRepository.renameTag(newTagName, tag)).thenReturn(true)

        viewModel.renameTag()

        assertEquals(viewModel.uiState.value?.tagName, newTagName)
        assertTrue(viewModel.event.value is TagDialogEvent.FinishEvent)
    }

    @Test
    fun renameTagError() {
        val newTagName = "tag2"
        viewModel.updateUiState(newTagName)
        whenever(fakeTagsRepository.renameTag(newTagName, tag)).thenReturn(false)

        viewModel.renameTag()

        assertEquals(viewModel.uiState.value?.tagName, newTagName)
        assertTrue(viewModel.event.value is TagDialogEvent.ShowErrorEvent)
    }
}
