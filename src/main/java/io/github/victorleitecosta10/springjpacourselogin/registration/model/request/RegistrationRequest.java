package io.github.victorleitecosta10.springjpacourselogin.registration.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistrationRequest {

    private final String firstName;
    private final String lastName;
    private final String password;
    private final String email;
}
