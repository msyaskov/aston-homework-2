package aston.hw2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuratorDto {

    private Integer id;

    private String name;

    private String email;

    private Integer experience;

}
