# QuotaGate IA · Backend

Plataforma de consumo de IA protegida por una **cadena de proxies** de rate limiting y cuotas, construida con Spring Boot 3 y Java 21.

- **API en producción:** https://quotagate-backend.onrender.com
- **Frontend:** https://github.com/FelipeCeron1404/quotagate-frontend

## Patrón Proxy

`MockAIGenerationService` es el sujeto real. `RateLimitProxyService` y `QuotaProxyService` implementan la misma interfaz `AIGenerationService` y cada uno recibe por constructor el siguiente eslabón, de modo que el encadenamiento sale de la composición y no de condicionales en el controlador.

```
GenerationController
        │  AIGenerationService (@Primary)
        ▼
RateLimitProxyService ──► 429 Too Many Requests + Retry-After
        │
        ▼
QuotaProxyService ──────► 402 Payment Required
        │
        ▼
MockAIGenerationService   Thread.sleep(1200ms) + texto predefinido
```

La cadena se arma en `GenerationProxyChainConfiguration`. Para agregar, quitar o reordenar controles solo se cambia esa clase.

## Planes

| Plan | Solicitudes/min | Tokens/mes |
|------|-----------------|------------|
| FREE | 10 | 50.000 |
| PRO | 60 | 500.000 |
| ENTERPRISE | Ilimitadas | Ilimitados |

## Endpoints

Todas las rutas que dependen del usuario reciben el encabezado `X-User-Id`.

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/ai/generate` | Genera texto pasando por la cadena de proxies completa |
| GET | `/api/quota/status` | Tokens usados, restantes, `resetDate`, plan y ventana de rate limit |
| GET | `/api/quota/history` | Uso diario de los últimos 7 días |
| POST | `/api/quota/upgrade` | Sube de plan (FREE → PRO, PRO → ENTERPRISE) con pago simulado |
| GET | `/api/consumers` | Usuarios de demostración |
| POST | `/api/demo/reset` | Restablece los datos de demostración |

Ejemplo:

```bash
curl -X POST https://quotagate-backend.onrender.com/api/ai/generate \
  -H "X-User-Id: laura-free" -H "Content-Type: application/json" \
  -d '{"prompt":"Explica el patrón Proxy"}'
```

## Tareas programadas

`UsageResetScheduler` reinicia la ventana de rate limit al inicio de cada minuto y la cuota mensual el día 1 de cada mes a la medianoche (zona horaria `America/Bogota`).

## Usuarios de demostración

| Id | Plan | Escenario |
|----|------|-----------|
| `laura-free` | FREE | Uso normal; ideal para mostrar el 429 |
| `mateo-free` | FREE | Casi sin cuota; ideal para mostrar el 402 y el upgrade |
| `sofia-pro` | PRO | Plan profesional |
| `andres-enterprise` | ENTERPRISE | Sin límites |

## Ejecutar en local

Requiere JDK 21 y Maven.

```bash
mvn spring-boot:run
mvn test
```

Por defecto usa H2 en memoria. Con el perfil `prod` y las variables `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER` y `PGPASSWORD` usa PostgreSQL.

## Despliegue

Render construye la imagen con el `Dockerfile` (ejecuta las pruebas durante el build) y expone el servicio en el puerto de la variable `PORT`. Health check: `/actuator/health`.
