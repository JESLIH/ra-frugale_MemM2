package mg.ratra.agentique.data.pbf

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SQLite ne peut pas ouvrir un fichier directement depuis les assets de
 * l'APK (il a besoin d'un accès par chemin de fichier classique, pas d'un
 * flux compressé) — copie donc pbf_index.db vers le stockage interne au
 * premier lancement, puis l'ouvre en lecture seule.
 */
@Singleton
class PbfDatabaseOpener @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dbFile: File
        get() = context.getDatabasePath(PbfContract.DB_NAME)

    fun open(): SQLiteDatabase {
        if (!dbFile.exists()) {
            copyFromAssets()
        }
        return SQLiteDatabase.openDatabase(
            dbFile.path,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
    }

    private fun copyFromAssets() {
        dbFile.parentFile?.mkdirs()
        context.assets.open(PbfContract.DB_NAME).use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
