# al3HG em Docker (somente máquina local)

Roda o servidor **Paper 1.21.11 via AdvancedSlimePaper (ASP) 4.2.0** com os plugins deste repositório (`al3hg` e os addons)
dentro de um contêiner, na **sua máquina** (Linux, Windows ou Mac). Não é uma configuração para hospedagem/produção.

## Pré-requisitos

- **Docker** com o plugin Compose (`docker compose`): Docker Desktop (Windows/Mac) ou Docker Engine (Linux).
- Para o `preparar.sh`: **bash** (no Windows, use Git Bash ou WSL), **Maven** e um **JDK 25+** (o build exige; ver "Build" no
  [README da raiz](../README.md)). Se `JAVA_HOME` estiver definido, ele é usado.
- Acesso à internet: o addon de estatísticas baixa o `sqlite-jdbc` do Maven Central ao carregar (declarado em `libraries:` no `plugin.yml`).
- Uma conta Minecraft Java Edition **1.21.11** para entrar no servidor.

## Fedora (automático)

No Fedora, o `fedora-setup.sh` faz o caminho inteiro (o Docker precisa **já estar instalado**; o script não o instala):

```bash
curl -fsSL https://raw.githubusercontent.com/lipebackend/Servidor-Minecraft/main/docker/fedora-setup.sh -o fedora-setup.sh && bash fedora-setup.sh
```

Opções: `--java 21|25` (padrão 25), `--sem-subir` (prepara tudo sem `docker compose up`), `--ajuda`. Pode rodar de novo à vontade;
`DESTINO=<pasta>` troca onde o repositório é clonado (padrão `~/Servidor-Minecraft`).

O que ele faz: confere Fedora, Docker e `docker compose` (se faltar permissão, usa `sudo docker`); instala via `dnf` só o que falta
(`git`, `curl`, `maven`, `python3`) e o JDK 25 (`java-25-openjdk-devel`, ou o Temurin 25 em `~/.local/jdk25`); clona/atualiza o
repositório; baixa o `server.jar` (AdvancedSlimePaper 1.21.11, branch `main`, pela API de download do InfernalSuite, com SHA-256);
gera uma **arena de teste** plana (`gerar-arena.py`), converte com o importer do ASP para `docker/data/slime_worlds/hgmapa.slime`;
roda o `preparar.sh` e sobe o contêiner.

**EULA:** o script cria `docker/data/eula.txt` com `eula=true` e avisa antes e depois. **Rodar o script significa aceitar** o
<https://aka.ms/MinecraftEULA>; se não aceita, não rode. (`--aceito-eula` existe só por compatibilidade e não muda nada.)

O que o script **não** faz: instalar o Docker; dar permissão do seu usuário ao Docker (ele avisa o `usermod -aG docker`); trocar a
arena de teste por um mapa de verdade (coloque o seu `.slime` em `docker/data/slime_worlds/hgmapa.slime`; o script não sobrescreve um
arquivo existente); abrir a porta no firewall; configurar lobby/BungeeCord.

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

- `docker compose config`, o build da imagem e o `docker compose up` **não** foram executados (sem Docker no ambiente de desenvolvimento),
  nem o `fedora-setup.sh` em um Fedora de verdade (só `bash -n`, `shellcheck` e uma execução com `docker`/`dnf`/`/etc/os-release`
  simulados em Linux não-Fedora; a instalação via `dnf` nunca rodou).
- Fora do Docker, o `server.jar` baixado (ASP `main`, 1.21.11) **iniciou com Java 21**, carregou o plugin `al3hg`, leu o
  `hgmapa.slime` gerado pelo script e `/hgc fs` clonou a arena; sem jogador conectado, a partida terminou sem vencedor. Java 25 (padrão da
  imagem) não foi exercitado no servidor; o importer rodou com JDK 25.
- Windows, Mac e as permissões de volume no Linux (UID/GID, SELinux com `:z`) não foram testados.
- O console via `docker compose attach` e o comportamento sem proxy BungeeCord não foram verificados.
- A arena de teste é só um piso plano de 256x256 blocos: não tem baús nem pontos de spawn, serve para validar o fluxo.
