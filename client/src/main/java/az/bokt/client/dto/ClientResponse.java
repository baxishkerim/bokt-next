package az.bokt.client.dto;

import az.bokt.client.domain.Client;
import az.bokt.common.domain.Gender;

import java.time.LocalDate;

public record ClientResponse(
        Long id,
        String pin,
        String firstName,
        String lastName,
        String middleName,
        LocalDate birthDate,
        Gender gender,
        String phone,
        String address
) {
    public static ClientResponse from(Client c) {
        return new ClientResponse(c.getId(), c.getPin(), c.getFirstName(), c.getLastName(),
                c.getMiddleName(), c.getBirthDate(), c.getGender(), c.getPhone(), c.getAddress());
    }
}
