package com.pocketcode.data.project.repository

import android.content.Context
import com.pocketcode.data.project.git.GitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.io.File

@RunWith(JUnit4::class)
class ProjectRepositoryImplTest {

    private lateinit var repository: ProjectRepositoryImpl
    private lateinit var tempDir: File
    private lateinit var context: Context
    private lateinit var gitClient: GitClient

    @Before
    fun setUp() {
        tempDir = createTempDirectory()
        context = mock(Context::class.java)
        gitClient = GitClient()
        `when`(context.filesDir).thenReturn(tempDir)
        repository = ProjectRepositoryImpl(context, gitClient)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `createProject should create a new project directory and files`() = runTest {
        val projectName = "TestProject"
        val result = repository.createProject(projectName)

        assertTrue(result.isSuccess)
        val project = result.getOrNull()
        assertNotNull(project)
        assertEquals(projectName, project?.name)

        val projectDir = File(tempDir, "projects/$projectName")
        assertTrue(projectDir.exists())
        assertTrue(File(projectDir, "build.gradle.kts").exists())
        assertTrue(File(projectDir, "src/main/AndroidManifest.xml").exists())
    }

    @Test
    fun `getProjects should return a list of existing projects`() = runTest {
        // Clear any existing projects first
        val initialProjects = repository.getProjects().first()
        initialProjects.forEach { project ->
            repository.deleteProject(project.id)
        }
        
        repository.createProject("Project1")
        repository.createProject("Project2")

        val projects = repository.getProjects().first()
        assertEquals(2, projects.size)
        assertTrue(projects.any { it.name == "Project1" })
        assertTrue(projects.any { it.name == "Project2" })
    }

    @Test
    fun `deleteProject should remove the project directory`() = runTest {
        val projectName = "ToDelete"
        repository.createProject(projectName)
        val project = repository.getProjects().first().find { it.name == projectName }!!

        val deleteResult = repository.deleteProject(project.id)
        assertTrue(deleteResult.isSuccess)

        val projects = repository.getProjects().first()
        assertFalse(projects.any { it.name == projectName })
        assertFalse(File(tempDir, "projects/$projectName").exists())
    }

    @Test
    fun `save and get file content should work correctly`() = runTest {
        val project = repository.createProject("FileTestProject").getOrThrow()
        val filePath = "src/main/java/com/example/App.kt"
        val content = "fun main() { println(\"Hello\") }"

        val saveResult = repository.saveFileContent(project, filePath, content)
        assertTrue(saveResult.isSuccess)

        val getContentResult = repository.getFileContent(project, filePath)
        assertTrue(getContentResult.isSuccess)
        assertEquals(content, getContentResult.getOrThrow())
    }

    @Test
    fun `listFiles should return the list of files in a directory`() = runTest {
        val project = repository.createProject("ListFilesTest").getOrThrow()
        repository.saveFileContent(project, "file1.txt", "content1")
        File(project.localPath, "dir1").mkdir()
        repository.saveFileContent(project, "dir1/file2.txt", "content2")

        val rootFiles = repository.listFiles(project, "").getOrThrow()
        // Expected files: build.gradle.kts, src (dir), file1.txt, dir1 (dir) = 4 items  
        // Note: AndroidManifest.xml is in src/main/, not root
        assertEquals(4, rootFiles.size) 
        assertTrue(rootFiles.any { it.name == "file1.txt" })
        assertTrue(rootFiles.any { it.name == "dir1" && it.isDirectory })
        assertTrue(rootFiles.any { it.name == "src" && it.isDirectory })
        assertTrue(rootFiles.any { it.name == "build.gradle.kts" && !it.isDirectory })

        val dir1Files = repository.listFiles(project, "dir1").getOrThrow()
        assertEquals(1, dir1Files.size)
        assertTrue(dir1Files.any { it.name == "file2.txt" })
    }

    @Test
    fun `git operations should work correctly`() = runTest {
        val project = repository.createProject("GitTestProject").getOrThrow()
        val projectDir = File(project.localPath)

        val initResult = repository.initGitRepo(project)
        assertTrue(initResult.isSuccess)
        assertTrue(File(projectDir, ".git").exists())

        val addResult = repository.addFileToGit(project, ".")
        assertTrue(addResult.isSuccess)

        val commitResult = repository.commitChanges(project, "Initial commit")
        assertTrue(commitResult.isSuccess)
    }

    private fun createTempDirectory(): File {
        return File.createTempFile("test", "").apply {
            delete()
            mkdir()
        }
    }
}
