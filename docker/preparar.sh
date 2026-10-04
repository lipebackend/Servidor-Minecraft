#!/usr/bin/env bash
# Prepara docker/data para o servidor local: compila o projeto e copia os JARs para docker/data/plugins.
# Não baixa o AdvancedSlimePaper e não cria o eula.txt (veja as instruções impressas no final).
# Uso: ./docker/preparar.sh   (de qualquer pasta; use Git Bash ou WSL no Windows)
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DATA="$RAIZ/docker/data"
PLUGINS="$DATA/plugins"

# --- 1. Compilar ----------------------------------------------------------------------------------
# O build exige JDK 25+ (o JAR do ASP é compilado para Java 25). Se JAVA_HOME estiver definido, o Maven o usa.
if ! command -v mvn >/dev/null 2>&1; then
  echo "ERRO: 'mvn' (Maven) não encontrado no PATH. Instale o Maven e um JDK 25+ e tente de novo." >&2
  exit 1
fi
if [ -n "${JAVA_HOME:-}" ]; then
  echo ">> Usando JAVA_HOME=$JAVA_HOME"
else
  echo ">> JAVA_HOME não definido: o Maven usará o 'java' do PATH (precisa ser JDK 25+)."
fi
echo ">> Compilando (mvn -q clean package -DskipTests)..."
(cd "$RAIZ" && mvn -q clean package -DskipTests)

# --- 2. Copiar os JARs dos plugins ----------------------------------------------------------------
mkdir -p "$PLUGINS"
echo ">> Copiando JARs para $PLUGINS"
copiados=0
for jar in "$RAIZ"/al3hg-plugin/target/*.jar "$RAIZ"/addons/*/target/*.jar; do
  [ -e "$jar" ] || continue
  nome="$(basename "$jar")"
  case "$nome" in
    original-*|*-sources.jar|*-javadoc.jar) continue ;;
  esac
  cp -f "$jar" "$PLUGINS/$nome"
  echo "   + $nome"
  copiados=$((copiados + 1))
done
if [ "$copiados" -eq 0 ]; then
  echo "ERRO: nenhum JAR encontrado em al3hg-plugin/target nem em addons/*/target." >&2
  exit 1
fi

# --- 3. Conferir o server.jar (AdvancedSlimePaper) ------------------------------------------------
echo
if [ -f "$DATA/server.jar" ]; then
  echo "OK: $DATA/server.jar encontrado."
else
  cat <<MSG
FALTA o server.jar (AdvancedSlimePaper). Este script NÃO o baixa.
  1. Abra https://infernalsuite.com/download/asp e baixe o AdvancedSlimePaper para o Minecraft 1.21.11
     (versão 4.2.0, a que o projeto usa).
  2. Salve o arquivo como: $DATA/server.jar
  3. Rode este script de novo para conferir.
MSG
fi

# --- 4. EULA --------------------------------------------------------------------------------------
if [ -f "$DATA/eula.txt" ]; then
  echo "OK: $DATA/eula.txt existe (o conteúdo é responsabilidade sua)."
else
  cat <<MSG

EULA da Mojang: este script NÃO cria o eula.txt, porque aceitar o EULA é uma decisão sua.
Leia https://aka.ms/MinecraftEULA e, SE aceitar, crie o arquivo $DATA/eula.txt com a linha:
  eula=true
MSG
fi

echo
echo "Próximo passo (com server.jar e eula.txt no lugar): cd docker && docker compose up --build"
