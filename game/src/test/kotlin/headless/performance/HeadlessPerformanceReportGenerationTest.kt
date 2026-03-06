import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertTrue

internal class HeadlessPerformanceReportGenerationTest {

    @AfterEach
    fun cleanup() {
        resetHeadlessTestRuntime()
    }

    @Test
    fun `performance benchmark log artifact is generated`() {
        val written = HeadlessPerformanceReportGenerator.generateAndWriteLog()
        assertTrue(Files.isRegularFile(written), "Expected performance benchmark log at $written")

        val content = Files.readString(written)
        assertTrue(content.contains("throughput.ticks_per_second="))
        assertTrue(content.contains("soak.final_wave="))
    }
}