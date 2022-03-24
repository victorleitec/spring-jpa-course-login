package io.github.victorleitecosta10.springjpacourselogin.appuser.service;

import io.github.victorleitecosta10.springjpacourselogin.appuser.model.entity.AppUser;
import io.github.victorleitecosta10.springjpacourselogin.appuser.model.repository.AppUserRepository;
import io.github.victorleitecosta10.springjpacourselogin.registration.token.model.ConfirmationToken;
import io.github.victorleitecosta10.springjpacourselogin.registration.token.service.ConfirmationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDateTime.*;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {

    private static final String USER_NOT_FOUND = "user with email %s not found";

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND, email)));
    }

    public String signUpUser(AppUser appUser) {
        //            TODO: check of attribuites are the same and
        //            TODO: if not email confirmed send confirmation email

        Optional<AppUser> existsUser = appUserRepository.findByEmail(appUser.getEmail());

        if (existsUser.isPresent()) {
            return appUserRepository.findByEmail(appUser.getEmail())
                    .filter(user -> user.getEmail().equals(appUser.getEmail()))
                    .filter(user -> !user.getEnabled())
                    .map(user -> {
                        String token = UUID.randomUUID().toString();

                        ConfirmationToken confirmationToken = new ConfirmationToken(token, now(), now().plusMinutes(15), user);
                        confirmationTokenService.saveConfirmationToken(confirmationToken);
                        return token;
                    })
                    .orElseThrow(() -> new IllegalStateException("email already taken"));
        }

        String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());
        appUser.setPassword(encodedPassword);

            appUserRepository.save(appUser);

//        TODO: Send confirmation token
        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(token, now(), now().plusMinutes(15), appUser);
        confirmationTokenService.saveConfirmationToken(confirmationToken);

//        TODO: Send email

        return token;
    }

    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }
}
