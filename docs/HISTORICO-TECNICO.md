> Registro histórico anterior à publicação no GitHub. Para instruções atuais, consulte o README e o guia DESENVOLVIMENTO.md na raiz.

# Modificar o Nimbo

Este é o projeto local do launcher, mantido em **Documents/Estudo/Home Simples**. O pacote é `com.nimbo.launcher`. A versão atual é **1.9.0**, código de versão **24**.

## Onde modificar

| Arquivo | Função |
|---|---|
| `src/com/nimbo/launcher/AppCatalog.java` | Descoberta e preparação de imagens em segundo plano; cache de 2 MiB e invalidação por pacote |
| `src/com/nimbo/launcher/AppOrder.java` | Regras puras de ordem, movimentação e ocultação |
| `src/com/nimbo/launcher/LatestTaskRunner.java` | Uma tarefa ativa e no máximo uma substituta; cancelamento e término de threads ociosas |
| `src/com/nimbo/launcher/ThumbnailLoader.java` | Cache de 1 MiB e carregamento sob demanda das miniaturas visíveis |
| `tests/` | Regressões executadas por `python3 tests/run.py`, sem TV nem dependências extras |
| `signing-certificate.sha256` | Identidade pública do certificado de atualização |
| `src/com/nimbo/launcher/MainActivity.java` | Tela inicial, cartões, ordem inicial, menu por pressão longa e apps ocultos |
| `src/com/nimbo/launcher/NetworkStatusIndicator.java` | Indicador passivo de rede; registra observadores enquanto a tela inicial está visível |
| `src/com/nimbo/launcher/SettingsSidebar.java` | Painel lateral, páginas, foco do controle e retorno |
| `res/anim/` e `res/values/styles.xml` | Animação de entrada e saída do painel |
| `src/com/nimbo/launcher/WallpaperController.java` | Carregamento, armazenamento, animação e pausa do wallpaper |
| `src/com/nimbo/launcher/WallpaperPickerActivity.java` | Seletor local de imagens e GIFs |
| `src/com/nimbo/launcher/LauncherSetupActivity.java` | Entrada externa para a página compartilhada de configuração |
| `src/com/nimbo/launcher/LauncherSetupPage.java` | Ações nativas de Home/acessibilidade e página no SettingsSidebar |
| `src/com/nimbo/launcher/HomeAccessibilityService.java` | Redirecionamento de outras telas iniciais |
| `src/com/nimbo/launcher/LineIcon.java` | Ícones vetoriais desenhados pelo próprio app |
| `res/` | Ícone do app, textos, configuração do serviço e licença |
| `apps.txt` | Preferências iniciais da linha de aplicativos |
| `AndroidManifest.xml` | Nome, versão, permissões e atividades |
| `build.py` | Geração e assinatura do APK |
| `releases/` | APKs das versões guardadas |

## Gerar e testar uma atualização

No Terminal, dentro desta pasta:

```sh
python3 build.py
adb connect IP:PORTA
adb -s IP:PORTA install -r --user 0 build/nimbo.apk
adb -s IP:PORTA shell am start -W -n com.nimbo.launcher/.MainActivity
```

Você também pode executar **atualizar-tv.command**. Ele compila, instala a atualização preservando os dados e abre o Nimbo. Se a TV mudar de endereço, atualize `TV_ALVO` no script ou defina essa variável antes de executá-lo.

Os SDKs usados já estão instalados neste computador. O compilador permite selecionar o SDK por `ANDROID_HOME` e a versão de ferramentas por `ANDROID_BUILD_TOOLS`.

## Arquivo necessário para manter as atualizações

`home-simples.jks` é a chave de assinatura desta instalação. Ela foi copiada junto do projeto. Preserve-a para que os próximos APKs possam atualizar o aplicativo instalado, mantendo preferências e wallpaper. A chave não entra no ZIP de código-fonte nem no controle de versão; seu acesso local foi limitado ao usuário.

## Preferências

Os apps ocultos são um conjunto de nomes de pacotes salvo em `SharedPreferences`, arquivo lógico `config`, chave `hidden_apps`. A seleção de wallpaper fica em uma cópia privada do app. A instalação com `adb install -r` preserva esses dados.

Ocultar um app remove o atalho deste launcher. Não desinstala nem desativa o app no Android. O card **+** ao final da fileira permite restaurar individualmente os aplicativos ocultos disponíveis.

## Verificação de alterações futuras

Teste o comportamento alterado na TV com o controle remoto. Para a função de ocultar: pressão longa abre o menu sem iniciar o app ao soltar OK; toque curto abre normalmente; ocultar retira o atalho; reabrir/atualizar preserva a seleção; Apps ocultos restaura os atalhos. Preserve a escolha de launcher padrão e o serviço de acessibilidade já configurados, salvo quando a alteração envolver essas funções.

O projeto deriva do TV Detox, de Ansub Khan, sob licença MIT. O texto original deve continuar incluído no projeto e no APK.

A navegação da tela inicial conecta explicitamente cada app à engrenagem por Cima. O botão Menu abre o painel ao soltar a tecla, evitando que o mesmo evento feche o diálogo recém-aberto. O painel é uma janela modal: o foco fica nas suas opções e Voltar/Esquerda retorna à página anterior.

Na versão 1.3.1, LineIcon invalida o Drawable ao mudar o filtro de cor ou a opacidade. Isso força o ImageView a redesenhar o ícone na troca de foco; alterar apenas o Paint deixa a cor anterior na imagem já desenhada.

Na versão 1.3.2, a lista do painel tem recuo horizontal adicional igual de 20 unidades de desenho e limita o destaque à área interna. A largura da lista é explícita. Na TV 1280 × 720, as margens do card selecionado ficaram em 42 px de cada lado do painel.

Na versão 1.4, foi removido o recuo extra da lista: a borda do destaque alinha com o título. Na TV, ambos começam em x=869; as margens externas são 29 px de cada lado.

O indicador de rede usa ConnectivityManager e WifiManager, com ACCESS_NETWORK_STATE e ACCESS_WIFI_STATE (permissões normais). Não solicita localização, não lê SSID e não faz requisições de rede. Conexão validada: ícone claro; conexão sem validação de internet: âmbar com aviso; sem rede: riscado em cinza; Ethernet: ícone de cabo. Os observadores são registrados em onStart e removidos em onStop. Referência: https://developer.android.com/develop/connectivity/network-ops/reading-network-state

Na versão 1.4.1, o Wi-Fi é um botão selecionável. MainActivity configura foco, fundo e links explícitos entre Wi-Fi, engrenagem e o último app usado. NetworkStatusIndicator usa cor escura com foco para manter contraste sobre o fundo claro. OK tenta ACTION_WIFI_SETTINGS, depois ACTION_WIRELESS_SETTINGS e ACTION_SETTINGS se o firmware não oferecer a tela direta. Na TV, ACTION_WIFI_SETTINGS abre com.android.tv.settings/.connectivity.NetworkActivity. Voltar preserva o foco no Wi-Fi.

## Fileira e recuperação — 1.5

MainActivity usa HorizontalScrollView com uma FrameLayout interna para a fileira, preservando espaço para a ampliação dos cards com foco. CARD_WIDTH=258 e GAP=24 no desenho 1920 × 1080. O último card tem a chave interna add_apps e LineIcon.PLUS; ele não pode ser ocultado. A lista de apps não é mais truncada em seis itens.

showAddApps filtra os pacotes ocultos que ainda podem ser abertos, mostra seus ícones e remove somente o pacote escolhido de hidden_apps. O foco é aplicado depois do fechamento da janela para evitar que o Android restaure o foco antigo no +. SettingsSidebar aceita ícones Drawable de aplicativos e tem ação de retorno adequada para listas vazias fora das configurações.

## Zoom e sombra — 1.5.1

O espaço do foco fica dentro de appRow, porque HorizontalScrollView pode cortar desenhos nos limites do seu filho. FOCUS_INSET=64 reserva espaço para o zoom e a sombra de elevação nas extremidades. A altura inclui os recuos e a legenda. A posição visível dos cards foi preservada compensando o deslocamento da janela de rolagem. Ao receber foco, o card solicita que seu retângulo ampliado permaneça visível; a solicitação é feita após o evento de foco e descartada se o foco já mudou.

## Último card — 1.5.2

HorizontalScrollView mede o filho com largura não restringida: definir LayoutParams.width não reservava o espaço final na largura real da FrameLayout. appRow agora tem padding real à direita/inferior, clipToPadding=false e tamanhos mínimos correspondentes à área calculada. Antes, a área medida terminava na borda não ampliada do +; depois, foi confirmado espaço de 43 px após o card na TV. Verificado também com a fileira rolada horizontalmente devido aos novos apps instalados pelo usuário.

## Cabeçalho — 1.5.3

Restaurado relógio com tamanho 176 e data 30 no desenho 1920 × 1080, em x=MARGIN, y=92. A data usa LayoutParams WRAP_CONTENT nos dois eixos para não ser limitada à largura dos dígitos. Engrenagem à direita com margem MARGIN; Wi-Fi com margem MARGIN+88. Foco e atalhos preservados.

## Menu do aplicativo — 1.6

MainActivity.appMenu usa SettingsSidebar.showAppPage com ícone e nome do app. Item.destructive define a paleta de remoção; títulos, descrições e ícones mudam com o foco mantendo contraste.

Mover abre uma lista de posições, remove o pacote da ordem atual e o insere no índice escolhido. A preferência config/app_order guarda a ordem como nomes de pacotes separados por vírgulas. A seleção é aplicada antes de complementar com novos apps, sem substituir a lista de descoberta. Entradas ocultas previamente ordenadas são preservadas.

Informações usa ACTION_APPLICATION_DETAILS_SETTINGS com URI package:. Desinstalar usa ACTION_UNINSTALL_PACKAGE com EXTRA_RETURN_RESULT e REQUEST_DELETE_PACKAGES, abrindo o desinstalador nativo. O app não tem DELETE_PACKAGES e não realiza remoção silenciosa. O retorno marca os cards para atualização; os broadcasts existentes também atualizam a fileira. Apps de sistema sem atualizações não recebem ação de remoção; para os atualizados, o Android administra a volta à versão de fábrica.

Os eventos Esquerda/Direita dos cards são consumidos pelo próprio card e direcionados para o vizinho, evitando que HorizontalScrollView retorne o foco ao começo ao atingir uma extremidade. Centro/Enter seguem o comportamento nativo para cliques e pressão longa.

## Cards maiores — 1.6.1

CARD_WIDTH passou de 258 para 288 unidades de desenho (+11,6%). Na TV 1280 × 720, o tamanho normal é 192 × 108 px. GAP=24 e os recuos para foco permanecem iguais. Conferidos o primeiro card e o + ampliados sem corte.


## Revisão e correções — 1.7.0

O comando externo `--es apps` foi removido. MainActivity continua exportada para ser Home, mas não transforma extras de Intents em preferências. A preferência histórica config/apps é apenas lida como prioridade, e a descoberta continua complementando com apps de TV. Ordem e ocultações mantêm as mesmas chaves.

AppCatalog mantém o catálogo preparado para a tela e usa cache limitado por bytes. PackageManager e rasterização ficam no worker; callbacks e criação de Views ficam na thread principal. Cada mudança de pacote invalida sua revisão no cache. A fila LatestTaskRunner cancela o pedido anterior e guarda no máximo um substituto; sua thread pode encerrar após 15 segundos ociosa. Atualizações visíveis são agrupadas em 250 ms.

ThumbnailLoader mantém uma única decodificação ativa; reutilizações do ListView retiram pedidos antigos antes da próxima decodificação. O cache é limitado a 1 MiB e não reutiliza resultados de uma geração antiga após atualização/permissão. Uma decodificação nativa já iniciada pode terminar, mas seu resultado obsoleto não é aplicado. A consulta MediaStore tem CancellationSignal e um worker independente. O botão Carregar mais amplia a consulta em lotes de 200 itens.

HomeAccessibilityService acompanha broadcasts de pacotes e limpa/recalcula as atividades HOME. O manifesto deixa o Android recriar a Home ao mudar tamanho, idioma ou modo de interface; o foco por nome de pacote é salvo em Bundle. WallpaperController usa temporários únicos, cancela tarefas anteriores, verifica interrupção durante a cópia e limpa temporários órfãos com mais de uma hora.

build.py verifica a presença e a impressão SHA-256 do certificado antes de substituir a pasta build. Ausência ou divergência da chave interrompe a geração de uma atualização. `python3 build.py --unsigned` compila sem a chave para análise, produzindo build/nimbo-unsigned.apk. Não instale esse arquivo sobre a versão assinada.

`python3 tests/run.py` verifica ordem/ocultação, restauração, pacotes ausentes, cancelamento, substituição de pedidos e proteção contra chave ausente/divergente. As verificações de interface física e desempenho desta versão ainda devem ser feitas na TV. Consulte docs/VALIDACAO-1.7.0.md.


## Textos e banner — 1.7.2

Todos os textos fixos de interface e avisos ficam em res/values/strings.xml. As classes usam R.string; mensagens com nomes, números e estados usam parâmetros posicionais. A mensagem Carregar mais usa R.plurals.images_more_status. Logs, chaves internas, nomes de arquivos, formatos de relógio e nomes de pacotes continuam no código. App_name, GIF, HDMI e o fallback de licença são recursos não traduzíveis.

O banner mantém viewport 320 × 180 e os mesmos paths. Suas dimensões intrínsecas são 160 × 90 dp. Não confundir esses valores com CARD_WIDTH ou com os banners de outros aplicativos.

A versão não altera allowBackup nem adiciona dataExtractionRules. Lint não recebeu suppressões para esses avisos: permanecem oito avisos fora do escopo solicitado. Consulte docs/VALIDACAO-1.7.2.md.

## Marca e configuração — 1.8.0

LauncherSetupActivity usa Views nativas, GradientDrawable e ScrollView. Cards mantêm o foco por DPAD sem ampliação que possa cortar suas bordas; cores de texto e ícone acompanham o foco. Os estados de launcher padrão e acessibilidade são atualizados em onResume. RoleManager e as configurações nativas continuam responsáveis pelas permissões. Cancelar o seletor retorna à tela sem um diálogo adicional.

A marca fica em res/drawable/nimbo_mark.xml; ícone e banner continuam nos recursos launcher_icon e launcher_banner. O banner mantém 160 × 90 dp e viewport 320 × 180. São vetores sem fontes externas ou bitmaps. Os textos visíveis usam Nimbo; identificadores com.homesimples.launcher, home-simples.jks e build/nimbo.apk são mantidos para compatibilidade. O APK distribuído é nimbo-1.8.0.apk. A pasta Documents/Estudo/Home Simples permanece no mesmo endereço.

## Configuração no painel lateral — 1.8.1

LauncherSetupPage concentra a escolha nativa de Home, o atalho de acessibilidade e o teste Início. MainActivity abre essa página na instância de SettingsSidebar já existente, sem trocar de atividade. onResume atualiza estados quando a página está visível. O painel retorna ao índice da última ação para preservar foco após telas nativas.

LauncherSetupActivity permanece como entrada externa e usa a mesma página compartilhada; o fechamento libera o diálogo em onDestroy. O desenho exclusivo de duas colunas da versão 1.8.0 foi removido. A marca independente nimbo_mark.xml, sem uso após esse ajuste, também foi retirada; ícone e banner do Nimbo foram preservados. Pacote, chave, backup e preferências continuam iguais.

## Seletor de wallpaper — 1.8.2

WallpaperPickerActivity usa janela translúcida de largura 660 no desenho de 1920 × 1080, na borda direita, sobre a Home. WallpaperPickerTheme mantém o fundo ao redor visível e a animação lateral. A página usa os mesmos recuos 44/76 e cores do SettingsSidebar.

SettingsSidebar.actionRow constrói as linhas para menus e ações da galeria. O rótulo pertence à linha e pode ser atualizado com setActionTitle durante mudanças de permissão. Seleções da ListView usam fundo claro arredondado e ColorStateList propagado às legendas; miniaturas continuam sob ThumbnailLoader, com cache e cancelamento existentes. O controle conecta ações, lista e paginação. Esquerda/Voltar fecha a atividade e mantém o contrato RESULT_OK/URI para MainActivity.

Sobre o Nimbo e o método do diálogo de licença foram removidos. A licença MIT original permanece em LICENSE, res/raw/tv_detox_license.txt e no APK; o manifesto referencia o recurso em com.homesimples.launcher.LICENSE. Backup, pacote, chave e armazenamento de preferências permanecem iguais.

## Wallpaper direto — 1.8.3

MainActivity.chooseWallpaper abre WallpaperPickerActivity diretamente do menu principal. A página wallpaperMenu foi removida. EXTRA_HAS_CUSTOM, passado à atividade não exportada, controla a opção Voltar ao fundo original no seletor. RESULT_RESET retorna uma escolha de restauração; MainActivity só a processa no request PICK_WALLPAPER, chamando WallpaperController.clear. RESULT_OK continua importando a URI selecionada. Cancelar não altera o wallpaper. Não foram alterados armazenamento, permissões ou o carregamento da galeria.

## Menu de energia — 1.8.4

MainActivity inclui Menu de energia no SettingsSidebar. HomeAccessibilityService.openPowerMenu chama apenas performGlobalAction(GLOBAL_ACTION_POWER_DIALOG). A referência da instância conectada é mantida na thread principal e limpa em onUnbind/onDestroy; não há Receiver, Binder ou Intent exportado para disparar a ação.

Sem conexão ao serviço, powerSetupPage orienta sua ativação pelas configurações nativas. onResume atualiza essa página; o retorno não abre o menu automaticamente. Se a ação do Android falhar, uma mensagem orienta usar as configurações da TV. ComponentName do serviço, filtro de eventos, canRetrieveWindowContent=false e permissões foram preservados. Nome e descrição agora explicam as funções Início e energia.

Nenhum comando de reboot, root, leitura de telas ou automação de confirmação é utilizado. A presença de Reiniciar/Desligar no menu fica a cargo do sistema. O teste abre e fecha o diálogo, sem selecionar ações de energia.

## Remoção da função de energia — 1.8.5

MainActivity, HomeAccessibilityService, LineIcon e strings.xml retornaram exatamente ao conteúdo da versão 1.8.3, anterior à função de energia. Removidos o item, a página de orientação, a chamada GLOBAL_ACTION_POWER_DIALOG e a referência estática da instância conectada. O serviço volta a ser identificado como Nimbo — botão Início. Pacote, ComponentName do serviço, permissões, backup e dados preservados; versão incrementada para permitir atualização sobre a 1.8.4.

## Identificador Nimbo — 1.9.0

Pacote e namespace Java com.nimbo.launcher. Fontes em src/com/nimbo/launcher; regressões em tests/com/nimbo/launcher. build.py gera Apps/R no mesmo namespace, com saída build/nimbo.apk (ou nimbo-unsigned.apk). Manifesto e settingsActivity da acessibilidade seguem o novo pacote. A chave e o alias originais permanecem com seus nomes históricos; nome de pacote e identidade de assinatura são independentes.

A mudança exige instalar outro pacote. A migração operacional desta TV usou cópias temporárias de desenvolvimento assinadas para transferir files/ e shared_prefs/, com validação SHA-256 por arquivo. Em seguida foram instalados os APKs de produção em ambos os pacotes. Essa ferramenta temporária não faz parte do aplicativo nem do ZIP distribuído. O pacote antigo fica desativado após a configuração do novo Home. Os arquivos migrados não entram no código-fonte.

atualizar-tv.command aceita o alvo como primeiro argumento ou em TV_ALVO. Sem um alvo explícito, só usa a conexão ADB se houver exatamente um dispositivo conectado. O script instala o novo pacote e abre com.nimbo.launcher/.MainActivity; não contém um endereço de TV fixo.
