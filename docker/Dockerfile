# al3HG - imagem para rodar o servidor (Paper 1.21.11 via AdvancedSlimePaper) SOMENTE na máquina local.
# O server.jar, os plugins, o eula.txt e o mundo ficam em /server (volume montado pelo docker-compose.yml);
# nada disso é copiado para dentro da imagem.
ARG JAVA_VERSION=25
FROM eclipse-temurin:${JAVA_VERSION}-jre

# UID/GID do usuário que roda o servidor. No Linux, use os mesmos valores do seu usuário
# (id -u / id -g) para conseguir editar os arquivos de docker/data sem sudo.
ARG MC_UID=1000
ARG MC_GID=1000

# Algumas imagens base já trazem um usuário com UID/GID 1000 (ex.: "ubuntu"); removemos antes de criar o "mc".
RUN (getent passwd "${MC_UID}" | cut -d: -f1 | xargs -r userdel -r 2>/dev/null || true) \
    && (getent group "${MC_GID}" | cut -d: -f1 | xargs -r groupdel 2>/dev/null || true) \
    && groupadd -g "${MC_GID}" mc \
    && useradd -m -u "${MC_UID}" -g "${MC_GID}" mc \
    && mkdir -p /server \
    && chown mc:mc /server

# Memória do JVM (-Xms e -Xmx recebem o mesmo valor). Pode ser trocada na hora: MEMORY=4G docker compose up
ENV MEMORY=2G

WORKDIR /server
VOLUME /server
EXPOSE 25565
USER mc

# "exec" para o Java ser o processo principal (recebe o SIGTERM do "docker compose down" e salva o mundo).
ENTRYPOINT ["sh", "-c", "exec java -Xms${MEMORY} -Xmx${MEMORY} -jar server.jar nogui"]
