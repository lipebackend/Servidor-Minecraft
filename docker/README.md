# al3HG em Docker (somente máquina local)

Roda o servidor **Paper 1.21.11 via AdvancedSlimePaper (ASP) 4.2.0** com os plugins deste repositório (`al3hg` e os addons)
dentro de um contêiner, na **sua máquina** (Linux, Windows ou Mac). Não é uma configuração para hospedagem/produção.

## Pré-requisitos

- **Docker** com o plugin Compose (`docker compose`): Docker Desktop (Windows/Mac) ou Docker Engine (Linux).
- Para o `preparar.sh`: **bash** (no Windows, use Git Bash ou WSL), **Maven** e um **JDK 25+** (o build exige; ver "Build" no
  [README da raiz](../README.md)). Se `JAVA_HOME` estiver definido, ele é usado.
- Acesso à internet: o addon de estatísticas baixa o `sqlite-jdbc` do Maven Central ao carregar (declarado em `libraries:` no `plugin.yml`).
- Uma conta Minecraft Java Edition **1.21.11** para entrar no servidor.

## Passo a passo

1. **Compilar e copiar os plugins** (na raiz do repositório):

   ```bash
   ./docker/preparar.sh
   ```

   O script roda `mvn -q clean package -DskipTests`, cria `docker/data/plugins/` e copia para lá os JARs de `al3hg-plugin`
   (`al3hg-2.0.0-SNAPSHOT.jar`) e dos addons (`al3hg-addon-stats-…` e `al3hg-addon-template-…`; o de *template* é só um modelo,
   apague-o de `docker/data/plugins/` se não quiser carregá-lo). Os `original-*` ficam de fora. No final ele diz o que ainda falta.

2. **Colocar o `server.jar`**: baixe o AdvancedSlimePaper em <https://infernalsuite.com/download/asp> (build para o Minecraft
   1.21.11, versão 4.2.0) e salve como `docker/data/server.jar`. O script **não** baixa o arquivo.

3. **EULA da Mojang**: leia <https://aka.ms/MinecraftEULA>. **Se você aceitar**, crie `docker/data/eula.txt` com a linha
   `eula=true`. Nenhum script deste projeto cria esse arquivo por você.

4. **Arena (`.slime`)**: veja a seção abaixo. Sem o mapa-modelo o servidor sobe, mas `/hgc start` não consegue criar a arena.

5. **Subir o servidor** (a partir da pasta `docker/`):

   ```bash
   cd docker
   docker compose up --build
   ```

   Memória do JVM (padrão `2G`): `MEMORY=4G docker compose up --build`.

## Como conectar

No Minecraft Java **1.21.11**, adicione o servidor `localhost:25565` (ou só `localhost`).

## Testar Java 21 x Java 25

O padrão é Java 25. Para a mesma imagem com Java 21 (`eclipse-temurin:21-jre`):

```bash
JAVA_VERSION=21 docker compose up --build
```

O plugin tem bytecode Java 21, mas o ASP 4.2.0 pode exigir Java 25 — essa é justamente a dúvida que este teste responde
(se o servidor recusar subir com 21, o erro aparece no log do contêiner).

## Arena `.slime`

O plugin precisa de um mapa-modelo no formato **Slime**. Segundo o [README da raiz](../README.md) (seção "Arena") e o
`config.yml`, o arquivo esperado é `<raiz do servidor>/slime_worlds/hgmapa.slime` (nome e pasta configuráveis em
`arena.template-world` e `arena.slime-directory`). Aqui a raiz do servidor é `/server`, ou seja, no seu computador:
`docker/data/slime_worlds/hgmapa.slime`. **Confira o caminho e o formato exatos** no README da raiz e em
[`docs/testes/roteiro-manual.md`](../docs/testes/roteiro-manual.md), que traz o roteiro de teste completo (comandos `/hgc …`
e o resultado esperado de cada passo). Este documento não repete os comandos para não ficar desatualizado.

## Console, parar e logs

- Console do servidor: `docker compose attach mc` (para sair sem parar o servidor: `Ctrl-p` e depois `Ctrl-q`).
- Parar: `docker compose down`.
- Logs: `docker compose logs -f mc` ou `docker/data/logs/latest.log`.
- Os dados (mundo, configs, bancos dos addons) ficam em `docker/data/`, que está no `.gitignore`.

## Linux: permissões de `docker/data`

O contêiner roda como usuário não-root `mc` (UID/GID 1000 por padrão). Se o seu usuário não for 1000, use
`MC_UID=$(id -u) MC_GID=$(id -g) docker compose up --build`.

## Lobby (BungeeCord)

Ao terminar a partida o plugin envia os jogadores ao servidor `lobby` via BungeeCord (`ending.lobby-server`). Este setup tem
**um único servidor, sem proxy**; o comportamento nesse caso (fallback) deve ser conferido no teste manual.

## O que não foi testado

- Os arquivos desta pasta foram escritos sem rodar o Docker no ambiente de desenvolvimento: `docker compose config`, o build
  da imagem e o `docker compose up` **não** foram executados.
- O servidor nunca foi iniciado com o ASP real (o JAR não é baixado automaticamente), então não se sabe se o ASP 4.2.0
  sobe com Java 21; Java 25 também não foi validado em execução.
- Windows, Mac e as permissões de volume no Linux (UID/GID) não foram testados.
- O console via `docker compose attach` e o comportamento sem proxy BungeeCord não foram verificados.
- Foi testado apenas o `preparar.sh` em Linux (build e cópia dos JARs) e a sintaxe com `bash -n`.
