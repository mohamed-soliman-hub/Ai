package com.aiphone.agent.data.repository
import android.content.Context
import com.aiphone.agent.data.local.preferences.SecurePreferences
import io.mockk.every; import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue; import org.junit.Before; import org.junit.Test
import java.io.File

class FileRepositoryImplTest {
    private lateinit var context: Context
    private lateinit var prefs: SecurePreferences
    private lateinit var repo: FileRepositoryImpl

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        every { prefs.isSandboxEnabled() } returns false
        repo = FileRepositoryImpl(context, prefs)
    }

    @Test
    fun `listFiles on non-existent directory returns failure`() = runTest {
        val result = repo.listFiles("/non/existent/path/12345")
        assertTrue(result.isFailure)
    }
}