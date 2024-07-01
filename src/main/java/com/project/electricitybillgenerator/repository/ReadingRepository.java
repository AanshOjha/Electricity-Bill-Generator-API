package com.project.electricitybillgenerator.repository;

import java.util.List;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.project.electricitybillgenerator.model.BillReading;

public interface ReadingRepository extends CrudRepository<BillReading, Integer> {
    void deleteByMeterId(int meterId);

    @Query(value = "SELECT * FROM billreading WHERE meter_id=?1 AND date LIKE ?2%", nativeQuery = true)
    Optional<BillReading> findByMeterIdAndDateTest(int meterId, String date);

    @Query(value = "SELECT current_month_reading FROM billreading where meter_id=?1", nativeQuery = true)
    public List<Double> previousReading(int meterId, Date previousDate);

    @Modifying // Because error: Statement.executeQuery() cannot issue statements that do not produce result sets
    @Query(value = "DELETE FROM billreading WHERE meter_id=?1 AND date LIKE ?2%", nativeQuery = true)
    void deleteReadingByMeterIdAndDate(int meterId, String date);

}
