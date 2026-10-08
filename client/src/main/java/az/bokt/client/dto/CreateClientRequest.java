package az.bokt.client.dto;

import az.bokt.common.domain.Gender;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateClientRequest(
        @NotBlank String pin,
        String firstName,
        String lastName,
        String middleName,
        LocalDate birthDate,
        Gender gender,
        String phone,
        String address,
        String secretWord
) {}
