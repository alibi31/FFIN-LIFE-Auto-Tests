package life.saqtapro.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.response.ValidatableResponse;
import life.saqtapro.api.model.annuity.PensionAnnuityContractRequest;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;

/** Договор пенсионного аннуитета в SaqtaPro */
public final class PensionAnnuityApi {

    // Обратите внимание: создание — v2, получение — v1 (так ходит фронтенд SaqtaPro)
    private static final String CREATE_CONTRACT = "/api/v2/contracts/pension-annuity/contract";
    private static final String GET_CONTRACT = "/api/v1/contracts/pension-annuity/contract/{contractId}";

    private static final ObjectMapper JSON = new ObjectMapper();

    private final String token;

    public PensionAnnuityApi(String token) {
        this.token = token;
    }

    @Step("Создать договор пенсионного аннуитета по расчёту {request.calculationId}")
    public ValidatableResponse createContract(PensionAnnuityContractRequest request) {
        String data = toJson(request);
        // AllureFilter не умеет показывать multipart-тело — прикладываем поле "data" сами
        Allure.addAttachment("multipart поле data", "application/json", data, ".json");

        return given()
                .spec(SaqtaRequestSpec.multipart(token))
                .multiPart(new MultiPartSpecBuilder(data)
                        .controlName("data")
                        .charset(StandardCharsets.UTF_8)
                        .build())
                .when()
                .post(CREATE_CONTRACT)
                .then();
    }

    @Step("Получить договор пенсионного аннуитета {contractId}")
    public ValidatableResponse getContract(long contractId) {
        return given()
                .spec(SaqtaRequestSpec.json(token))
                .pathParam("contractId", contractId)
                .when()
                .get(GET_CONTRACT)
                .then();
    }

    private static String toJson(Object body) {
        try {
            return JSON.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать тело договора", e);
        }
    }
}
