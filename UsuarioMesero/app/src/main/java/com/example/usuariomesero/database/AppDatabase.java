package com.example.usuariomesero.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Base de datos principal de la aplicación utilizando Room.
 * Define las entidades y provee acceso a los DAOs.
 */
@Database(entities = {MesaEntity.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    /**
     * @return El DAO para interactuar con la tabla de mesas.
     */
    public abstract MesaDao mesaDao();

    /**
     * Obtiene la instancia única (Singleton) de la base de datos.
     * @param context Contexto de la aplicación.
     * @return Instancia de AppDatabase.
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "restaurante_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Se permiten consultas en el hilo principal para fines de este ejercicio
                    .build();
        }
        return instance;
    }
}
