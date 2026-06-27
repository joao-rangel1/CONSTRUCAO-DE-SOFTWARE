# Observabilidade — O que foi implementado

> Sessão de implementação referente à seção 15 da ARQUITETURA_GERAL.md.

---

## Contexto

O projeto já possuía a infraestrutura de rastreamento (dependências OpenTelemetry, OTEL Collector, Actuator configurado), mas nenhum log estruturado havia sido implementado nos serviços e o endpoint Prometheus não funcionava por falta de dependência. Esta sessão cobriu todas as lacunas descritas na seção 15.

---

## 1. Dependência Prometheus adicionada

**Arquivos modificados:** `user-service/pom.xml`, `resource-service/pom.xml`, `allocation-service/pom.xml`, `schedule-service/pom.xml`, `api-gateway/pom.xml`

Adicionada a dependência em todos os serviços:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Por quê:** o endpoint `/actuator/prometheus` estava configurado mas retornava 404 sem esta dependência. Ela habilita a exportação de métricas no formato Prometheus scrape.

---

## 2. Log de inicialização nos serviços

**Arquivos modificados:**
- `user-service/src/main/java/br/edu/sarc/user/UserServiceApplication.java`
- `resource-service/src/main/java/br/edu/sarc/resource/ResourceServiceApplication.java`
- `allocation-service/src/main/java/br/edu/sarc/allocation/AllocationServiceApplication.java`
- `schedule-service/src/main/java/br/edu/sarc/schedule/ScheduleServiceApplication.java`

Adicionado `@Bean ApplicationRunner` com log `INFO` em cada classe principal:

```java
@Bean
ApplicationRunner onStartup() {
    return args -> log.info("allocation-service iniciado com sucesso");
}
```

**Cobre o requisito:** *"Inicialização"*

---

## 3. Logs de negócio no AlocacaoService

**Arquivo:** `allocation-service/.../service/AlocacaoService.java`

| Evento | Método | Nível |
|---|---|---|
| Alocação criada com sucesso | `criar()` | `INFO` |
| Conflito de horário detectado | `validarConflito()` | `WARN` |
| Tentativa de alocar recurso inativo | `validarECarregarRecursos()` | `WARN` |

**Cobre os requisitos:** *"Criação de alocação"*, *"Bloqueio por conflito de horário"*, *"Tentativa de alocar recurso inativo"*

---

## 4. Logs de operações administrativas no RecursoService

**Arquivo:** `resource-service/.../service/RecursoService.java`

| Evento | Método | Nível |
|---|---|---|
| Recurso criado | `criar()` | `INFO` |
| Recurso atualizado | `atualizar()` | `INFO` |
| Recurso ativado | `ativar()` | `INFO` |
| Recurso desativado | `desativar()` | `WARN` |
| Recurso removido | `remover()` | `INFO` |

**Cobre o requisito:** *"Operações administrativas sobre recursos"*

---

## 5. Logs de erros e segurança nos GlobalExceptionHandlers

**Arquivos modificados:**
- `allocation-service/.../exception/GlobalExceptionHandler.java`
- `user-service/.../exception/GlobalExceptionHandler.java`
- `resource-service/.../exception/GlobalExceptionHandler.java`

| Exceção | Serviço | Nível |
|---|---|---|
| `BusinessException` | allocation-service | `WARN` |
| `ForbiddenOperationException` | allocation-service | `WARN` |
| `DuplicateEmailException` | user-service | `WARN` |
| `RecursoNotFoundException` | resource-service | `WARN` |

**Cobre os requisitos:** *"Erros de validação de negócio"*, *"Falhas de autenticação e autorização"*

---

## 6. Stack Prometheus + Grafana no Docker Compose

**Arquivos criados/modificados:**

| Arquivo | Descrição |
|---|---|
| `docker/prometheus.yml` | Configuração de scrape dos 5 serviços a cada 15s |
| `docker/grafana/provisioning/datasources/prometheus.yml` | Datasource Prometheus provisionado automaticamente |
| `docker-compose.yml` | Serviços `prometheus` (porta 9090) e `grafana` (porta 3001) adicionados |

**Como acessar:**

| Interface | URL | Credenciais |
|---|---|---|
| Grafana | `http://localhost:3001` | `admin` / `admin` |
| Prometheus | `http://localhost:9090` | — |
| Targets Prometheus | `http://localhost:9090/targets` | — |

**Dashboard recomendado:** importar ID `4701` no Grafana (JVM Micrometer — métricas Spring Boot prontas).

---

## Rastreamento distribuído (OTel)

O rastreamento HTTP distribuído já estava funcional antes desta sessão via auto-instrumentação do `micrometer-tracing-bridge-otel`. Todas as requisições HTTP recebidas e o tempo de resposta são rastreados automaticamente com 100% de sampling (`probability: 1.0`). Os traces são enviados ao OTEL Collector via OTLP HTTP (`http://otel-collector:4318/v1/traces`).

---

## Resumo dos arquivos alterados

| Arquivo | Tipo de mudança |
|---|---|
| `*/pom.xml` (5 arquivos) | Dependência Prometheus |
| `*Application.java` (4 arquivos) | Log de inicialização |
| `AlocacaoService.java` | Logs de negócio |
| `RecursoService.java` | Logs de operações admin |
| `GlobalExceptionHandler.java` (3 arquivos) | Logs de erro e segurança |
| `docker-compose.yml` | Serviços Prometheus e Grafana |
| `docker/prometheus.yml` | Novo — config de scrape |
| `docker/grafana/provisioning/datasources/prometheus.yml` | Novo — datasource automático |
