package com.pocketcode.data.project.repository

import android.content.Context
import com.pocketcode.data.project.git.GitClient
import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.model.ProjectImportRequest
import com.pocketcode.domain.project.model.ProjectImportSource
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.domain.project.repository.ProjectRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayInputStream
import javax.inject.Inject
import java.util.zip.ZipInputStream

class ProjectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gitClient: GitClient
) : ProjectRepository {

    private val projectsDir by lazy {
        File(context.filesDir, "projects").apply {
            mkdirs()
            // Create sample projects if none exist
            if (listFiles { file -> file.isDirectory }?.isEmpty() != false) {
                createSampleProjects(this)
            }
        }
    }

    private val projectsDirCanonical by lazy { projectsDir.canonicalFile }

    private fun createSampleProjects(baseDir: File) {
        // Create sample project "HelloWorld"
        val helloWorldDir = File(baseDir, "HelloWorld")
        helloWorldDir.mkdirs()
        
        // Create basic Android project structure
        File(helloWorldDir, "app/src/main/java/com/example/helloworld").mkdirs()
        File(helloWorldDir, "app/src/main/res/layout").mkdirs()
        File(helloWorldDir, "app/src/main/res/values").mkdirs()
        
        // Create MainActivity.kt
        File(helloWorldDir, "app/src/main/java/com/example/helloworld/MainActivity.kt").writeText("""
            package com.example.helloworld
            
            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.material3.Text
            
            class MainActivity : ComponentActivity() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    setContent {
                        Text("Hello World!")
                    }
                }
            }
        """.trimIndent())
        
        // Create build.gradle.kts
        File(helloWorldDir, "app/build.gradle.kts").writeText("""
            plugins {
                id("com.android.application")
                id("org.jetbrains.kotlin.android")
            }
            
            android {
                compileSdk = 34
                
                defaultConfig {
                    applicationId = "com.example.helloworld"
                    minSdk = 24
                    targetSdk = 34
                    versionCode = 1
                    versionName = "1.0"
                }
            }
            
            dependencies {
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.activity:activity-compose:1.8.2")
                implementation("androidx.compose.material3:material3:1.1.2")
            }
        """.trimIndent())
        
        // Create AndroidManifest.xml
        File(helloWorldDir, "app/src/main/AndroidManifest.xml").writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <application android:label="HelloWorld">
                    <activity android:name=".MainActivity" android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
        """.trimIndent())
        
        // Create another sample project "WebPortfolio"
        val webPortfolioDir = File(projectsDir, "WebPortfolio")
        webPortfolioDir.mkdirs()
        File(webPortfolioDir, "css").mkdirs()
        File(webPortfolioDir, "js").mkdirs()
        File(webPortfolioDir, "images").mkdirs()
        
        // Create index.html
        File(webPortfolioDir, "index.html").writeText("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Mi Portfolio</title>
                <link rel="stylesheet" href="css/styles.css">
            </head>
            <body>
                <header>
                    <nav>
                        <h1>Mi Portfolio</h1>
                        <ul>
                            <li><a href="#about">Sobre mí</a></li>
                            <li><a href="#projects">Proyectos</a></li>
                            <li><a href="#contact">Contacto</a></li>
                        </ul>
                    </nav>
                </header>
                
                <main>
                    <section id="hero">
                        <h2>¡Hola! Soy un desarrollador</h2>
                        <p>Bienvenido a mi portfolio web</p>
                        <button onclick="showAlert()">Salúdame</button>
                    </section>
                    
                    <section id="about">
                        <h2>Sobre mí</h2>
                        <p>Soy un desarrollador apasionado por crear aplicaciones web modernas.</p>
                    </section>
                    
                    <section id="projects">
                        <h2>Mis Proyectos</h2>
                        <div class="project-card">
                            <h3>PocketCode</h3>
                            <p>Editor de código móvil</p>
                        </div>
                    </section>
                </main>
                
                <footer id="contact">
                    <p>Contacto: developer@example.com</p>
                </footer>
                
                <script src="js/script.js"></script>
            </body>
            </html>
        """.trimIndent())
        
        // Create styles.css
        File(webPortfolioDir, "css/styles.css").writeText("""
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }
            
            body {
                font-family: 'Arial', sans-serif;
                line-height: 1.6;
                color: #333;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
            }
            
            header {
                background: rgba(255, 255, 255, 0.95);
                padding: 1rem;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            }
            
            nav {
                display: flex;
                justify-content: space-between;
                align-items: center;
                max-width: 1200px;
                margin: 0 auto;
            }
            
            nav ul {
                display: flex;
                list-style: none;
                gap: 2rem;
            }
            
            nav a {
                text-decoration: none;
                color: #333;
                font-weight: 500;
                transition: color 0.3s;
            }
            
            nav a:hover {
                color: #667eea;
            }
            
            main {
                max-width: 1200px;
                margin: 0 auto;
                padding: 2rem;
            }
            
            section {
                margin: 3rem 0;
                padding: 2rem;
                background: rgba(255, 255, 255, 0.9);
                border-radius: 10px;
                box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            }
            
            #hero {
                text-align: center;
                padding: 4rem 2rem;
            }
            
            #hero h2 {
                font-size: 3rem;
                margin-bottom: 1rem;
                color: #333;
            }
            
            #hero p {
                font-size: 1.2rem;
                margin-bottom: 2rem;
                color: #666;
            }
            
            button {
                background: #667eea;
                color: white;
                border: none;
                padding: 12px 30px;
                border-radius: 25px;
                font-size: 1.1rem;
                cursor: pointer;
                transition: background 0.3s;
            }
            
            button:hover {
                background: #5a6fd8;
            }
            
            .project-card {
                background: #f8f9fa;
                padding: 2rem;
                border-radius: 8px;
                margin: 1rem 0;
                border-left: 4px solid #667eea;
            }
            
            footer {
                background: rgba(0, 0, 0, 0.8);
                color: white;
                text-align: center;
                padding: 2rem;
                margin-top: 3rem;
            }
            
            @media (max-width: 768px) {
                nav {
                    flex-direction: column;
                    gap: 1rem;
                }
                
                nav ul {
                    gap: 1rem;
                }
                
                #hero h2 {
                    font-size: 2rem;
                }
            }
        """.trimIndent())
        
        // Create script.js
        File(webPortfolioDir, "js/script.js").writeText("""
            // JavaScript para el portfolio
            
            function showAlert() {
                alert('¡Hola! Gracias por visitar mi portfolio 👋');
            }
            
            // Smooth scrolling para navegación
            document.querySelectorAll('a[href^="#"]').forEach(anchor => {
                anchor.addEventListener('click', function (e) {
                    e.preventDefault();
                    const target = document.querySelector(this.getAttribute('href'));
                    if (target) {
                        target.scrollIntoView({
                            behavior: 'smooth',
                            block: 'start'
                        });
                    }
                });
            });
            
            // Animación simple al cargar
            window.addEventListener('load', function() {
                const hero = document.getElementById('hero');
                hero.style.opacity = '0';
                hero.style.transform = 'translateY(20px)';
                hero.style.transition = 'opacity 0.8s, transform 0.8s';
                
                setTimeout(() => {
                    hero.style.opacity = '1';
                    hero.style.transform = 'translateY(0)';
                }, 100);
            });
            
            // Cambiar color del header al hacer scroll
            window.addEventListener('scroll', function() {
                const header = document.querySelector('header');
                if (window.scrollY > 50) {
                    header.style.background = 'rgba(255, 255, 255, 0.98)';
                    header.style.backdropFilter = 'blur(10px)';
                } else {
                    header.style.background = 'rgba(255, 255, 255, 0.95)';
                    header.style.backdropFilter = 'none';
                }
            });
        """.trimIndent())
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

    override suspend fun importProject(request: ProjectImportRequest): Result<Project> = withContext(Dispatchers.IO) {
        try {
            when (val source = request.source) {
                is ProjectImportSource.Archive -> importFromArchive(source)
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

    private fun importFromArchive(source: ProjectImportSource.Archive): Result<Project> {
        val rootFolder = detectRootFolder(source.bytes)
        val desiredName = source.suggestedName?.takeIf { it.isNotBlank() }
            ?: rootFolder
            ?: "ImportedProject"

        val projectDir = ensureUniqueProjectDir(desiredName)

        return try {
            extractArchive(source.bytes, rootFolder, projectDir)
            val project = Project(
                id = projectDir.name,
                name = projectDir.name,
                localPath = projectDir.absolutePath
            )
            Result.success(project)
        } catch (error: Exception) {
            projectDir.deleteRecursively()
            Result.failure(error)
        }
    }

    private fun extractArchive(bytes: ByteArray, rootFolder: String?, projectDir: File) {
        projectDir.mkdirs()
        val projectCanonical = projectDir.canonicalFile

        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val normalizedName = normalizeEntryName(entry.name)
                val relativePath = when {
                    normalizedName.isBlank() -> null
                    rootFolder != null && normalizedName == rootFolder -> null
                    rootFolder != null && normalizedName.startsWith("$rootFolder/") ->
                        normalizedName.removePrefix("$rootFolder/").trimStart('/')
                    else -> normalizedName
                }

                if (!relativePath.isNullOrBlank()) {
                    val targetFile = File(projectDir, relativePath)
                    if (!isInsideDirectory(projectCanonical, targetFile)) {
                        throw SecurityException("Invalid entry path: ${entry.name}")
                    }

                    if (entry.isDirectory) {
                        targetFile.mkdirs()
                    } else {
                        targetFile.parentFile?.mkdirs()
                        FileOutputStream(targetFile).use { output ->
                            zip.copyTo(output)
                        }
                    }
                }

                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    private fun detectRootFolder(bytes: ByteArray): String? {
        val candidates = mutableSetOf<String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val normalized = normalizeEntryName(entry.name)
                if (normalized.isNotBlank()) {
                    val segment = normalized.substringBefore('/')
                    if (segment.isNotBlank()) {
                        candidates.add(segment)
                        if (candidates.size > 1) {
                            return null
                        }
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return candidates.firstOrNull()
    }

    private fun normalizeEntryName(name: String): String {
        return name.replace('\\', '/').trim('/')
    }

    private fun ensureUniqueProjectDir(rawName: String): File {
        val baseName = sanitizeProjectName(rawName)
        var attempt = 0
        var candidate: File
        do {
            val suffix = if (attempt == 0) "" else "-${attempt + 1}"
            candidate = File(projectsDir, "$baseName$suffix")
            attempt++
        } while (candidate.exists())
        return candidate
    }

    private fun sanitizeProjectName(rawName: String): String {
        val trimmed = rawName.trim()
        if (trimmed.isEmpty()) return "ImportedProject"

        val normalized = buildString {
            trimmed.forEach { char ->
                append(
                    when {
                        char.isLetterOrDigit() -> char
                        char == '-' || char == '_' -> char
                        char.isWhitespace() -> '-'
                        else -> '-'
                    }
                )
            }
        }.replace(Regex("-+"), "-")

        val cleaned = normalized.trim('-','_')
        return if (cleaned.isNotEmpty()) cleaned else "ImportedProject"
    }

    private fun isInsideDirectory(rootDir: File, target: File): Boolean {
        val rootPath = rootDir.canonicalPath
        val targetPath = target.canonicalPath
        return targetPath == rootPath || targetPath.startsWith(rootPath + File.separator)
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
