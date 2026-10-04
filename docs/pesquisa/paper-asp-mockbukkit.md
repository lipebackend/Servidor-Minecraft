# Pesquisa: Paper, AdvancedSlimePaper e MockBukkit

Notas de pesquisa para quem desenvolve plugins e addons do servidor (Minecraft 1.21.11).
Fontes oficiais linkadas em cada seção. Itens não confirmados estão marcados.

## 1. Paper

- Plugins novos podem usar `paper-plugin.yml`, mas plugins que dependem de `getCommand()` no `onEnable` devem registrar comandos por código (Brigadier). Os addons deste projeto usam `plugin.yml` com `depend: [Al3HG]`.
- Defina sempre `api-version` no descritor, senão o plugin carrega como legado com aviso no console.
- A API do servidor só pode ser usada na thread principal. I/O (SQLite, arquivos) deve ser assíncrono e voltar à thread principal pelo scheduler.
- Documentação: https://docs.papermc.io/paper/dev/getting-started/paper-plugins/
- Observação: a documentação atual do Paper já descreve a versão `26.x` (Java 25). Este projeto usa Paper 1.21.11 para casar com o ASP; veja a seção 2 sobre a versão de Java.

## 2. AdvancedSlimePaper (ASP) 4.2.0-SNAPSHOT

O ASP é um fork do Paper. A API já vem no servidor, então o plugin Slime World Plugin é opcional.

- Branch principal do ASP: Minecraft 1.21.11, `aspApiVersion=4.2.0-SNAPSHOT` (https://github.com/InfernalSuite/AdvancedSlimePaper/blob/main/gradle.properties).
- Dependência Maven (escopo `provided`):
  - repositório `https://repo.infernalsuite.com/repository/maven-snapshots/`
  - `com.infernalsuite.asp:api:4.2.0-SNAPSHOT`
- Pacote de imports: `com.infernalsuite.asp.api` (o antigo `aswm` não vale mais).
- Por ser SNAPSHOT, congele o JAR do servidor usado nos testes.
- Confirmado pelo build da fase 1: a API do ASP 4.2.0-SNAPSHOT é bytecode class 69 (Java 25). O projeto precisa de JDK 25 para compilar e gera bytecode Java 21.
- Risco em aberto, não verificado: se o JAR do servidor ASP 1.21.11 também exigir Java 25 em execução, o servidor de produção e o de testes precisam rodar JDK 25. Conferir a versão de Java exigida pelo build do servidor em https://infernalsuite.com/download/asp antes do deploy.

### Ciclo de vida de um mundo de partida

1. `onEnable`: ler o mapa-modelo uma vez, somente leitura: `api.readWorld(loader, "arena", true, props)` (pode rodar fora da thread principal).
2. A cada partida: `template.clone("hg-" + matchId, loader)`.
3. Carregar a cópia com `api.loadWorld(clone, true)`. Este passo precisa rodar na thread principal.
4. Fim da partida: enviar todos ao lobby e só então `Bukkit.unloadWorld(nome, false)`. O mundo é descartável, não há o que salvar.

### Cuidados

- No `onDisable`, descarte os mundos carregados pelo plugin, senão ocorre erro de "zip closed". Use `unloadWorld(nome, false)`; `true` salva de forma assíncrona e pode não terminar antes de o classloader fechar (https://github.com/InfernalSuite/homepage/blob/main/docs_asp/api/common_issues.md).
- Depois do unload, não guarde referências a `World`, `Chunk`, `Location`, tasks ou listeners do mundo antigo: isso mantém o mundo na memória (https://github.com/InfernalSuite/AdvancedSlimePaper/issues/201). O estado da partida guarda só `matchId` e o nome do mundo.
- Isso elimina o `shutdown()` do `/hgc stop` e do `/hgc rest` e o `limparMapa`: basta descartar o mundo e criar outro.

## 3. MockBukkit

- Artefato da linha 1.21: `org.mockbukkit.mockbukkit:mockbukkit-v1.21`. A versão mais recente que encontrei no Maven Central foi `4.116.3` (ago/2026).
- A versão do `paper-api` de teste deve ser a que o manifesto do JAR do MockBukkit declara (o README mostra como extraí-la no Maven).
- Não confirmado: que essa versão coincide exatamente com a 1.21.11 do servidor. Conferir no primeiro build.
- O MockBukkit simula o Paper puro e não conhece o ASP: o `ArenaProvider` precisa de uma implementação falsa nos testes. O carregamento real de mundos `.slime` entra no roteiro de teste manual no servidor.
