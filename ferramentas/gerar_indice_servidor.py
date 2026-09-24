#!/usr/bin/env python3
"""Gera servidor/midias.json, o índice que o app lê para achar letras e áudios no servidor.

Coloque os arquivos nomeados pelo número da música e rode:
    python3 ferramentas/gerar_indice_servidor.py

    servidor/letras/1001.lrc
    servidor/audio/1001.mp3      (ou .m4a, .ogg, .wav)

Para áudios hospedados em outro lugar (Google Drive, S3, seu site...), liste as URLs em
servidor/links_externos.json:
    {"1001": {"audio": "https://meusite.com/playbacks/evidencias.mp3"}}
"""
import json
from pathlib import Path

SERVIDOR = Path(__file__).resolve().parent.parent / "servidor"
EXTENSOES_AUDIO = (".mp3", ".m4a", ".ogg", ".wav")


def main() -> None:
    musicas: dict[str, dict[str, str]] = {}

    for arquivo in sorted((SERVIDOR / "letras").glob("*.lrc")):
        if arquivo.stem.isdigit():
            musicas.setdefault(arquivo.stem, {})["letra"] = f"letras/{arquivo.name}"

    for arquivo in sorted((SERVIDOR / "audio").iterdir()):
        if arquivo.suffix.lower() in EXTENSOES_AUDIO and arquivo.stem.isdigit():
            musicas.setdefault(arquivo.stem, {})["audio"] = f"audio/{arquivo.name}"

    externos = SERVIDOR / "links_externos.json"
    if externos.exists():
        for codigo, midias in json.loads(externos.read_text(encoding="utf-8")).items():
            musicas.setdefault(codigo, {}).update(midias)

    ordenadas = dict(sorted(musicas.items(), key=lambda item: int(item[0])))
    destino = SERVIDOR / "midias.json"
    destino.write_text(
        json.dumps({"musicas": ordenadas}, ensure_ascii=False, indent=1) + "\n", encoding="utf-8"
    )
    print(f"{len(ordenadas)} músicas no índice {destino}")


if __name__ == "__main__":
    main()
