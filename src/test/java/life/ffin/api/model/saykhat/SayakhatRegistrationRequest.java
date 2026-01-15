package life.ffin.api.model.saykhat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record SayakhatRegistrationRequest(
        @JsonProperty("country1") String country1,
        @JsonProperty("fioKir") String fioKir,
        @JsonProperty("iin") String iin,
        @JsonProperty("iin2") String iin2,
        @JsonProperty("sex") String sex,
        @JsonProperty("sex2") String sex2,
        @JsonProperty("curRate") String curRate,
        @JsonProperty("dateBirth") String dateBirth,
        @JsonProperty("dateBirth2") String dateBirth2,
        @JsonProperty("dateStart") String dateStart,
        @JsonProperty("dateEnd") String dateEnd,
        @JsonProperty("address") String address,
        @JsonProperty("address2") String address2,
        @JsonProperty("passportNum") String passportNum,
        @JsonProperty("passportNum2") String passportNum2,
        @JsonProperty("passportGive") String passportGive,
        @JsonProperty("passportGive2") String passportGive2,
        @JsonProperty("passportDate") String passportDate,
        @JsonProperty("passportDate2") String passportDate2,
        @JsonProperty("passportDateEnd") String passportDateEnd,
        @JsonProperty("passportDateEnd2") String passportDateEnd2,
        @JsonProperty("mobilePhone") String mobilePhone,
        @JsonProperty("mobilePhone2") String mobilePhone2,
        @JsonProperty("phone") String phone,
        @JsonProperty("phone2") String phone2,
        @JsonProperty("email") String email,
        @JsonProperty("email2") String email2,
        @JsonProperty("product") String product,
        @JsonProperty("rprogramm") String rprogramm,
        @JsonProperty("full_amount") String fullAmount,
        @JsonProperty("agreement") String agreement,
        @JsonProperty("fioLat") String fioLat,
        @JsonProperty("fioLat2") String fioLat2


) {
}
