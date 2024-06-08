package com.project.electricitybillgenerator.repository;

import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.model.BillUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<BillUser, Integer> {
    Optional<BillUser> findByMeterId(int meterId);

    @Modifying
    @Query(value = "delete from billreading where meter_id=?1", nativeQuery = true)
    public void deleteBillReadingByMeterId(int meterId);

    @Modifying
    @Query(value = "delete from user where meter_id=?1", nativeQuery = true)
    public void deleteUserByMeterId(int meterId);

    @Modifying
    @Query(value = "SET SQL_SAFE_UPDATES = 0", nativeQuery = true)
    public void disableSafeUpdates();

    @Modifying
    @Query(value = "delete from user;", nativeQuery = true)
    public void deleteAllUsers();

    @Modifying
    @Query(value = "delete from billreading;", nativeQuery = true)
    public void deleteAllReadings();

    @Modifying
    @Query(value = "SET SQL_SAFE_UPDATES = 1", nativeQuery = true)
    public void enableSafeUpdates();


}
