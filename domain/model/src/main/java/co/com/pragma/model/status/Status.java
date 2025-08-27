package co.com.pragma.model.status;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class Status {

    private UUID id;

    private String name;

    private String description;
}
