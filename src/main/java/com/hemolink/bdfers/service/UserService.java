package com.hemolink.bdfers.service;

import com.hemolink.bdfers.dto.request.AccountProfileRequest;
import com.hemolink.bdfers.dto.request.RegisterRequest;
import com.hemolink.bdfers.entity.AppUser;
import com.hemolink.bdfers.enums.UserRole;
import com.hemolink.bdfers.exception.BusinessRuleException;
import com.hemolink.bdfers.exception.DuplicateEmailException;
import com.hemolink.bdfers.exception.NotFoundException;
import com.hemolink.bdfers.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        AppUser user = new AppUser();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        user.setActive(true);

        try {
            // Flush immediately so a unique-constraint violation from a
            // simultaneous registration is detected here (DEF-10).
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException();
        }
    }

    @Transactional(readOnly = true)
    public AppUser getAccount(String email) {
        return findUser(email);
    }

    /**
     * Updates name and phone. The phone number is also the donor contact
     * number shared under FR-15. The email (login ID) cannot be changed.
     */
    @Transactional
    public void updateProfile(String email, AccountProfileRequest request) {
        AppUser user = findUser(email);
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        userRepository.save(user);
    }

    /**
     * Changes the password after checking the current one (NFR-02).
     * The new password is stored as a BCrypt hash.
     *
     * @throws BusinessRuleException if the current password is wrong or the
     *         new password is the same as the current one
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        AppUser user = findUser(email);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessRuleException("Your current password is incorrect.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessRuleException("The new password must be different from the current one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private AppUser findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User account not found."));
    }
}
