package co.edu.ucc.aiquota.web;

import co.edu.ucc.aiquota.plan.SubscriptionPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpgradePlanRequest(
        @NotNull(message = "Debes indicar el plan al que deseas subir")
        SubscriptionPlan targetPlan,
        @NotBlank(message = "La referencia del pago simulado es obligatoria")
        @Size(max = 60, message = "La referencia del pago no puede superar los 60 caracteres")
        String paymentReference
) {
}
