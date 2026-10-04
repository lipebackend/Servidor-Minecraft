#!/usr/bin/env bash
# al3HG - automação para Fedora: prepara tudo e sobe o servidor local de testes com Docker.
# NÃO instala o Docker (ele já deve estar instalado). Veja docker/README.md, seção "Fedora (automático)".
# Uso: bash fedora-setup.sh [--java 21|25] [--aceito-eula] [--sem-subir] [--ajuda]
# Variável opcional: DESTINO (pasta do clone; padrão ~/Servidor-Minecraft).
set -euo pipefail

REPO_URL="https://github.com/lipebackend/Servidor-Minecraft"
DESTINO="${DESTINO:-$HOME/Servidor-Minecraft}"
JAVA_VERSION="25"
ACEITO_EULA="n"
SUBIR="s"
# Prefixo do docker: vazio (usuário comum) ou "sudo" (se faltar permissão no socket do Docker).
PREFIXO_DOCKER=()

info() { printf '>> %s\n' "$*"; }
ok()   { printf 'OK: %s\n' "$*"; }
aviso() { printf 'AVISO: %s\n' "$*" >&2; }
erro() { printf 'ERRO: %s\n' "$*" >&2; exit 1; }

ajuda() {
  cat <<'MSG'
Uso: bash fedora-setup.sh [opções]

Prepara e sobe o servidor local do al3HG no Fedora (não instala o Docker).

Opções:
  --java 21|25    versão do Java da imagem Docker (padrão: 25)
  --aceito-eula   cria o eula.txt (eula=true). Significa que VOCÊ aceita https://aka.ms/MinecraftEULA
  --sem-subir     prepara tudo, mas não executa "docker compose up"
  --ajuda         mostra esta mensagem

Variável de ambiente: DESTINO = pasta do clone (padrão: ~/Servidor-Minecraft).
Pode rodar de novo quantas vezes quiser: o script só faz o que ainda falta.
MSG
}

ler_argumentos() {
  while [ "$#" -gt 0 ]; do
    case "$1" in
      --java) [ "$#" -ge 2 ] || erro "--java precisa de um valor (21 ou 25)."; JAVA_VERSION="$2"; shift 2 ;;
      --java=*) JAVA_VERSION="${1#--java=}"; shift ;;
      --aceito-eula) ACEITO_EULA="s"; shift ;;
      --sem-subir) SUBIR="n"; shift ;;
      --ajuda|-h|--help) ajuda; exit 0 ;;
      *) erro "opção desconhecida: $1 (use --ajuda)." ;;
    esac
  done
  case "$JAVA_VERSION" in
    21|25) ;;
    *) erro "--java aceita apenas 21 ou 25 (recebido: $JAVA_VERSION)." ;;
  esac
}

checar_sistema() {
  [ -r /etc/os-release ] || erro "/etc/os-release não encontrado: não consegui identificar a distribuição."
  # shellcheck disable=SC1091
  . /etc/os-release
  [ "${ID:-}" = "fedora" ] || erro "este script é só para Fedora (detectado: ${PRETTY_NAME:-desconhecido})."
  ok "Fedora detectado (${PRETTY_NAME:-Fedora})."
  [ "$(id -u)" -ne 0 ] || erro "não rode como root. Rode como seu usuário; o script usa sudo só quando precisa."
}

checar_docker() {
  command -v docker >/dev/null 2>&1 \
    || erro "'docker' não encontrado. Este script não instala o Docker; instale-o e rode de novo."
  local saida
  if saida="$(docker info 2>&1)"; then
    ok "Docker respondendo."
  elif printf '%s' "$saida" | grep -qi 'permission denied'; then
    command -v sudo >/dev/null 2>&1 || erro "sem permissão no Docker e 'sudo' não existe."
    info "Sem permissão para usar o Docker como usuário comum; tentando com sudo..."
    sudo docker info >/dev/null 2>&1 || erro "'sudo docker info' também falhou. Veja: sudo systemctl status docker"
    PREFIXO_DOCKER=(sudo)
    aviso "usando 'sudo docker'. Para usar sem sudo: sudo usermod -aG docker \"$USER\" e faça login de novo."
  else
    printf '%s\n' "$saida" >&2
    erro "o daemon do Docker não respondeu. Se estiver parado: sudo systemctl start docker"
  fi
  "${PREFIXO_DOCKER[@]}" docker compose version >/dev/null 2>&1 \
    || erro "'docker compose' (plugin Compose v2) não encontrado. Instale o plugin e rode de novo."
  ok "docker compose disponível."
}

# --- Dependências (git, curl, maven, JDK 25) ----------------------------------------------------
precisa_sudo() { command -v sudo >/dev/null 2>&1 || erro "'sudo' não encontrado, e ele é necessário para: $*"; }

instalar_pacotes() {
  local faltam=() cmd pkg
  for par in git:git curl:curl mvn:maven; do
    cmd="${par%%:*}"; pkg="${par##*:}"
    command -v "$cmd" >/dev/null 2>&1 && ok "$cmd já instalado." || faltam+=("$pkg")
  done
  [ "${#faltam[@]}" -eq 0 ] && return 0
  precisa_sudo "sudo dnf install ${faltam[*]}"
  info "Instalando via dnf: ${faltam[*]}"
  sudo dnf install -y "${faltam[@]}"
}

# Imprime a versão principal do javac em $1/bin/javac (vazio se não houver).
versao_jdk() {
  [ -x "$1/bin/javac" ] || return 0
  "$1/bin/javac" -version 2>&1 | sed -n 's/^javac \([0-9]\+\).*/\1/p'
}

# Procura um JDK 25 já instalado e deixa o caminho em JAVA_HOME (retorna 1 se não achar).
achar_jdk25() {
  local candidato versao
  for candidato in "${JAVA_HOME:-}" "$HOME/.local/jdk25" /usr/lib/jvm/java-25-openjdk* /usr/lib/jvm/temurin-25*; do
    [ -n "$candidato" ] || continue
    versao="$(versao_jdk "$candidato")"
    if [ -n "$versao" ] && [ "$versao" -ge 25 ]; then JAVA_HOME="$candidato"; export JAVA_HOME; return 0; fi
  done
  return 1
}

baixar_temurin() {
  local arq alvo="$HOME/.local/jdk25" tmp
  case "$(uname -m)" in
    x86_64) arq="x64" ;;
    aarch64) arq="aarch64" ;;
    *) erro "arquitetura não suportada para o Temurin: $(uname -m)" ;;
  esac
  tmp="$(mktemp -d)"
  info "Baixando o Temurin 25 JDK ($arq) do Adoptium para $alvo ..."
  curl -fSL "https://api.adoptium.net/v3/binary/latest/25/ga/linux/$arq/jdk/hotspot/normal/eclipse" -o "$tmp/jdk.tar.gz" \
    || erro "falha ao baixar o Temurin 25."
  mkdir -p "$tmp/jdk" "$(dirname "$alvo")"
  tar -xzf "$tmp/jdk.tar.gz" -C "$tmp/jdk" --strip-components=1
  rm -rf "$alvo" && mv "$tmp/jdk" "$alvo" && rm -rf "$tmp"
}

garantir_jdk25() {
  if achar_jdk25; then ok "JDK 25 utilizável em $JAVA_HOME."; return 0; fi
  if dnf info java-25-openjdk-devel >/dev/null 2>&1; then
    precisa_sudo "sudo dnf install java-25-openjdk-devel"
    info "Instalando java-25-openjdk-devel via dnf..."
    sudo dnf install -y java-25-openjdk-devel
  else
    info "java-25-openjdk-devel não existe no dnf desta versão do Fedora; usando o Temurin."
    baixar_temurin
  fi
  achar_jdk25 || erro "o JDK 25 não foi encontrado depois da instalação."
  ok "JAVA_HOME=$JAVA_HOME (válido só durante este script; para o seu terminal: export JAVA_HOME=$JAVA_HOME)."
}

# --- Repositório e build ------------------------------------------------------------------------
clonar_ou_atualizar() {
  if [ -d "$DESTINO/.git" ]; then
    info "Atualizando $DESTINO (git pull)..."
    git -C "$DESTINO" pull --ff-only || aviso "git pull falhou (alterações locais?); seguindo com o que já está em $DESTINO."
  elif [ -e "$DESTINO" ] && [ -n "$(ls -A "$DESTINO" 2>/dev/null)" ]; then
    erro "$DESTINO existe, não está vazio e não é um clone git. Use DESTINO=<outra pasta> ou remova-o."
  else
    info "Clonando $REPO_URL em $DESTINO ..."
    git clone "$REPO_URL" "$DESTINO"
  fi
  DATA="$DESTINO/docker/data"
}

compilar() {
  info "Rodando docker/preparar.sh (compila e copia os JARs)..."
  bash "$DESTINO/docker/preparar.sh"
}

# --- Subir o servidor ---------------------------------------------------------------------------
subir_servidor() {
  local docker_cmd="docker"
  [ "${#PREFIXO_DOCKER[@]}" -eq 0 ] || docker_cmd="sudo docker"
  if [ "$SUBIR" != "s" ]; then
    info "--sem-subir: tudo pronto. Para subir depois: cd $DESTINO/docker && JAVA_VERSION=$JAVA_VERSION $docker_cmd compose up --build -d"
    return 0
  fi
  info "Subindo o servidor (docker compose up --build -d, Java $JAVA_VERSION)..."
  (cd "$DESTINO/docker" && "${PREFIXO_DOCKER[@]}" env JAVA_VERSION="$JAVA_VERSION" MC_UID="$(id -u)" MC_GID="$(id -g)" \
    docker compose up --build -d)
  cat <<MSG

Servidor iniciado (Java $JAVA_VERSION).
  Ver logs:  cd $DESTINO/docker && $docker_cmd compose logs -f
  Conectar:  localhost:25565 (Minecraft Java 1.21.11)
  Parar:     cd $DESTINO/docker && $docker_cmd compose down
MSG
}

main() {
  ler_argumentos "$@"
  checar_sistema
  checar_docker
  instalar_pacotes
  garantir_jdk25
  clonar_ou_atualizar
  compilar
  subir_servidor
}

main "$@"
