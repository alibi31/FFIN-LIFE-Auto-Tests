package life.saqtapro.config;

import org.aeonbits.owner.Config;

/**
 * Конфигурация модуля SaqtaPro. Отдельный интерфейс, чтобы не трогать общий EnvironmentConfiguration.
 * Порядок источников: -D свойства → переменные окружения → saqtapro-${env}.properties → saqtapro-test.properties.
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "system:env",
        "classpath:saqtapro-${env}.properties",
        "classpath:saqtapro-test.properties"
})
public interface SaqtaConfig extends Config {

    /** UI SaqtaPro на тестовом стенде */
    @Key("saqtapro.web.url")
    String webUrl();

    /** Backend API SaqtaPro */
    @Key("saqtapro.api.url")
    String apiUrl();

    /** WireMock, на который смотрит SaqtaPro вместо БМГ / eGov / комплаенс-провайдера */
    @Key("saqtapro.wiremock.url")
    String wiremockUrl();

    /** Сколько ждать завершения асинхронной верификации (БМГ → eGov → согласие по SMS) */
    @Key("saqtapro.gov.verification.timeout.seconds")
    @DefaultValue("30")
    int govVerificationTimeoutSeconds();

    /**
     * Расчёт пенсионного аннуитета, на который оформляется договор. Временное решение: как только
     * будет запрос расчёта из SaqtaPro, тест будет создавать свежий расчёт сам.
     */
    @Key("saqtapro.annuity.calculation.id")
    Long annuityCalculationId();

    /** Таймаут клиента SaqtaPro к гос. сервисам — нужен для тестов на зависание внешнего контура */
    @Key("saqtapro.gov.client.timeout.ms")
    @DefaultValue("5000")
    int govClientTimeoutMs();
}
