package life.saqtapro.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Тело POST /api/login: SaqtaPro ждёт поле "mail", а не "login" или "email" */
public record LoginRequest(
        @JsonProperty("mail") String mail,
        @JsonProperty("password") String password
) {

    /** Пароль не должен попасть в Allure и логи через toString() */
    @Override
    public String toString() {
        return "LoginRequest[mail=" + mail + ", password=***]";
    }
}
