# Desenvolver o Nimbo

O pacote atual é `com.nimbo.launcher`. A versão **1.9.0** usa `versionCode` **24**. O código é Java nativo, com recursos Android e um script de compilação em Python; não há projeto Gradle.

## Preparar o ambiente

Instale Python 3, um JDK com `javac` e `keytool`, Android SDK Platform 34 e Build Tools 37.0.0. Para instalar na TV, use também Android SDK Platform Tools (`adb`). O ambiente validado usa JDK 19 no macOS.

```sh
export ANDROID_HOME="$HOME/Library/Android/sdk"
export ANDROID_BUILD_TOOLS="37.0.0"
python3 build.py --unsigned
python3 tests/run.py
```

`ANDROID_HOME` deve apontar para a sua instalação do SDK. A plataforma de compilação é fixada em `platforms/android-34/android.jar`; a versão de Build Tools pode ser selecionada pela variável acima. O resultado sem assinatura é `build/nimbo-unsigned.apk` e precisa ser assinado antes de ser instalado.

## Onde modificar

| Arquivo ou pasta | Responsabilidade |
| --- | --- |
| `src/com/nimbo/launcher/MainActivity.java` | Home, cards, foco, menu por pressão longa e apps ocultos |
| `src/com/nimbo/launcher/AppCatalog.java` | Descoberta de apps e preparação de banners/ícones em segundo plano |
| `src/com/nimbo/launcher/AppOrder.java` | Regras de ordem, movimentação e ocultação |
| `src/com/nimbo/launcher/SettingsSidebar.java` | Painéis laterais, linhas de ação e navegação pelo controle |
| `src/com/nimbo/launcher/NetworkStatusIndicator.java` | Estado da conexão e observadores do Android |
| `src/com/nimbo/launcher/WallpaperController.java` | Importação, armazenamento, animação e pausa do wallpaper |
| `src/com/nimbo/launcher/WallpaperPickerActivity.java` | Permissões, consulta da galeria e seleção de imagem/GIF |
| `src/com/nimbo/launcher/ThumbnailLoader.java` | Miniaturas sob demanda e cache |
| `src/com/nimbo/launcher/LatestTaskRunner.java` | Execução e substituição de tarefas em segundo plano |
| `src/com/nimbo/launcher/LauncherSetupPage.java` | Página compartilhada de escolha da Home e acessibilidade |
| `src/com/nimbo/launcher/LauncherSetupActivity.java` | Entrada externa para a configuração do launcher |
| `src/com/nimbo/launcher/HomeAccessibilityService.java` | Redirecionamento opcional de outras telas iniciais |
| `src/com/nimbo/launcher/LineIcon.java` | Ícones desenhados localmente |
| `res/values/strings.xml` | Textos e plurais da interface |
| `res/drawable/`, `res/anim/`, `res/values/styles.xml` | Marca, animações e estilos |
| `apps.txt` | Prioridade inicial de pacotes, complementada pela descoberta de apps |
| `AndroidManifest.xml` | Identificador, versão, atividades e permissões |
| `build.py` | Recursos, Java, DEX, alinhamento e assinatura do APK |
| `tests/` | Regressões JVM e verificações da assinatura |

## Assinatura

Uma atualização precisa manter o pacote e o certificado da instalação existente. O projeto espera a chave local `home-simples.jks`, alias `home-simples`, e verifica seu certificado contra a impressão pública em `signing-certificate.sha256` **antes de substituir a pasta `build`**. O nome histórico da chave não afeta o pacote do Nimbo.

O script não cria uma chave substituta automaticamente. A chave privada fica fora do Git; a impressão SHA-256 do certificado é pública e pode ser versionada.

A senha pode ser fornecida pela variável `NIMBO_KEYSTORE_PASSWORD` ou por um arquivo **`signing.local.json`** na raiz:

```json
{
  "keystore_password": "SUA_SENHA_LOCAL"
}
```

Esse arquivo é ignorado pelo Git. Restrinja o acesso aos arquivos locais:

```sh
chmod 600 home-simples.jks signing.local.json
python3 build.py
# APK assinado: build/nimbo.apk
```

Se a variável de ambiente estiver definida, ela tem prioridade sobre o arquivo. A senha é repassada às ferramentas de assinatura pelo ambiente dos processos, sem ser embutida nos argumentos do comando. Chave ausente, senha ausente ou certificado divergente interrompem a compilação assinada.

### Distribuições próprias

Sem a chave original, você pode compilar com `--unsigned`. Para assinar sua própria distribuição, crie uma chave sua e ajuste a impressão do certificado. Isso não permite atualizar uma instalação assinada com a chave original.

```sh
# Faça isto apenas para uma distribuição própria, em uma cópia do projeto.
keytool -genkeypair -keystore home-simples.jks -alias home-simples \
  -keyalg RSA -keysize 2048 -validity 10000

# O keytool pedirá a senha. Registre o certificado público dessa chave.
keytool -exportcert -keystore home-simples.jks -alias home-simples \
  -file signing-certificate.der
python3 -c 'import hashlib,pathlib; print(hashlib.sha256(pathlib.Path("signing-certificate.der").read_bytes()).hexdigest())' > signing-certificate.sha256
```

Configure a senha local e execute `python3 build.py`. Guarde a chave fora do repositório para manter as próximas atualizações dessa distribuição.

## Instalar e atualizar pela rede

Pareie o ADB com a depuração sem fio da TV quando necessário. Use o endereço de **conexão**, que pode ter uma porta diferente da de pareamento:

```sh
adb pair IP:PORTA_DE_PAREAMENTO
adb connect IP:PORTA_DE_CONEXAO
adb devices
adb -s IP:PORTA_DE_CONEXAO install -r --user 0 build/nimbo.apk
adb -s IP:PORTA_DE_CONEXAO shell am start -W -n com.nimbo.launcher/.MainActivity
```

Quando o dispositivo aparecer por mDNS, você também pode usar seu identificador de `adb devices` no argumento `-s`.

No macOS, `./atualizar-tv.command IP:PORTA` compila, instala e abre o Nimbo. O alvo também pode vir de `TV_ALVO`. Sem alvo explícito, o script aceita apenas uma conexão ADB ativa.

O `install -r` preserva os dados quando pacote e assinatura são os mesmos. Trocar de `com.homesimples.launcher` para `com.nimbo.launcher` cria uma instalação separada e exige uma migração própria; o APK não inclui migração automática entre esses pacotes. Não é necessário desativar outros launchers para testar uma alteração de interface.

## Dados e permissões

Preferências ficam no arquivo lógico `config` de `SharedPreferences`: `hidden_apps` guarda ocultações e `app_order` guarda a ordem. O wallpaper importado fica no armazenamento privado do app. Esses dados pessoais não fazem parte do código-fonte.

O indicador de rede usa `ConnectivityManager` e `WifiManager`, sem ler SSID ou pedir localização. Os observadores acompanham o ciclo visível da Home. A acessibilidade opcional filtra as outras atividades HOME e não solicita conteúdo de janelas nem filtragem de teclas. A desinstalação usa a confirmação nativa do Android.

## Verificar alterações

Execute `python3 tests/run.py`. As regressões JVM cobrem ordenação, ocultação, restauração, pacotes ausentes e substituição/cancelamento de tarefas. As verificações de assinatura protegem a saída existente contra chave ausente, senha ausente e certificado divergente; a última depende da chave local.

Confira no emulador ou na TV os fluxos afetados pela mudança:

- Foco do primeiro app até o **+**, incluindo zoom e sombra sem cortes.
- Toque curto e pressão longa de OK, retorno de telas nativas e navegação por Voltar.
- Ocultar, mover e restaurar apps, preservando as preferências após reabrir.
- Selecionar/cancelar wallpapers e pausar o GIF ao abrir outro aplicativo.
- Retornar das configurações com foco e wallpaper preservados.

Na validação 1.9.0, Lint registrou zero erros e oito avisos conhecidos: `OldTargetApi`, `DiscouragedApi`, `RtlHardcoded` e `DataExtractionRules`. Os relatórios em `docs/VALIDACAO-*.md` descrevem o estado de cada versão; não substituem testes do comportamento alterado.

## Histórico e licença

O [histórico de alterações](docs/HISTORICO.md) e as [notas técnicas históricas](docs/HISTORICO-TECNICO.md) registram a evolução do app, incluindo funções posteriormente removidas. Este guia descreve o fluxo atual de desenvolvimento.

O projeto deriva do [TV Detox](https://github.com/Ansub/tv-detox), de Ansub Khan, sob licença MIT. Preserve `LICENSE` e `res/raw/tv_detox_license.txt`, incluído no APK.
