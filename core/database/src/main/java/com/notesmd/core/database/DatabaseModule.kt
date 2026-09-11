package com.notesmd.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "notesmd_database"
        )
        .fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed two built-in rows on first launch
                val terminalDark = """
                    INSERT INTO templates (name, font_family, font_scale, background_color, heading_color, code_block_tint, quote_accent_color, is_default, is_deleted, sync_state, version)
                    VALUES ('Terminal Dark', 'MONOSPACE', 1.0, '#0D1117', '#58A6FF', '#161B22', '#388BFD', 1, 0, 'SYNCED', 1)
                """.trimIndent()
                val githubLight = """
                    INSERT INTO templates (name, font_family, font_scale, background_color, heading_color, code_block_tint, quote_accent_color, is_default, is_deleted, sync_state, version)
                    VALUES ('GitHub Light', 'SANS_SERIF', 1.0, '#FFFFFF', '#1F2328', '#F6F8FA', '#0969DA', 0, 0, 'SYNCED', 1)
                """.trimIndent()
                db.execSQL(terminalDark)
                db.execSQL(githubLight)
            }
        })
        .build()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides
    fun provideTemplateDao(database: AppDatabase): TemplateDao = database.templateDao()
}
