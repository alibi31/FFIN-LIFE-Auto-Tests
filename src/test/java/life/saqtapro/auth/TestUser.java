package life.saqtapro.auth;

import life.saqtapro.config.Saqta;

/**
 * Заранее заведённые на стенде пользователи. Два агента и два филиала нужны для проверок
 * IDOR (чужой договор) и межфилиальной изоляции; два андеррайтера — для гонки решений.
 *
 * Учётные данные: переменные окружения SAQTA_<ИМЯ>_LOGIN (почта) / SAQTA_<ИМЯ>_PASSWORD,
 * например SAQTA_QA_EMPLOYEE_LOGIN.
 */
public enum TestUser {
    /**
     * Сотрудник отдела тестирования (филиал Алматы, can_access_admin = true).
     * Пока единственная заведённая учётка — на ней работают тесты оформления договоров.
     */
    QA_EMPLOYEE(Role.MANAGER, "ALMATY"),
    AGENT_A(Role.AGENT, "ALMATY"),
    AGENT_B(Role.AGENT, "ALMATY"),
    MANAGER_ALMATY(Role.MANAGER, "ALMATY"),
    MANAGER_ASTANA(Role.MANAGER, "ASTANA"),
    UNDERWRITER(Role.UNDERWRITER, "HEAD_OFFICE"),
    UNDERWRITER_2(Role.UNDERWRITER, "HEAD_OFFICE"),
    COMPLIANCE_OFFICER(Role.COMPLIANCE_OFFICER, "HEAD_OFFICE");

    private final Role role;
    private final String branch;

    TestUser(Role role, String branch) {
        this.role = role;
        this.branch = branch;
    }

    public Role role() {
        return role;
    }

    public String branch() {
        return branch;
    }

    public String login() {
        return Saqta.secret("SAQTA_" + name() + "_LOGIN");
    }

    public String password() {
        return Saqta.secret("SAQTA_" + name() + "_PASSWORD");
    }
}
