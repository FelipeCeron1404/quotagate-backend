package co.edu.ucc.aiquota.generation;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class GeneratedTextCatalog {

    private static final List<String> PREDEFINED_TEXTS = List.of(
            "El patrón Proxy introduce un objeto intermediario que controla el acceso a otro objeto. "
                    + "En esta plataforma, cada solicitud atraviesa primero el límite de peticiones por minuto "
                    + "y luego la cuota mensual de tokens antes de llegar al servicio real de generación.",
            "Una buena estrategia para controlar costos en servicios de IA es medir el consumo en tokens. "
                    + "Así cada plan puede ofrecer un volumen predecible y los usuarios saben cuánto les queda "
                    + "antes de alcanzar su límite mensual.",
            "El rate limiting protege la estabilidad del sistema: evita que un solo usuario sature el servicio "
                    + "con ráfagas de solicitudes. Cuando se supera el límite, la API responde con el código 429 "
                    + "y el encabezado Retry-After indica cuántos segundos esperar.",
            "Separar responsabilidades en proxies independientes permite agregar, quitar o reordenar controles "
                    + "sin modificar el servicio real. Cada proxy implementa la misma interfaz y delega en el "
                    + "siguiente eslabón de la cadena.",
            "Los planes por niveles son comunes en plataformas SaaS: el plan gratuito permite explorar el "
                    + "producto, el plan profesional amplía la capacidad y el plan empresarial elimina los límites "
                    + "para equipos con alta demanda.",
            "Este texto fue producido por un servicio de IA simulado. La respuesta se elige al azar entre un "
                    + "conjunto predefinido y tarda alrededor de un segundo en generarse para imitar la latencia "
                    + "de un modelo real."
    );

    private final TokenEstimator tokenEstimator;

    public GeneratedTextCatalog(TokenEstimator tokenEstimator) {
        this.tokenEstimator = tokenEstimator;
    }

    public String randomText() {
        return PREDEFINED_TEXTS.get(ThreadLocalRandom.current().nextInt(PREDEFINED_TEXTS.size()));
    }

    public int averageCompletionTokens() {
        int total = PREDEFINED_TEXTS.stream().mapToInt(tokenEstimator::estimate).sum();
        return Math.round((float) total / PREDEFINED_TEXTS.size());
    }
}
