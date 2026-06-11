# Observabilidade — Melhorias pendentes

> Itens identificados que estão fora do escopo atual mas que elevariam a qualidade da observabilidade do SARC.

---

## 1. UI de Traces Distribuídos (Jaeger)

**Status:** OTEL Collector coleta traces mas só exporta para o console (`debug`). Não há UI visual.

**O que falta:**
- Adicionar o serviço `jaeger` no `docker-compose.yml`
- Atualizar `docker/otel-collector.yml` para exportar para Jaeger via OTLP

**Impacto:** sem Jaeger, não é possível visualizar o caminho completo de uma requisição passando por `api-gateway → allocation-service`, nem medir latência por span.

**Configuração sugerida:**

```yaml
# docker-compose.yml
jaeger:
  image: jaegertracing/all-in-one:latest
  container_name: sarc-jaeger
  ports:
    - "16686:16686"  # UI
```

```yaml
# docker/otel-collector.yml — adicionar ao pipeline de traces
exporters:
  otlp/jaeger:
    endpoint: jaeger:4317
    tls:
      insecure: true

service:
  pipelines:
    traces:
      exporters: [debug, otlp/jaeger]
```

---

## 2. Pipeline de Logs no OTEL Collector

**Status:** o `otel-collector.yml` só possui pipeline de `traces`. Logs estruturados dos serviços não são centralizados pelo collector.

**O que falta:**
- Adicionar receiver de logs no OTEL Collector (ex: `filelog` ou `otlp`)
- Criar pipeline `logs` no collector
- Configurar exporter de logs (ex: Loki para visualizar no Grafana)

**Impacto:** atualmente os logs ficam apenas no stdout de cada container. Não há busca centralizada por `traceId`, `professor` ou `recursoId` entre serviços.

---

## 3. Correlação de Logs com TraceId

**Status:** os logs estruturados emitem mensagens de negócio mas não incluem o `traceId` do span OTel ativo.

**O que falta:** com `micrometer-tracing` presente, o MDC do Logback é populado automaticamente com `traceId` e `spanId`. Basta adicionar `%X{traceId}` no padrão do Logback para cada serviço.

**Exemplo de `logback-spring.xml`:**

```xml
<pattern>%d{HH:mm:ss} %-5level [%X{traceId}] %logger{36} - %msg%n</pattern>
```

**Impacto:** sem isso, um log de conflito de horário não pode ser correlacionado com o trace da requisição que o originou.

---

## 4. Dashboard Grafana customizado para o SARC

**Status:** o Grafana está configurado e funcional, mas depende de importação manual de dashboard genérico (ID 4701).

**O que falta:** criar um dashboard JSON provisionado automaticamente em `docker/grafana/provisioning/dashboards/` com painéis específicos do negócio:

- Número de alocações criadas por hora
- Taxa de conflitos de horário (WARN por minuto)
- Tentativas de alocar recurso inativo
- Latência dos endpoints públicos (`/api/schedules`)
- Erro 403 por serviço (falhas de autorização)

---

## 5. Log de falha de conexão com banco

**Status:** parcialmente coberto — `DataAccessException` não está mapeada nos `GlobalExceptionHandler`.

**O que falta:** adicionar handler em cada `GlobalExceptionHandler`:

```java
@ExceptionHandler(DataAccessException.class)
public ResponseEntity<ErrorResponse> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
    log.error("Falha de acesso ao banco: {} | path={}", ex.getMessage(), request.getRequestURI());
    return build(HttpStatus.SERVICE_UNAVAILABLE, "Erro interno de persistencia", request, Map.of());
}
```

**Cobre o requisito:** *"Falha de conexão com banco"* da seção 15.

---

## 6. Log de operação administrativa no AlocacaoService

**Status:** o `AlocacaoService.remover()` loga a remoção de qualquer alocação, mas não distingue explicitamente quando um admin remove a alocação de outro professor.

**O que falta:** adicionar log diferenciado antes de `alocacaoRepository.delete(alocacao)`:

```java
if (currentUser.admin() && !alocacao.getProfessor().getEmail().equals(currentUser.email())) {
    log.info("Operacao administrativa: admin={} removendo alocacao id={} do professor={}",
        currentUser.email(), id, alocacao.getProfessor().getEmail());
}
```

---

## 7. Health Check customizado para dependências

**Status:** o Actuator expõe `/actuator/health` com status básico da JVM. Não há indicação quando o Keycloak ou o Config Server estão indisponíveis.

**O que falta:** implementar `HealthIndicator` customizado por serviço que verifica conectividade com suas dependências críticas (banco, Keycloak).

---

## 8. Alertas no Grafana

**Status:** métricas coletadas mas sem regras de alerta configuradas.

**O que falta:** configurar alertas para:
- Taxa de erro 5xx > 1% nos últimos 5 minutos
- Latência p95 do `/api/allocations` > 2 segundos
- Heap JVM > 80% em qualquer serviço

---

## Priorização sugerida

| Prioridade | Melhoria | Esforço |
|---|---|---|
| Alta | Correlação de logs com TraceId (#3) | Baixo |
| Alta | Log de `DataAccessException` (#5) | Baixo |
| Alta | Log admin no `AlocacaoService` (#6) | Baixo |
| Média | Jaeger para traces visuais (#1) | Médio |
| Média | Dashboard customizado Grafana (#4) | Médio |
| Baixa | Pipeline de logs no OTEL Collector (#2) | Alto |
| Baixa | Health Checks customizados (#7) | Médio |
| Baixa | Alertas Grafana (#8) | Médio |
