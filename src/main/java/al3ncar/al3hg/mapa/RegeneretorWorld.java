public static boolean limparMapa(Plugin plugin, String nomeMundo) {
    World world = Bukkit.getWorld(nomeMundo);
    if (world == null) return false;

    // Nunca tente regenerar os mundos padrão do servidor
    if (world.equals(Bukkit.getWorlds().get(0))) {
        plugin.getLogger().warning("Não é possível regenerar o mundo principal.");
        return false;
    }

    World destino = Bukkit.getWorlds().stream()
            .filter(w -> !w.equals(world))
            .findFirst()
            .orElse(null);
    if (destino == null) return false;

    for (Player p : world.getPlayers()) {
        p.teleport(destino.getSpawnLocation());
    }

    Path pasta = world.getWorldFolder().toPath();

    if (!Bukkit.unloadWorld(world, false)) {
        plugin.getLogger().warning("Falha ao descarregar mundo: " + nomeMundo);
        return false;
    }

    // Remove lock explicitamente
    try {
        Files.deleteIfExists(pasta.resolve("session.lock"));
    } catch (IOException e) {
        plugin.getLogger().warning("Não foi possível remover session.lock: " + e.getMessage());
    }

    // Deleta de forma assíncrona
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try (var s = Files.walk(pasta)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); }
                catch (IOException e) { e.printStackTrace(); }
            });
            plugin.getLogger().info("Mundo " + nomeMundo + " regenerado com sucesso.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    return true;
}
