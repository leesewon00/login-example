package loginexample.login;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class LoginForm {
    @NotEmpty
    private String email;
    @NotEmpty
    private String password;
}
