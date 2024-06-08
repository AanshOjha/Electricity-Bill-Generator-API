package com.project.electricitybillgenerator.repository;

import java.util.List;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.project.electricitybillgenerator.model.BillReading;
import org.springframework.stereotype.Repository;

public interface ReadingRepository extends CrudRepository<BillReading, Integer> {
    @Query(value = "SELECT current_month_reading FROM billreading where meter_id=?1", nativeQuery = true)
    public List<Double> previousReading(int meterId, Date previousDate);

    @Query(value = "SELECT * FROM billreading WHERE meter_id=?1 AND date=?2", nativeQuery = true)
    public Optional<BillReading> findByMeterId(int meterId, String date);
}
