# Build com contexto raiz (context: .) para acessar o pom.xml pai do monorepo
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# 1. Copia apenas os pom.xml para aproveitar o cache de dependências
COPY pom.xml .
COPY user-service/pom.xml user-service/
COPY resource-service/pom.xml resource-service/
COPY allocation-service/pom.xml allocation-service/
COPY schedule-service/pom.xml schedule-service/
COPY api-gateway/pom.xml api-gateway/
COPY config-server/pom.xml config-server/
COPY discovery-server/pom.xml discovery-server/

# 2. Baixa dependências (camada cacheada enquanto os pom.xml não mudam)
RUN mvn -pl schedule-service -am dependency:go-offline -B --no-transfer-progress -q

# 3. Copia o código-fonte e compila
COPY . .
RUN mvn -pl schedule-service -am package -DskipTests -B --no-transfer-progress

# Runtime: imagem mínima JRE
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/schedule-service/target/schedule-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
