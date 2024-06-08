package com.project.electricitybillgenerator.repository;

import com.project.electricitybillgenerator.model.BillUser;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<BillUser, Integer> {
}
