package com.project.electricitybillgenerator.repository;

import com.project.electricitybillgenerator.model.BillReading;
import com.project.electricitybillgenerator.model.BillUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<BillUser, Integer> {
    Optional<BillUser> findByMeterId(int meterId);

    Optional<BillUser> findByMeterIdAndPassword(int meterId, String password);

    void deleteByMeterId(int meterId);
}
