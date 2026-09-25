# SopaRegemClick

Plugin Minecraft PaperMC que adiciona um mecânico de sopa de cogumelo que cura o jogador ao clicar com o botão direito.

## Visão Geral

**SopaRegemClick** é um plugin Minecraft para PaperMC que cura jogadores quando eles clicam com o botão direito segurando **Sopa de Cogumelo** (Mushroom Stew), restaurando 3 pontos de saúde.

## Tecnologias

| Tecnologia | Descrição |
|------------|-----------|
| **Java 21** | Versão do JDK usada no projeto |
| **Maven** | Sistema de build e gerenciamento de dependências |
| **PaperMC API** | API Bukkit otimizada para servidores Paper |
| **Bukkit API** | Framework para desenvolvimento de plugins Minecraft |

## Funcionalidades

- **Cura ao clicar**: Quando um jogador clicar (clique direito) com **Sopa de Cogumelo** na mão, sua saúde aumenta em 3 pontos (até o máximo)
- **Comando `/sopa`**: Comando administrativo para recarregar configurações
- **Prefixo personalizável**: Mensagens formatadas com códigos de cor §

## Arquitetura do Código

```
src/main/java/
├── al3ncar.sopaRegemClick/
│   ├── SopaRegemClick.java    # Classe principal (JavaPlugin)
│   ├── command/SopaReload.java # Executor de comandos
│   ├── events/Regem.java      # Listener de eventos (PlayerInteractEvent)
│   └── utils/PrefixoC.java    # Classe utilitária de formatação
```

### Classes Principais

1. **SopaRegemClick.java** - Classe principal que estende `JavaPlugin` e implementa `Listener`. Registra o comando `/sopa` e o evento de jogador.

2. **Regem.java** - Event listener que detecta quando um jogador interage com o item. Lógica:
   - Verifica se o item é Mushroom Stew
   - Verifica se é clique direito (AR ou BLOCK)
   - Aumenta saúde em 3.0 (`player.getHealth() + 3.0`)
   - Decrementa a quantidade da sopa em 1
   - Devolve um novo item de sopa para o inventário
   - Cancela o evento para evitar comportamento padrão

3. **SopaReload.java** - CommandExecutor que trata o comando `/sopa`. Requer permissão `hgsopa.admin`. Suporta subcomando `reload`.

4. **PrefixoC.java** - Classe utilitária que retorna um prefixo formatado `§1[...] ` para mensagens de chat.

## Comandos

| Comando | Permissão | Descrição |
|---------|-----------|-----------|
| `/sopa` | `hgsopa.admin` | Recarrega as configurações do plugin |

## Permissões

- `hgsopa.admin` - Permite usar o comando `/sopa`

## Configuração

O plugin não requer arquivo de configuração externo. Todas as configurações são feitas por meio do comando `/sopa reload`.

## Build e Deploy

```bash
mvn clean package
```

O JAR será gerado em `target/soparegemclick-1.0.1.jar`.

## Autor

al3ncar