package com.healthsupport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequestDto {
    @NotBlank(message = "Issue description cannot be empty")
    private String issueDescription;

    private LocalDateTime preferredTime;
}
