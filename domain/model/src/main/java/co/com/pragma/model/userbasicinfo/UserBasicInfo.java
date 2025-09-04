package co.com.pragma.model.userbasicinfo;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserBasicInfo {

    private String name;

    private String surname;

    private String email;

    private String documentNumber;

    private Integer baseSalary;
}
