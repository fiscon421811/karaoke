#!/usr/bin/env python3
"""Importa os SEUS arquivos MP3 para o Karaokê Brasil, já com a letra sincronizada.

Para cada MP3 da pasta indicada:
  1. descobre título e artista (tags ID3 ou nome "Artista - Título.mp3");
  2. encontra a música no catálogo (servidor/catalogo.json) e pega o número dela;
  3. copia o áudio como <numero>.mp3;
  4. baixa do LRCLIB a letra sincronizada da gravação com a MESMA duração do MP3
     e salva como <numero>.lrc.

Uso:
    pip install mutagen
    python3 ferramentas/importar_mp3.py "C:/Users/edson/Music/Karaoke"
    python3 ferramentas/importar_mp3.py "C:/Users/edson/Music/Karaoke" --adb   # já envia para a TV

O resultado vai para a pasta minhas_musicas/ (ignorada pelo Git). Com --adb, os arquivos são
copiados para a TV conectada (adb connect <ip-da-tv>), na pasta que o app lê primeiro.

Não publique MP3 de músicas comerciais no repositório: ele é público.
"""
import argparse
import json
import shutil
import subprocess
import sys
import unicodedata
import urllib.parse
import urllib.request
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
CATALOGO = RAIZ / "servidor" / "catalogo.json"
PASTA_NA_TV = "/sdcard/Android/data/br.com.karaokebrasil/files/karaoke/"
LRCLIB = "https://lrclib.net/api/search"
USER_AGENT = "KaraokeBrasil/1.0 (https://github.com/fiscon421811/karaoke)"
TOLERANCIA_DURACAO_S = 3.0


def normalizar(texto: str) -> str:
    sem_acento = unicodedata.normalize("NFD", texto)
    sem_acento = "".join(c for c in sem_acento if not unicodedata.combining(c))
    return " ".join("".join(c if c.isalnum() else " " for c in sem_acento.lower()).split())


def ler_mp3(arquivo: Path) -> tuple[str, str, float | None]:
    """Devolve (título, artista, duração em segundos) do MP3."""
    titulo = artista = ""
    duracao = None
    try:
        from mutagen import File as MutagenFile

        audio = MutagenFile(arquivo, easy=True)
        if audio is not None:
            duracao = float(audio.info.length)
            titulo = (audio.get("title") or [""])[0]
            artista = (audio.get("artist") or [""])[0]
    except ImportError:
        sys.exit("Instale a biblioteca de tags: pip install mutagen")
    except Exception as erro:  # arquivo corrompido, tags estranhas...
        print(f"  ! não consegui ler as tags de {arquivo.name}: {erro}")
    if not titulo and " - " in arquivo.stem:
        artista, titulo = (parte.strip() for parte in arquivo.stem.split(" - ", 1))
    return titulo or arquivo.stem, artista, duracao


def encontrar(musicas: list[dict], titulo: str, artista: str) -> dict | None:
    """Casa título + artista com o catálogo, tolerando acentos e artistas "feat."."""
    alvo_titulo, alvo_artista = normalizar(titulo), normalizar(artista)
    mesmo_titulo = [m for m in musicas if normalizar(m["titulo"]) == alvo_titulo]
    if alvo_artista:
        for m in mesmo_titulo:
            art = normalizar(m["artista"])
            if art in alvo_artista or alvo_artista in art:
                return m
    return mesmo_titulo[0] if len(mesmo_titulo) == 1 else None


def buscar_letra(titulo: str, artista: str, duracao: float | None) -> tuple[str | None, str]:
    """Busca no LRCLIB a letra sincronizada cuja duração bate com a do MP3."""
    consulta = urllib.parse.urlencode({"track_name": titulo, "artist_name": artista})
    pedido = urllib.request.Request(f"{LRCLIB}?{consulta}", headers={"User-Agent": USER_AGENT})
    try:
        with urllib.request.urlopen(pedido, timeout=15) as resposta:
            resultados = json.load(resposta)
    except Exception as erro:
        return None, f"erro ao consultar o LRCLIB: {erro}"

    sincronizadas = [r for r in resultados if r.get("syncedLyrics")]
    if not sincronizadas:
        return None, "sem letra sincronizada no LRCLIB"
    if duracao is None:
        return sincronizadas[0]["syncedLyrics"], "duração desconhecida; confira a sincronia"

    melhor = min(sincronizadas, key=lambda r: abs((r.get("duration") or 0) - duracao))
    diferenca = abs((melhor.get("duration") or 0) - duracao)
    if diferenca <= TOLERANCIA_DURACAO_S:
        return melhor["syncedLyrics"], "sincronizada com esta gravação"
    return melhor["syncedLyrics"], f"gravação diferente ({diferenca:.0f}s de diferença); confira a sincronia"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("pasta", type=Path, help="pasta com os seus arquivos .mp3")
    parser.add_argument("--saida", type=Path, default=RAIZ / "minhas_musicas")
    parser.add_argument("--adb", action="store_true", help="enviar os arquivos para a TV via adb")
    args = parser.parse_args()

    musicas = json.loads(CATALOGO.read_text(encoding="utf-8"))["musicas"]
    args.saida.mkdir(parents=True, exist_ok=True)
    arquivos = sorted(p for p in args.pasta.rglob("*") if p.suffix.lower() == ".mp3")
    if not arquivos:
        sys.exit(f"Nenhum .mp3 encontrado em {args.pasta}")

    importadas, sem_catalogo, sem_letra = 0, [], []
    for arquivo in arquivos:
        titulo, artista, duracao = ler_mp3(arquivo)
        musica = encontrar(musicas, titulo, artista)
        if musica is None:
            sem_catalogo.append(f"{arquivo.name}  (título: {titulo!r}, artista: {artista!r})")
            continue

        codigo = musica["codigo"]
        shutil.copy2(arquivo, args.saida / f"{codigo}.mp3")
        letra, situacao = buscar_letra(musica["titulo"], musica["artista"], duracao)
        if letra:
            (args.saida / f"{codigo}.lrc").write_text(letra, encoding="utf-8")
        else:
            sem_letra.append(f"{codigo} {musica['titulo']} — {situacao}")
        importadas += 1
        print(f"✔ {codigo}  {musica['titulo']} — {musica['artista']}  [{situacao}]")

    print(f"\n{importadas} música(s) importada(s) para {args.saida}")
    if sem_letra:
        print("\nSem letra (o áudio toca, mas sem letra na tela):")
        print("\n".join(f"  - {item}" for item in sem_letra))
    if sem_catalogo:
        print("\nNão encontradas no catálogo (inclua-as em ferramentas/gerar_catalogo.py ou renomeie o")
        print('arquivo para "Artista - Título.mp3" igual ao catálogo):')
        print("\n".join(f"  - {item}" for item in sem_catalogo))

    if args.adb and importadas:
        print(f"\nEnviando para a TV ({PASTA_NA_TV})...")
        subprocess.run(["adb", "shell", "mkdir", "-p", PASTA_NA_TV], check=True)
        for arquivo in sorted(args.saida.iterdir()):
            if arquivo.suffix in (".mp3", ".lrc"):
                subprocess.run(["adb", "push", str(arquivo), PASTA_NA_TV], check=True)
        print("Pronto! Abra o app na TV.")


if __name__ == "__main__":
    main()
