# 🎤 Karaokê Brasil — Android TV

App de karaokê para **Android TV**, escrito em **Kotlin** com **Jetpack Compose for TV**,
com banco de músicas brasileiras organizado por gênero.

## Funcionalidades

- **Catálogo por gênero** (166 músicas, 12 gêneros): Sertanejo, MPB, Samba, Pagode, Forró, Axé,
  Rock Nacional, Bossa Nova, Funk, Gospel, Brega & Românticas, Folclore & Infantil.
- **Banco de dados local (Room/SQLite)** importado de `assets/catalogo.json`.
- Cada música tem um **número** (como nas máquinas de karaokê): `1001` = 1ª de Sertanejo, `7003` = 3ª de Rock…
- **Busca** por título, artista (sem precisar de acento: `forro` acha "Forró") ou número.
- **Fila de músicas** para a festa, **Favoritas** e **Mais cantadas**.
- **Player de karaokê** com letra sincronizada (formato `.lrc`): a linha atual vai sendo "pintada"
  de amarelo, com contagem regressiva antes do início e prévia das próximas linhas.
- Toca o áudio (playback) com **Media3/ExoPlayer**; sem áudio, roda em **modo demonstração**
  (a letra avança sozinha).
- Controle total pelo **controle remoto** (D-pad):

  | Tecla | Ação no player |
  |---|---|
  | OK / Play-Pause | pausar / continuar |
  | ◀ / ▶ | voltar / avançar 5 s |
  | ▼ / Próxima | pular para a próxima da fila |
  | Voltar | sair |

## Letras e áudios

Por direitos autorais, o app **não traz letras nem áudios de músicas protegidas** — só os metadados
(título/artista). As 8 primeiras cantigas de **Folclore & Infantil** (`12001`–`12008`, domínio público)
já vêm com letra sincronizada para testar.

Para adicionar letra/áudio de qualquer música, use o **número** dela como nome do arquivo:

- `<numero>.lrc` — letra sincronizada
- `<numero>.mp3` (ou `.m4a`, `.ogg`, `.wav`) — playback

E coloque em **um** destes lugares:

1. **No projeto** (vai junto no APK): `app/src/main/assets/letras/` e `app/src/main/assets/audio/`
2. **Na TV, sem recompilar**:
   ```bash
   adb push 1001.lrc 1001.mp3 /sdcard/Android/data/br.com.karaokebrasil/files/karaoke/
   ```

Exemplo de `.lrc`:

```
[ti:Ciranda, Cirandinha]
[ar:Cantiga popular]
[00:05.00]Ciranda, cirandinha
[00:08.50]Vamos todos cirandar
```

Arquivos `.lrc` podem ser criados em editores como o *LRC Maker* ou baixados de fontes licenciadas.

## Adicionar músicas ao catálogo

O catálogo é gerado por um script Python:

1. Edite as listas em `ferramentas/gerar_catalogo.py` (acrescente **no fim** da lista do gênero,
   para não alterar os números existentes).
2. Rode `python3 ferramentas/gerar_catalogo.py`.
3. Recompile o app — as músicas novas entram no banco na próxima abertura, sem perder
   favoritas nem contagens.

Para criar um gênero novo, adicione-o também em `data/Genero.kt` (com um prefixo novo).

## Abrindo no IntelliJ IDEA

Requisitos:

- **IntelliJ IDEA** 2024.3 ou mais recente (Community ou Ultimate) com o plugin **Android** habilitado
  (*Settings → Plugins → Android*).
- **JDK 17+** (pode usar o JetBrains Runtime embutido).
- **Android SDK** com *Platform 35* e *Build-Tools*. Na primeira vez, o IntelliJ oferece baixar o SDK;
  ou configure em *File → Project Structure → SDKs → Android SDK*.

Passos:

1. *File → Open…* e selecione a pasta do projeto (onde está o `settings.gradle.kts`).
2. Aceite *Trust Project*; aguarde o **Gradle Sync** (baixa as dependências).
3. Se pedir, crie `local.properties` com `sdk.dir=/caminho/para/Android/Sdk`.
4. Crie um emulador **Android TV** (*Tools → Android → Device Manager* → *Create device* →
   categoria **TV** → ex.: *Television (1080p)*, imagem API 34) ou conecte uma TV/TV Box
   com **depuração ADB** ativada (`adb connect <ip-da-tv>`).
5. Selecione a configuração **app** e clique em **Run ▶**.

Pela linha de comando:

```bash
./gradlew assembleDebug        # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # instala no dispositivo conectado
./gradlew testDebugUnitTest    # testes do leitor de letras LRC
```

## Estrutura

```
app/src/main/java/br/com/karaokebrasil/
├── KaraokeApp.kt            # Application: cria banco, repositório e fila
├── MainActivity.kt
├── data/                    # Room: Musica, MusicaDao, KaraokeDatabase, Genero, fila
├── letra/                   # LrcParser, Letra, FonteDeMidia (localiza .lrc e áudio)
└── ui/
    ├── KaraokeNavHost.kt    # navegação + diálogo de opções da música
    ├── KaraokeViewModel.kt  # catálogo, busca, favoritas, fila
    ├── home/  genero/  busca/  fila/
    └── player/              # PlayerScreen + PlayerViewModel (ExoPlayer / relógio demo)
app/src/main/assets/
├── catalogo.json            # banco de músicas (gerado pelo script)
├── letras/                  # <numero>.lrc
└── audio/                   # <numero>.mp3
ferramentas/gerar_catalogo.py
```

## Tecnologias

Kotlin 2.0 · Jetpack Compose + `androidx.tv:tv-material` · Navigation Compose · Room (KSP) ·
Media3 ExoPlayer · Coroutines/Flow · AGP 8.7 · Gradle 8.14
