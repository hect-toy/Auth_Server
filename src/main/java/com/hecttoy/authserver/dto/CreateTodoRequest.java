package com.hecttoy.authserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTodoRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Min(value = 0, message = "Priority must be at least 0")
    private Integer priority = 0;
}
