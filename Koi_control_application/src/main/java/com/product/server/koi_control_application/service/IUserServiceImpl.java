package com.product.server.koi_control_application.service;


import com.product.server.koi_control_application.CustomException.UserNotFoundException;
import com.product.server.koi_control_application.model.Users;
import com.product.server.koi_control_application.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IUserServiceImpl implements IUserService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void saveUser(Users user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        usersRepository.save(user);
    }

    @Override
    public Users getUser(int id) {
        return usersRepository.fetchUsersById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public Page<Users> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return usersRepository.findAll(pageable);
    }

    @Override
    public void deleteUser(int id) {
        Users user = getUser(id);
        usersRepository.delete(user);
    }

    @Override
    public void updateUser(Users user) {
        // TODO document why this method is empty
    }

    @Override
    public boolean checkPassword(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    @Override
    public void addPackage(int id, int packageId) {
        Users user = getUser(id);
        user.setPackageId(packageId);
        usersRepository.save(user);
    }
}
