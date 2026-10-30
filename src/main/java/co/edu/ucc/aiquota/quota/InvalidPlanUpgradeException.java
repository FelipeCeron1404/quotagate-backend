package co.edu.ucc.aiquota.quota;

import co.edu.ucc.aiquota.plan.SubscriptionPlan;

public class InvalidPlanUpgradeException extends RuntimeException {

    public InvalidPlanUpgradeException(SubscriptionPlan currentPlan, SubscriptionPlan targetPlan) {
        super("No es posible cambiar del plan " + currentPlan + " al plan " + targetPlan
                + ". Solo se permite subir a un plan superior");
    }
}
