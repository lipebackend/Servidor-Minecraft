/**
 * Nucleo do addon de estatisticas: sem Bukkit/Paper e sem al3hg-api.
 *
 * <p>A camada de adaptacao fica em {@code al3ncar.al3hg.addon.stats.bukkit}: o
 * {@code HgApiAdapter} (unica classe que importa a al3hg-api) repassa os eventos ao
 * {@code StatsListener}, que usa {@code StatsRules} + {@code StatsRepository} via
 * {@code StatsService}, e os comandos voltam a thread principal pelo scheduler antes de
 * responder ao jogador.
 *
 * <p>sqlite-jdbc NAO usa shade/relocate: o {@code plugin.yml} declara
 * {@code libraries: org.xerial:sqlite-jdbc:<versao>} (a versao vem da property
 * {@code sqlite-jdbc.version} do addons/pom.xml) e o pom o mantem como {@code provided}.
 */
package al3ncar.al3hg.addon.stats;
