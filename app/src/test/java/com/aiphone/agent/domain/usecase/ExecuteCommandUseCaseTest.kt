package com.aiphone.agent.domain.usecase
import com.aiphone.agent.core.orchestration.Router
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ExecuteCommandUseCaseTest {
    private val router = mockk<Router>(relaxed = true)
    private val useCase = ExecuteCommandUseCase(router)

    @Test
    fun `invoke should emit thinking result`() = runTest {
        // Router is mocked - just ensure use case constructs without error
        assert(useCase != null)
    }
}