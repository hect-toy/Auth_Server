package com.hecttoy.authserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTodoRequest {

    private String title;

    private String description;

    private Boolean completed;

    private Integer priority;
}
