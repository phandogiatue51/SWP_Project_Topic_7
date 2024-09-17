package com.product.server.koi_control_application.service;


import com.product.server.koi_control_application.customException.*;
import com.product.server.koi_control_application.model.UserLimit;
import com.product.server.koi_control_application.pojo.userRegister;
import com.product.server.koi_control_application.repository.UserLimitRepository;
import com.product.server.koi_control_application.serviceInterface.IEmailService;
import com.product.server.koi_control_application.serviceInterface.IUserService;
import com.product.server.koi_control_application.model.UserRole;
import com.product.server.koi_control_application.model.Users;
import com.product.server.koi_control_application.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.product.server.koi_control_application.model.UserRoleEnum.ROLE_MEMBER;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UsersRepository usersRepository;
    private final IEmailService service;
    private final PasswordEncoder passwordEncoder;
    private final UserLimitRepository userLimitRepository;
    private final EmailServiceImpl emailService;
    @Override
    public void updatedUser(Users user) {
        usersRepository.save(user);
    }
    @Override
    public Users saveUser(userRegister register) {
        try {
            if (getUsersByUsername(register.getUsername()) == null) {

                Users user = Users.builder()
                        .username(register.getUsername())
                        .email(register.getEmail())
                        .roles(new HashSet<>())
                        .build();
                //Create user
                user.setPassword(passwordEncoder.encode(register.getPassword()));
                user.getRoles().add(new UserRole(register.getRole().getValue()));
                Users savedUser = usersRepository.save(user);

                // Create user limit
                UserLimit.builder().pondLimit(50).fishLimit(500).userId(savedUser.getId()).build();
                userLimitRepository.save(UserLimit.builder().pondLimit(50).fishLimit(500).userId(savedUser.getId()).build());

                // Send email to user
                userRegisterMail(user.getEmail(), savedUser);
                return savedUser;
            }

        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage().contains("users_email_unique")) {
                throw new AlreadyExistedException("Email already exists: " + register.getEmail());
            } else if (ex.getMessage().contains("users_username_unique")) {
                throw new AlreadyExistedException("Username already exists: " + register.getUsername());
            } else {
                throw ex;
            }
        }

        throw new AlreadyExistedException(register.getUsername());
    }

    @Override
    public void userRegisterMail(String email, Users savedUser) {
        String verificationLink = "https://koi-controls-e5hxekcpd0cmgjg2.eastasia-01.azurewebsites.net/api/users/verify/email/" + email;
        String emailBody = "Your account has been created successfully. Please verify your email to activate your account by clicking the following link: " + verificationLink;
        emailService.sendMail(savedUser.getEmail(), "Welcome to KOI Control Application", emailBody);
    }

    @Override
    public Users getUser(int id) {
        return usersRepository.fetchUsersById(id).orElseThrow(() -> new NotFoundException(String.valueOf(id)));
    }

    @Override
    public Users getUsersByEmail(String email) {
        return usersRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(email));
    }

    @Override
    public Users getUsersByUsername(String username) {
        return usersRepository.findByUsername(username).orElse(null);
    }

    @Override
    public Users userLogin(String email, String password) {
        return usersRepository.findByEmailAndPassword(email, password).orElseThrow(() -> new NotFoundException(email));
    }

    @Override
    public Page<Users> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return usersRepository.findAll(pageable);
    }

    @Override
    public void deleteUser(int id) {
        Users user = getUser(id);
        if (user == null) {
            throw new NotFoundException(String.valueOf(id));
        }
        user.removeRole();
        usersRepository.delete(user);
    }

    @Override
    public void resetPassword(String email) {
        Users user = usersRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(email));
        usersRepository.save(user);
    }

    @Override
    public String generateNewPassword() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

    @Override
    public void updatePassword(String email, String newPassword) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
    }

    @Override
    public void updateUser(Users user) {
        // TODO document why this method is empty
    }



}
