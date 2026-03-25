package com.example.extract_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedCccdDataDto {

    @JsonProperty("cccd_number")
    private String cccdNumber;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    private String gender;
    private String nationality;

    @JsonProperty("place_of_origin")
    private String placeOfOrigin;

    @JsonProperty("place_of_residence")
    private String placeOfResidence;
}
