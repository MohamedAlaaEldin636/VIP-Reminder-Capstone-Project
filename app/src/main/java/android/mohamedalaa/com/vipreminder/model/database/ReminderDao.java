package android.mohamedalaa.com.vipreminder.model.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Mohamed on 8/4/2018.
 *
 */
@Dao
public interface ReminderDao {

    // Query

    @Query("SELECT * FROM remindertable ORDER BY time ASC")
    List<ReminderEntity> getAllSync();

    @Query("SELECT * FROM remindertable ORDER BY time ASC")
    LiveData<List<ReminderEntity>> getAllAsync();

    @Query("SELECT * FROM remindertable WHERE id = :id")
    ReminderEntity getReminderEntityByIdSync(long id);

    @Query("SELECT * FROM remindertable WHERE workRequestUUID = :uuid")
    ReminderEntity getReminderEntityByWorkRequestUUIDSync(String uuid);

    // Insert

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<ReminderEntity> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ReminderEntity reminderEntity);

    // Update

    @Update
    int update(ReminderEntity reminderEntity);

    // Delete

    @Delete
    int delete(ReminderEntity... reminderEntities);

    @Query("DELETE FROM remindertable")
    int deleteAll();

}