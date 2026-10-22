<div align="center">
  <img src="docs/assets/nimbo-icon.svg" width="88" height="88" alt="Ícone do Nimbo">
  <h1>Nimbo Launcher</h1>
  <p>Uma tela inicial simples para a sua Android TV.<br>Seus apps, seu wallpaper e o controle na mão.</p>

  ![Versão 1.9.0](https://img.shields.io/badge/vers%C3%A3o-1.9.0-CBC2EE)
  ![Android 9 ou superior](https://img.shields.io/badge/Android-9%2B-3DDC84?logo=android&logoColor=white)
  ![Java nativo](https://img.shields.io/badge/Java-nativo-ED8B00)
  [![Licença MIT](https://img.shields.io/badge/licen%C3%A7a-MIT-blue)](LICENSE)
</div>

---

O **Nimbo** é um launcher para Android TV e Google TV com foco em uma Home limpa, personalizável e fácil de usar pelo controle remoto. A tela inicial reúne uma fileira de aplicativos, relógio, data e atalhos de rede e configurações. Imagens e GIFs locais podem virar o fundo da sua TV.

Feito em **Java com componentes nativos do Android**, sem bibliotecas externas em tempo de execução, anúncios ou compras no app. O projeto é uma adaptação do [TV Detox](https://github.com/Ansub/tv-detox), de Ansub Khan.

[Recursos](#recursos) · [Como usar](#como-usar) · [Instalação](#instalação) · [Compilação](#compilação) · [Desenvolvimento](DESENVOLVIMENTO.md)

## Recursos

- **Apps em uma fileira:** navegue com as setas, organize a ordem e oculte atalhos.
- **Wallpapers com movimento:** escolha imagens ou GIFs locais de até 32 MiB.
- **Menu por pressão longa:** abra, mova, oculte, veja informações ou solicite a desinstalação de um app.
- **Card “+”:** recupere aplicativos ocultos sem procurar nas configurações.
- **Painéis laterais:** configurações, wallpapers e ações seguem o mesmo visual e navegação.
- **Estado da rede:** indicador de Wi-Fi ou Ethernet com atalho para as configurações de conexão.
- **Botão Início:** escolha o Nimbo como launcher padrão ou use a integração opcional de acessibilidade quando o firmware limitar essa escolha.

## Como usar

| Controle | Ação |
| --- | --- |
| **Esquerda / Direita** | Navegar entre os aplicativos |
| **OK** | Abrir o aplicativo ou confirmar uma opção |
| **Segurar OK sobre um app** | Abrir o menu de ações do aplicativo |
| **Cima na fileira** | Selecionar a engrenagem; Esquerda leva ao ícone de rede |
| **Menu** | Abrir o painel de configurações |
| **Voltar** | Retornar uma página ou fechar o painel |
| **Card +** | Mostrar os aplicativos ocultos disponíveis para restaurar |

Na engrenagem, **Wallpaper** abre diretamente o seletor de imagens e GIFs. O arquivo escolhido é copiado para o armazenamento privado do Nimbo. Se houver um fundo personalizado, o seletor também oferece **Voltar ao fundo original**.

**Ocultar** remove apenas o atalho da Home. **Desinstalar** abre a confirmação nativa do Android; as opções disponíveis dependem de o app ser do usuário ou do sistema.

## Instalação

Requer **Android 9 / API 28 ou superior** e uma TV ou box com controle direcional. A disponibilidade das telas de configuração varia conforme o fabricante.

Este repositório contém o código-fonte. Para obter um APK instalável, siga a seção de [compilação e assinatura](#compilação). Com o APK assinado e o ADB já pareado/conectado à TV:

```sh
adb devices
adb -s IP:PORTA install -r --user 0 build/nimbo.apk
adb -s IP:PORTA shell am start -W -n com.nimbo.launcher/.MainActivity
```

Substitua `IP:PORTA` pelo endereço atual da depuração sem fio ou pelo identificador exibido por `adb devices`.

Depois, abra **Configurações → Definir como padrão** no Nimbo. Primeiro tente a escolha nativa do Android. Se a TV não permitir a troca, a mesma página oferece acesso ao serviço **Nimbo — botão Início** em Acessibilidade. Mantenha apenas um serviço de substituição de launcher ativo.

> O pacote atual é `com.nimbo.launcher`. Versões antigas com `com.homesimples.launcher` são aplicativos separados para o Android: instalar este APK não transfere automaticamente os dados do pacote antigo.

## Compilação

O projeto usa um script Python e as ferramentas do Android SDK, sem Gradle. Você pode compilar pelo terminal.

### Requisitos

- Python 3.
- JDK com `java`, `javac` e `keytool` no `PATH`; o projeto foi validado com JDK 19.
- Android SDK Platform **34**.
- Android SDK Build Tools **37.0.0**.
- Android SDK Platform Tools para instalar pela conexão ADB.

```sh
git clone https://github.com/Luscarvalho/Nimbo-Launcher.git
cd Nimbo-Launcher

# Ajuste para o local do Android SDK no seu computador.
export ANDROID_HOME="$HOME/Library/Android/sdk"
export ANDROID_BUILD_TOOLS="37.0.0"

python3 build.py --unsigned
```

No macOS, o script já procura o SDK em `~/Library/Android/sdk` por padrão. Em outros sistemas ou instalações, defina `ANDROID_HOME`.

O comando acima gera **`build/nimbo-unsigned.apk`**, útil para verificar a compilação sem a chave privada. Esse arquivo precisa ser assinado antes de ser instalado.

### APK assinado

A compilação de atualização requer a chave local `home-simples.jks`, com alias `home-simples`, e o certificado correspondente a `signing-certificate.sha256`. O nome histórico da chave foi mantido; o app continua sendo Nimbo.

A senha é lida de **`NIMBO_KEYSTORE_PASSWORD`** ou do arquivo local **`signing.local.json`**, ambos descritos no [guia de desenvolvimento](DESENVOLVIMENTO.md#assinatura). A chave e a configuração local ficam fora do Git.

```sh
python3 build.py
# Saída: build/nimbo.apk
```

Para manter as atualizações de uma instalação, preserve o mesmo pacote e a mesma chave. Quem fizer uma distribuição própria deve usar sua própria identidade de assinatura; veja o guia de desenvolvimento.

No macOS, com a assinatura local configurada, o atalho abaixo compila, instala e abre o Nimbo:

```sh
./atualizar-tv.command IP:PORTA
```

Sem argumento, ele usa a conexão existente apenas se houver exatamente um dispositivo ADB conectado.

## Verificação e implementação

```sh
python3 tests/run.py
```

Os testes executam regressões de ordem e ocultação de apps, cancelamento de tarefas e proteção da chave de assinatura. Não exigem TV ou emulador; alguns testes de assinatura dependem da chave local.

O catálogo de aplicativos e as imagens são preparados fora da thread da interface. Há caches limitados para ícones e miniaturas, cancelamento de tarefas substituídas e pausa da animação do wallpaper quando a Home deixa de estar visível. GIFs grandes ainda podem consumir mais memória e processamento que imagens estáticas.

A versão **1.9.0** foi compilada e conferida em uma TV física. A análise Lint registrada tem **zero erros e oito avisos conhecidos**. Consulte os [registros de validação](docs/VALIDACAO-1.9.0.md) e o [guia de desenvolvimento](DESENVOLVIMENTO.md) para os limites dessas verificações.

## Privacidade e permissões

- O aplicativo não declara a permissão `INTERNET` e não inclui analytics ou serviços de anúncios.
- O acesso a imagens é solicitado para escolher wallpapers locais, conforme a versão do Android.
- As permissões de estado de rede alimentam o indicador de conexão; o Nimbo não lê o nome da rede nem solicita localização.
- A acessibilidade é **opcional** e usada para redirecionar outras telas iniciais para o Nimbo. O serviço não solicita leitura do conteúdo das janelas nem filtragem de teclas.
- A remoção de aplicativos passa pelo desinstalador nativo, com confirmação do Android.

## Contribuir

Sugestões e problemas podem ser enviados pelas [Issues](https://github.com/Luscarvalho/Nimbo-Launcher/issues). Em relatos de interface, inclua a versão do Android, o modelo do dispositivo e os passos para reproduzir. Para alterações no código, consulte [DESENVOLVIMENTO.md](DESENVOLVIMENTO.md) e confira a navegação com o controle remoto.

## Créditos e licença

Nimbo é derivado do [TV Detox](https://github.com/Ansub/tv-detox), de **Ansub Khan**, e distribuído sob a [licença MIT](LICENSE). O aviso original de copyright permanece no projeto e no recurso de licença incluído no APK.
