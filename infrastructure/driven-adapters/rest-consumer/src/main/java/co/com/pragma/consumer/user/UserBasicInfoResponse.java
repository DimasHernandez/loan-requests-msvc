package co.com.pragma.consumer.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserBasicInfoResponse {

    private String name;

    private String surname;

    private String email;

    private Integer baseSalary;
}
