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

## Como cantar

Ao escolher uma música, o app oferece:

- **▶ Cantar com vídeo do YouTube**: funciona para **todas** as músicas. Abre o app do YouTube
  já buscando "<título> <artista> karaokê". Os vídeos são dos canais de karaokê do YouTube,
  que cuidam dos direitos.
- **🎤 Cantar agora**: o player do próprio app, com letra sincronizada. Aparece quando a música
  tem letra (`.lrc`) disponível (selo **♪ LETRA** no cartão).

## Letras e áudios no servidor

O app procura a letra/áudio de cada música, pelo **número**, nesta ordem:

1. Na TV: `/sdcard/Android/data/br.com.karaokebrasil/files/karaoke/<numero>.lrc|.mp3`
   (copie com `adb push`)
2. Dentro do APK: `app/src/main/assets/letras/` e `app/src/main/assets/audio/`
3. **No servidor**: a pasta [`servidor/`](servidor/) deste repositório no GitHub, lida pela
   internet. O endereço fica em `SERVIDOR_MIDIA_URL`, no `app/build.gradle.kts`. Pode ser trocado
   por qualquer site seu (precisa terminar com `/`).

Para publicar músicas no servidor, **sem recompilar o app**:

1. Coloque `servidor/letras/<numero>.lrc` e/ou `servidor/audio/<numero>.mp3`.
   Para áudios hospedados em outro site, liste as URLs em `servidor/links_externos.json`:
   `{"1001": {"audio": "https://meusite.com/evidencias.mp3"}}`
2. Rode `python3 ferramentas/gerar_indice_servidor.py` (atualiza `servidor/midias.json`).
3. Faça commit e push. Na próxima abertura, o app já mostra e toca as músicas novas.
   O áudio toca por streaming; a letra fica em cache para uso sem internet.

> ⚠️ **Direitos autorais**: letras e playbacks de músicas comerciais são protegidos. Publique no
> servidor só o que você tem direito de distribuir: cantigas de domínio público, gravações
> próprias ou conteúdo licenciado (ex.: comprado de distribuidoras de karaokê). As 8 cantigas de
> **Folclore & Infantil** (`12001`–`12008`) já estão no servidor como exemplo.

Exemplo de `.lrc` (dá para criar no *LRC Maker* ou em outros editores de letra sincronizada):

```
[ti:Ciranda, Cirandinha]
[ar:Cantiga popular]
[00:05.00]Ciranda, cirandinha
[00:08.50]Vamos todos cirandar
```

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

> **Erro `jlink executable ... jbr\bin\jlink.exe does not exist`?** O Gradle está usando o Java
> embutido do IntelliJ, que não tem `jlink`. O projeto já pede um JDK 17 via *toolchain* (baixado
> automaticamente), mas se o erro persistir vá em *Settings → Build, Execution, Deployment →
> Build Tools → Gradle → Gradle JVM* e escolha um JDK completo (ex.: *Download JDK… → Eclipse
> Temurin 17*). Depois, *File → Sync Project with Gradle Files*.

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
├── letra/                   # LrcParser, Letra, FonteDeMidia (local, assets ou servidor)
└── ui/
    ├── KaraokeNavHost.kt    # navegação + diálogo de opções da música
    ├── KaraokeViewModel.kt  # catálogo, busca, favoritas, fila
    ├── home/  genero/  busca/  fila/
    └── player/              # PlayerScreen + PlayerViewModel (ExoPlayer / relógio demo)
app/src/main/assets/
├── catalogo.json            # banco de músicas (gerado pelo script)
├── letras/                  # <numero>.lrc
└── audio/                   # <numero>.mp3
servidor/                    # letras/áudios servidos pela internet + midias.json
ferramentas/                 # scripts Python: catálogo e índice do servidor
```

## Tecnologias

Kotlin 2.0 · Jetpack Compose + `androidx.tv:tv-material` · Navigation Compose · Room (KSP) ·
Media3 ExoPlayer · Coroutines/Flow · AGP 8.7 · Gradle 8.14
