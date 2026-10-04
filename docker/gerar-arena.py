#!/usr/bin/env python3
"""Gera um mundo vanilla (Anvil) pequeno e plano, só com a biblioteca padrão do Python.

É a "arena de teste" do fedora-setup.sh: 16x16 chunks (256x256 blocos) de grama sobre pedra, sem construções.
Serve para validar o fluxo (importar para .slime e carregar no ASP); para jogar de verdade use um mapa seu.
Uso: python3 gerar-arena.py <pasta-do-mundo>
"""
import gzip
import os
import struct
import sys
import zlib

DATA_VERSION = 4671  # Minecraft 1.21.11
SECOES_MIN_Y = -4    # seção mais baixa (y = -64)
SECOES = 24          # mundo completo (y -64..319); só as duas de baixo têm blocos: pedra e uma camada de grama em y=-48
RAIO = 8             # chunks de -8 a 7 em X e Z


def nome(n):
    b = n.encode("utf-8")
    return struct.pack(">H", len(b)) + b


def tag(tipo, n, corpo):
    return struct.pack(">B", tipo) + nome(n) + corpo


def t_byte(n, v): return tag(1, n, struct.pack(">b", v))
def t_int(n, v): return tag(3, n, struct.pack(">i", v))
def t_long(n, v): return tag(4, n, struct.pack(">q", v))
def t_str(n, v): return tag(8, n, nome(v))
def t_longs(n, vs): return tag(12, n, struct.pack(">i", len(vs)) + struct.pack(">%dq" % len(vs), *vs))


def t_lista(n, tipo, itens):
    return tag(9, n, struct.pack(">Bi", tipo, len(itens)) + b"".join(itens))


def t_comp(n, filhos): return tag(10, n, b"".join(filhos) + b"\x00")


def secao(y, blocos):
    """blocos: lista de nomes na paleta; se tiver 2, a camada local y=0 usa o índice 1 (resto 0)."""
    paleta = [t_comp("", [t_str("Name", b)])[3:] for b in blocos]  # itens de lista não levam tipo/nome
    estado = [t_lista("palette", 10, paleta)]
    if len(blocos) > 1:
        estado.append(t_longs("data", [0x1111111111111111] * 16 + [0] * 48))  # 4 bits/bloco, 256 blocos = 16 longs
    bioma = [t_lista("palette", 8, [nome("minecraft:plains")])]
    return t_comp("", [t_byte("Y", y), t_comp("block_states", estado), t_comp("biomes", bioma)])[3:]


def chunk(cx, cz):
    secoes = [secao(SECOES_MIN_Y, ["minecraft:stone"]),
              secao(SECOES_MIN_Y + 1, ["minecraft:air", "minecraft:grass_block"])]
    secoes += [secao(SECOES_MIN_Y + i, ["minecraft:air"]) for i in range(2, SECOES)]  # o ASP exige todas as seções
    corpo = [t_int("DataVersion", DATA_VERSION), t_int("xPos", cx), t_int("zPos", cz), t_int("yPos", SECOES_MIN_Y),
             t_str("Status", "minecraft:full"), t_long("LastUpdate", 0), t_long("InhabitedTime", 0),
             t_lista("sections", 10, secoes), t_lista("block_entities", 10, [])]
    return t_comp("", corpo)


def escrever_regioes(pasta):
    os.makedirs(os.path.join(pasta, "region"), exist_ok=True)
    regioes = {}
    for cx in range(-RAIO, RAIO):
        for cz in range(-RAIO, RAIO):
            regioes.setdefault((cx >> 5, cz >> 5), []).append((cx, cz))
    for (rx, rz), chunks in regioes.items():
        cabecalho, dados, setor = bytearray(4096), bytearray(), 2
        for cx, cz in chunks:
            z = zlib.compress(chunk(cx, cz))
            bloco = struct.pack(">iB", len(z) + 1, 2) + z
            bloco += b"\x00" * (-len(bloco) % 4096)
            idx = 4 * ((cx & 31) + (cz & 31) * 32)
            cabecalho[idx:idx + 4] = struct.pack(">I", (setor << 8) | (len(bloco) // 4096))
            dados += bloco
            setor += len(bloco) // 4096
        with open(os.path.join(pasta, "region", "r.%d.%d.mca" % (rx, rz)), "wb") as f:
            f.write(bytes(cabecalho) + bytes(4096) + bytes(dados))


def escrever_level_dat(pasta):
    data = [t_int("DataVersion", DATA_VERSION), t_int("version", 19133), t_str("LevelName", os.path.basename(pasta)),
            t_int("SpawnX", 0), t_int("SpawnY", -47), t_int("SpawnZ", 0)]
    raiz = t_comp("", [t_comp("Data", data)])
    with open(os.path.join(pasta, "level.dat"), "wb") as f:
        f.write(gzip.compress(raiz))


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit("Uso: python3 gerar-arena.py <pasta-do-mundo>")
    destino = os.path.abspath(sys.argv[1])
    os.makedirs(destino, exist_ok=True)
    escrever_regioes(destino)
    escrever_level_dat(destino)
    print("Mundo vanilla gerado em", destino)
