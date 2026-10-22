# Validação da versão 1.3

TV box SEI300BHM, Android 14. Testes realizados em 18/10/2026.

- Tela inicial com relógio compacto e engrenagem no canto superior direito, sem segunda fileira de atalhos.
- Play Store no mesmo grupo de UniTV, YouTube e Downloader, com banner do aplicativo.
- Seta Cima leva dos apps à engrenagem; OK abre o painel lateral modal à direita.
- Botão Menu abre e mantém o painel; a abertura ocorre ao soltar a tecla para evitar fechamento imediato no mesmo evento.
- Navegação com as setas e destaque de foco nas opções do painel.
- Wallpaper abre uma página lateral e permite entrar no seletor de imagens e GIFs.
- Definir como padrão abre LauncherSetupActivity, que mostra o launcher padrão e a acessibilidade ativos.
- Configurações da TV abre com.android.tv.settings/.MainSettings.
- Pressão longa em OK sobre a Play Store abre o menu Ocultar sem iniciar a loja.
- Ocultar retira seu atalho da fileira. Apps ocultos lista a loja no painel e restaura o atalho individualmente.
- Voltar retorna de Apps ocultos ao painel principal; outro Voltar fecha o painel.
- Todos os atalhos usados nos testes foram restaurados ao final.
- APK final compilado diretamente de Documents/Estudo/Home Simples e instalado com preservação de dados.
- Após a instalação, Home Simples continua resolvido como HOME e seu serviço de acessibilidade permanece ativo.

APK SHA-256: `402fd31a9d17c0e3cfb7745f518849249a902278bf282d12878711a02cb70620`.

A versão mantém a rotina de wallpapers e o armazenamento de apps ocultos das versões anteriores. Não foi feita uma nova medição de RAM nem aplicada uma imagem de fundo durante esta rodada de testes.
