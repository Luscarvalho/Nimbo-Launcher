# Testes no emulador — Home Simples 1.7.0

Executados em 20/10/2026 no AVD Television_1080p, Android TV 14/API 34, ARM64, 1920 × 1080, densidade 320 e 2 GiB de RAM configurada. Dispositivo ADB: emulator-5554. Nenhuma conexão com a TV física.

## Método

Instalação do APK assinado; navegação com eventos DPAD/Menu/Voltar; hierarquia UIAutomator para verificar foco e textos; capturas de tela e logs AndroidRuntime/HomeSimples. Quatro APKs mínimos temporários permitiram testar instalação, remoção e rolagem sem remover apps preexistentes. Dois arquivos sintéticos permitiram testar imagens e GIFs.

## Resultados

| Verificação | Resultado observado |
|---|---|
| Inicialização e desenho da Home | Abertura e renderização corretas em 1080p, com primeiro card selecionado |
| Apps de fábrica | Play Store, YouTube e Cast moderator descobertos no AVD |
| Instalação com Home visível | Apps de teste apareceram sem sair e voltar |
| Remoção com Home visível | App temporário removido desapareceu da lista |
| Fileira e card + | Rolagem e limite direito mantiveram foco no +, com zoom e sombra visíveis |
| Menu por pressão longa | Abrir, Mover, Ocultar, Informações do app e Desinstalar presentes |
| Ocultar/restaurar | App ocultado apareceu no + e recuperou foco ao restaurar |
| Mover | App movido à primeira posição, com foco recuperado |
| Configuração externa | Extra apps com pacote inexistente foi ignorado; lista permaneceu intacta |
| Atalho de rede | Abriu configurações nativas e voltou com foco no indicador |
| Permissão de imagens | Diálogo do Android TV solicitou acesso; arquivos foram listados após permitir |
| Wallpaper GIF | Seleção e aplicação confirmadas por imagem e log animated=true |
| Ciclo do GIF | Logs gif_stop ao abrir configurações nativas e gif_start ao voltar |
| Remover wallpaper | Fundo padrão restaurado e opção de remoção retirada do painel |
| Resolução e foco | Foco preservado na repetição com controle remoto; resolução de 1080p restaurada e abertura limpa conferida |
| Falhas em execução | Nenhuma FATAL EXCEPTION no log filtrado coletado durante estes fluxos |

Nos testes de foco, interações por mouse podem deixar o Android em modo de toque, sem card focado até uma tecla direcional. A sequência de resolução foi repetida com DPAD e sem interação externa para separar esse efeito. A primeira tentativa inconclusiva não foi considerada uma aprovação. No emulador, restauração de resolução foi complementada por uma inicialização limpa em 1080p.

## Limites

Estes testes não medem o consumo da box nem garantem ausência de bugs. RAM/CPU, fluidez e comportamento do firmware físico continuam pendentes. Não foram exercitados nesta etapa: galeria com mais de 200 imagens, estresse prolongado, confirmação do desinstalador nativo, escolha de HOME padrão e ativação do serviço de acessibilidade.

## Estado ao terminar

Apps temporários, imagens sintéticas e dados criados por este teste foram removidos. Home Simples ficou instalado e aberto, em estado inicial. O launcher padrão original do emulador (Google) foi mantido. Para experimentar, use as setas, OK, pressão longa em OK, Menu e Voltar. Home do sistema continuará abrindo o launcher Google até você escolher outro padrão.

O APK é o mesmo 1.7.0 entregue anteriormente; não houve alteração de código nesta etapa.
