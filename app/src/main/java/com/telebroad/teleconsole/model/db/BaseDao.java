package com.telebroad.teleconsole.model.db;

import com.google.common.collect.Iterables;
import com.telebroad.teleconsole.model.Message;

import java.util.Collection;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface BaseDao<Entity> {

    @SuppressWarnings("unchecked")
    @Insert(onConflict = REPLACE)
    void save(Entity... entities);

    void deleteAll();

    default void refresh(Entity[] newEntities){
        deleteAll();
        save(newEntities);
    }
    default void newRefresh(Entity...entities){
        deleteAll();
        save(entities);
    }
}

