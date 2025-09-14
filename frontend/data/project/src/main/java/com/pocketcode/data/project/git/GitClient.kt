package com.pocketcode.data.project.git

import org.eclipse.jgit.api.Git
import java.io.File
import javax.inject.Inject

class GitClient @Inject constructor() {

    fun init(directory: File): Result<Unit> {
        return try {
            Git.init().setDirectory(directory).call()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun add(directory: File, filePattern: String): Result<Unit> {
        return try {
            Git.open(directory).use { git ->
                git.add().addFilepattern(filePattern).call()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun commit(directory: File, message: String): Result<Unit> {
        return try {
            Git.open(directory).use { git ->
                git.commit().setMessage(message).call()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
