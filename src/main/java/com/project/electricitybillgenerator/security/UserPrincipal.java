package com.project.electricitybillgenerator.security;

import com.project.electricitybillgenerator.entity.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {
    
    private final Customer customer;
    
    public UserPrincipal(Customer customer) {
        this.customer = customer;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(customer.getRole().name()));
    }
    
    @Override
    public String getPassword() {
        return customer.getPassword();
    }
    
    @Override
    public String getUsername() {
        return customer.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return customer.getActive();
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return customer.getActive();
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public Long getId() {
        return customer.getId();
    }
    
    public Customer.Role getRole() {
        return customer.getRole();
    }
}
