package com.locadora.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Veiculo::class, ClienteCache::class, Locacao::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun veiculoDao(): VeiculoDao
    abstract fun locacaoDao(): LocacaoDao
}
