package com.pocketcode.data.project.repository

import android.content.Context
import com.pocketcode.domain.project.model.Project
import com.pocketcode.data.project.git.GitClient
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.domain.project.repository.ProjectRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gitClient: GitClient
) : ProjectRepository {

    private val projectsDir by lazy {
        File(context.filesDir, "projects").apply { mkdirs() }
    }

    override fun getProjects(): Flow<List<Project>> = flow {
        val projectFiles = withContext(Dispatchers.IO) {
            projectsDir.listFiles { file -> file.isDirectory } ?: emptyArray()
        }
        val projects = projectFiles.map { file ->
            Project(id = file.name, name = file.name, localPath = file.absolutePath)
        }
        emit(projects)
    }

    override suspend fun getProject(id: String): Project? = withContext(Dispatchers.IO) {
        val projectDir = File(projectsDir, id)
        if (projectDir.exists() && projectDir.isDirectory) {
            Project(id = id, name = id, localPath = projectDir.absolutePath)
        } else {
            null
        }
    }

    override suspend fun createProject(name: String): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val projectDir = File(projectsDir, name)
            if (projectDir.exists()) {
                Result.failure(IllegalArgumentException("Project with name '$name' already exists"))
            } else {
                projectDir.mkdirs()
                // Create a basic Android project structure
                File(projectDir, "src/main/java/com/example/${name.lowercase()}").mkdirs()
                File(projectDir, "src/main/res/layout").mkdirs()
                File(projectDir, "build.gradle.kts").writeText(getInitialBuildGradleContent(name))
                File(projectDir, "src/main/AndroidManifest.xml").writeText(getInitialManifestContent(name))

                val project = Project(id = name, name = name, localPath = projectDir.absolutePath)
                Result.success(project)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProject(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val projectDir = File(projectsDir, id)
            if (projectDir.exists()) {
                val deleted = projectDir.deleteRecursively()
                if (deleted) Result.success(Unit) else Result.failure(Exception("Failed to delete project directory."))
            } else {
                Result.failure(Exception("Project with id '$id' not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFileContent(project: Project, filePath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(project.localPath, filePath)
            if (file.exists() && file.isFile) {
                Result.success(file.readText())
            } else {
                Result.failure(Exception("File not found at path: $filePath"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveFileContent(project: Project, filePath: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(project.localPath, filePath)
            file.parentFile?.mkdirs()
            file.writeText(content)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getInitialBuildGradleContent(projectName: String): String {
        return """
        plugins {
            id("com.android.application")
            id("org.jetbrains.kotlin.android")
        }

        android {
            namespace = "com.example.${projectName.lowercase()}"
            compileSdk = 33

            defaultConfig {
                applicationId = "com.example.${projectName.lowercase()}"
                minSdk = 24
                targetSdk = 33
                versionCode = 1
                versionName = "1.0"
            }
        }
        """.trimIndent()
    }

    private fun getInitialManifestContent(projectName: String): String {
        return """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
            package="com.example.${projectName.lowercase()}">

            <application
                android:allowBackup="true"
                android:icon="@mipmap/ic_launcher"
                android:label="@string/app_name"
                android:roundIcon="@mipmap/ic_launcher_round"
                android.supportsRtl="true"
                android:theme="@style/Theme.PocketCode">
                <activity
                    android:name=".MainActivity"
                    android:exported="true">
                    <intent-filter>
                        <action android:name="android.intent.action.MAIN" />
                        <category android:name="android.intent.category.LAUNCHER" />
                    </intent-filter>
                </activity>
            </application>
        </manifest>
        """.trimIndent()
    }

    override suspend fun listFiles(project: Project, path: String): Result<List<ProjectFile>> = withContext(Dispatchers.IO) {
        try {
            val directory = File(project.localPath, path)
            if (directory.exists() && directory.isDirectory) {
                val files = directory.listFiles()?.map {
                    ProjectFile(
                        name = it.name,
                        path = it.absolutePath.removePrefix(project.localPath).removePrefix("/"),
                        isDirectory = it.isDirectory
                    )
                }?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
                Result.success(files)
            } else {
                Result.failure(Exception("Directory not found at path: $path"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun initGitRepo(project: Project): Result<Unit> = withContext(Dispatchers.IO) {
        gitClient.init(File(project.localPath))
    }

    override suspend fun addFileToGit(project: Project, filePattern: String): Result<Unit> = withContext(Dispatchers.IO) {
        gitClient.add(File(project.localPath), filePattern)
    }

    override suspend fun commitChanges(project: Project, message: String): Result<Unit> = withContext(Dispatchers.IO) {
        gitClient.commit(File(project.localPath), message)
    }
}
