package com.example.demoe.Service;


import com.example.demoe.Entity.Admin;
import com.example.demoe.Entity.User;
import com.example.demoe.Repository.AdminRepo;
import com.example.demoe.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepo adminRepo; // Assuming you have a UserRepository to interact with your database

    public Optional<Admin> getAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Admin admin = (Admin) authentication.getPrincipal();
        String email = admin.getEmail();
        return adminRepo.findByEmail(email);
    }
}

