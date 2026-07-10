package com.example.usuariomesero.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

/**
 * Data Access Object (DAO) para la tabla de mesas.
 */
@Dao
public interface MesaDao {
    @Query("SELECT * FROM mesas ORDER BY numero ASC")
    List<MesaEntity> getAllMesas();

    @Query("SELECT * FROM mesas WHERE numero = :numero LIMIT 1")
    MesaEntity getMesaByNumero(int numero);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MesaEntity> mesas);

    @Update
    void updateMesa(MesaEntity mesa);

    @Query("DELETE FROM mesas")
    void deleteAll();
}
