package co.com.pragma.model.userbasicinfo;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class UserBasicInfo {

    private String name;

    private String surname;

    private String email;

    private Integer baseSalary;
}
