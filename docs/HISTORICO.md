> Registro histórico anterior à publicação no GitHub. Para instruções atuais, consulte o README e o guia DESENVOLVIMENTO.md na raiz.

# Nimbo — versão 1.9.0

Launcher de Android TV com fileira de aplicativos, wallpapers locais e configuração pelo controle. Adaptação do [TV Detox](https://github.com/Ansub/tv-detox), de Ansub Khan, sob licença MIT preservada no código e no APK. Sem anúncios ou compras no app.

## Novo pacote — versão 1.9.0

O identificador do aplicativo passou de com.homesimples.launcher para com.nimbo.launcher. O nome visível continua Nimbo. Código Java, manifesto, acessibilidade, testes e ferramentas de compilação usam o novo namespace; o APK de compilação agora se chama build/nimbo.apk.

A mudança de pacote cria uma instalação separada no Android. A migração local desta TV transfere as preferências e o wallpaper, configura o novo Nimbo como Home e substitui o serviço antigo pelo novo quando ele estava ativo. A instalação antiga fica desativada, com seus dados preservados. O APK distribuído é de produção e não permite depuração. Consulte docs/VALIDACAO-1.9.0.md.

A chave original continua guardada como home-simples.jks, com o mesmo certificado, para preservar a identidade de assinatura. Não crie outra chave para atualizar as próximas versões do Nimbo.

## Remoção do menu de energia — versão 1.8.5

Removida a opção Menu de energia, sua orientação de ativação, ícone e textos. O serviço volta a se chamar Nimbo — botão Início e cuida somente da abertura da Home, como na versão 1.8.3. A abertura direta do seletor de wallpaper e as outras funções permanecem disponíveis. Consulte docs/VALIDACAO-1.8.5.md.

## Histórico: menu de energia — versão 1.8.4 (removido na 1.8.5)

Configurações → Menu de energia abre o menu nativo do Android usando a acessibilidade do Nimbo. No emulador Android TV 14, o menu mostra Desligar e Reiniciar. As opções disponíveis dependem do firmware da TV. O Nimbo abre o menu; nenhuma ação de energia é executada automaticamente.

Se o serviço estiver desligado, um painel orienta a ativar Nimbo — Início e energia na acessibilidade. Ao retornar, a página atualiza seu estado. Não há acesso a conteúdo de telas, filtragem de teclas, root ou permissão de reinício do sistema. Consulte docs/VALIDACAO-1.8.4.md.

## Wallpaper direto — versão 1.8.3

A opção Wallpaper nas configurações abre diretamente a galeria de imagens e GIFs. Removida a página intermediária Escolher imagem ou GIF. Quando há um fundo personalizado, Voltar ao fundo original aparece no próprio seletor. O resultado dessa ação volta à Home, que executa a restauração pelo WallpaperController existente.

## Wallpaper integrado — versão 1.8.2

O seletor de imagens agora abre no mesmo formato de painel lateral da Home, com fundo, margens, fontes e destaque de foco dos outros menus. Permitir acesso, Atualizar, Outros locais e Carregar mais usam o mesmo componente de ação do SettingsSidebar. A lista mantém miniaturas, nomes e dimensões, com contraste adequado na seleção. Voltar/Esquerda fecha o seletor.

Removida a opção Sobre o Nimbo e seu diálogo. A licença original permanece no projeto e no APK, referenciada pelos metadados do aplicativo. As regras de permissão, consulta, paginação e carregamento em segundo plano foram preservadas. Consulte docs/VALIDACAO-1.8.2.md.

## Configuração integrada — versão 1.8.1

Definir como padrão agora abre uma página dentro do mesmo painel lateral das configurações. Reutiliza SettingsSidebar, com suas cores, tipografia, margens, ícones e destaque de foco. A Home e o wallpaper continuam visíveis ao fundo. Esquerda/Voltar retorna às configurações; o nome Nimbo é mantido.

As ações nativas de escolha de launcher, acessibilidade e teste do botão Início permanecem disponíveis, com estados em suas descrições e foco preservado ao retornar. A entrada externa LauncherSetupActivity usa a mesma página compartilhada. Consulte docs/VALIDACAO-1.8.1.md.

## Nimbo — versão 1.8.0

Home Simples agora se chama Nimbo. A tela de configuração do botão Início foi redesenhada com cards arredondados, ícones, estados individuais e navegação pelo controle. A escolha de launcher e a acessibilidade continuam usando as telas nativas do Android. Cancelar a escolha retorna aos cards sem abrir outro aviso.

Ícone, banner e marca usam vetores locais. O pacote, a chave de assinatura, as preferências e a pasta do projeto foram preservados para permitir atualização sem perder configurações. A tela inicial mantém seu relógio e seus cards.

Compilação, assinatura e navegação conferidas no emulador Android TV 14. Lint: zero erros e oito avisos já existentes. Consulte docs/VALIDACAO-1.8.0.md. A TV física não foi conectada nesta etapa.

## Recursos e textos — versão 1.7.2

Textos de interface, mensagens, acessibilidade e formatos organizados em res/values/strings.xml, mantendo português e o conteúdo exibido. Quantidades usam o recurso nativo de pluralização. Banner vetorial dimensionado em 160 × 90 dp, equivalente a 320 × 180 px em xhdpi, com o desenho preservado. Regras de backup mantidas.

Android Lint: zero erros e oito avisos restantes (alvo Android, orientação, RTL e backup). Textos de Home, rede, painel e configuração conferidos no emulador. Nenhuma conexão com a TV física.

## Ajuste visual — versão 1.7.1

Removido o texto de ajuda do rodapé dos painéis laterais (Voltar para fechar / Voltar para configurações). A navegação por Voltar e Esquerda permanece disponível.

## Correções de saúde — versão 1.7.0

- A Home não aceita mais configuração da lista por extras de atividades externas. As preferências privadas existentes continuam sendo lidas.
- Catálogo, nomes, banners e ícones são preparados fora da thread da interface. Cache de imagens de apps limitado a 2 MiB; ocultar e mover reutilizam as imagens preparadas.
- Instalações, remoções e alterações de pacotes atualizam a Home aberta, agrupando eventos próximos. Apps preinstalados com interface de TV também são descobertos.
- Galeria com cache de miniaturas de 1 MiB, uma decodificação ativa e descarte de solicitações de linhas recicladas. Consulta da galeria separada das miniaturas e cancelável.
- Carregar mais imagens permite acessar arquivos além das primeiras 200 imagens recentes.
- Acessibilidade recalcula seu filtro quando launchers são instalados, removidos ou reativados.
- Mudanças de resolução/configuração recriam a tela e recuperam o card selecionado. Importações antigas de wallpaper são canceladas, sem substituir o fundo atual por um resultado obsoleto.
- A compilação exige a chave e o certificado originais. Não gera uma chave substituta silenciosamente.

Esta versão foi compilada e verificada no computador. A instalação, navegação com controle e medições de RAM/CPU na TV estão pendentes. O desenho dos cards, do cabeçalho e dos painéis foi mantido no código.

## Menu do aplicativo — versão 1.6

Segure **OK** sobre um app para abrir um painel com seu ícone, nome e ações:

- **Abrir:** inicia o app.
- **Mover:** abre uma lista de posições; escolha com as setas e confirme com OK. A ordem é salva e os novos apps continuam sendo descobertos.
- **Ocultar:** retira o atalho da Home, mantendo o app instalado. O card + permite recuperá-lo.
- **Informações do app:** abre a tela nativa de armazenamento, permissões e demais opções.
- **Desinstalar:** abre o desinstalador do Android com confirmação. Apps atualizados do sistema mostram Desinstalar atualizações; apps de sistema sem atualização não mostram remoção direta.

As opções de remoção usam destaque de cor próprio. A navegação da fileira para nas extremidades, inclusive no +.

## Aplicativos e card +

Na versão 1.6.1, a largura passou de 258 para 288 unidades de desenho (192 px na TV atual), com altura proporcional. Cabeçalho, menus, espaçamento e preferências foram preservados.

Correção 1.5.2: a largura mínima medida e o padding real da fileira reservam espaço após o último card. Corrige o corte direito que ainda ocorria no +.

Correção 1.5.1: área interna ampliada nas extremidades e abaixo da fileira para o zoom e a sombra dos cards, incluindo o +. O tamanho normal e o espaçamento entre os cards foram mantidos.

- Cards com menor intervalo: 24 unidades no desenho de 1920 × 1080, equivalente a 16 px na TV atual. A versão 1.6.1 aumentou os cards em aproximadamente 12%, mantendo o intervalo entre eles.
- O último card tem somente **+**. OK abre um painel lateral com os aplicativos ocultos disponíveis, usando seus nomes e ícones.
- Escolha um aplicativo para adicioná-lo de volta. O painel fecha e o app restaurado fica selecionado na fileira.
- Segure **OK** sobre um app e escolha **Ocultar** para retirar seu atalho. O app continua instalado e habilitado.
- O **+** permanece visível mesmo quando não há apps ocultos. Nesse caso, o painel informa que a lista está vazia e permite voltar à tela inicial.
- A opção Apps ocultos foi retirada das configurações. A recuperação agora é feita pelo **+**.
- A fileira usa rolagem horizontal quando necessário; não há o antigo limite de seis apps. A ordem inicial vem de apps.txt ou da lista personalizada salva, complementada por apps compatíveis, incluindo os de fábrica.
- Play Store participa da fileira como qualquer aplicativo. Ocultações são preservadas em atualizações.

## Cabeçalho e configurações

Na versão 1.5.3, o relógio grande e a data voltaram à esquerda, com o estilo original. Os dois ícones à direita continuam selecionáveis e com seus atalhos.

- **Cima** em um app seleciona a engrenagem; **Esquerda** seleciona Wi-Fi. **OK** no Wi-Fi abre a configuração de rede da TV. Direita retorna à engrenagem e Baixo retorna ao card usado anteriormente.
- O ícone informa o estado real da rede. Claro: conectado com internet. Âmbar com aviso: conectado sem internet validada. Riscado: desconectado. Ethernet usa um ícone de cabo.
- **Engrenagem** ou **Menu** abre o painel lateral: Configurações da TV, Wallpaper e Definir como padrão. Entradas HDMI aparecem quando disponíveis.
- Voltar/Esquerda retorna uma página ou fecha o painel. Definir como padrão abre a escolha nativa do Android e o acesso à acessibilidade.
- O serviço opcional de acessibilidade detecta somente a abertura de outras atividades Home. Não pede leitura de telas, filtragem de teclas ou gestos.

## Wallpaper

**Engrenagem → Wallpaper → Escolher imagem ou GIF** permite usar arquivos locais em Downloads/Imagens. O arquivo é copiado para o armazenamento privado do launcher. Para remover a cópia, escolha Voltar ao fundo original.

Android 9 ou superior. Arquivos de até 32 MB; decodificação limitada a 1280 × 720 preservando proporção, com corte central na exibição. GIF pausa ao abrir outro app e retoma ao voltar. Sem vídeos, Reddit ou sincronização online. Não solicita internet, microfone ou localização; usa permissões normais para consultar o estado da rede.

## Modificar e compilar

Projeto em **Documents/Estudo/Home Simples**. Consulte DESENVOLVIMENTO.md para o mapa dos arquivos. O atalho atualizar-tv.command compila e instala na TV, preservando os dados.

Requer Python 3, JDK, Android SDK platform 34 e build tools 37.0.0. Execute `python3 build.py`; saída em build/nimbo.apk. ANDROID_HOME e ANDROID_BUILD_TOOLS permitem selecionar as ferramentas.

Execute `python3 tests/run.py` para as regressões sem TV. Para verificar a compilação sem a chave privada, use `python3 build.py --unsigned`; esse APK não é uma atualização instalável da versão assinada.

Preserve **home-simples.jks** e **signing-certificate.sha256**: é a chave necessária para atualizar a instalação atual. Ela não entra no ZIP de fonte nem no controle de versão. As versões anteriores estão em releases/ e as verificações em docs/.

Antes de desinstalar, escolha outro launcher e desligue o serviço Nimbo — botão Início. A licença original está em LICENSE e no recurso tv_detox_license incluído no APK.
