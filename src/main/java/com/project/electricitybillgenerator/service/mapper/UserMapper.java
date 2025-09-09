package com.project.electricitybillgenerator.service.mapper;

import com.project.electricitybillgenerator.dto.UserCreateRequest;
import com.project.electricitybillgenerator.dto.UserResponse;
import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.model.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper service for converting between User entities and DTOs.
 * 
 * @author Electricity Bill Generator Team
 * @version 1.0
 */
@Component
public class UserMapper {

    /**
     * Convert UserCreateRequest DTO to BillUser entity.
     * 
     * @param request the user creation request
     * @param role the role to assign to the user
     * @return BillUser entity
     */
    public BillUser toEntity(UserCreateRequest request, UserRole role) {
        if (request == null) {
            return null;
        }
        
        BillUser user = new BillUser();
        user.setName(request.getName());
        user.setAddress(request.getAddress());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Will be encoded by the service
        user.setRole(role);
        
        return user;
    }

    /**
     * Convert BillUser entity to UserResponse DTO.
     * 
     * @param user the BillUser entity
     * @return UserResponse DTO
     */
    public UserResponse toResponse(BillUser user) {
        if (user == null) {
            return null;
        }
        
        UserResponse response = new UserResponse();
        response.setMeterId(user.getMeterId());
        response.setName(user.getName());
        response.setAddress(user.getAddress());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole() != null ? user.getRole().name() : null);
        
        return response;
    }

    /**
     * Convert list of BillUser entities to list of UserResponse DTOs.
     * 
     * @param users list of BillUser entities
     * @return list of UserResponse DTOs
     */
    public List<UserResponse> toResponseList(List<BillUser> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
