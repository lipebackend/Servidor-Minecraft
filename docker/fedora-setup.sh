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

main() {
  ler_argumentos "$@"
  checar_sistema
  checar_docker
}

main "$@"
