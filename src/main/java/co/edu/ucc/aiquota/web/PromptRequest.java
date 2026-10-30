package co.edu.ucc.aiquota.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PromptRequest(
        @NotBlank(message = "El prompt no puede estar vacío")
        @Size(max = 4000, message = "El prompt no puede superar los 4000 caracteres")
        String prompt
) {
}
