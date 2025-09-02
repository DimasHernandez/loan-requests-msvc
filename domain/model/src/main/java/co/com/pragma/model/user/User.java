package co.com.pragma.model.user;

import co.com.pragma.model.user.enums.DocumentType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class User {

    private UUID id;

    private String name;

    private String surname;

    private String email;

    private DocumentType documentType;

    private String documentNumber;

    private String address;

    private String phoneNumber;

    private Integer baseSalary;

}
