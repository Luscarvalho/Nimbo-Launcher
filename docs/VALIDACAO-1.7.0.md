# Validação 1.7.0 — 20/10/2026

Versão 1.7.0, código 15. Correções da revisão de saúde da versão 1.6.1.

## Concluído no computador

- Compilação completa com SDK 34 e build tools 37.0.0, sem erros. Sete avisos de APIs depreciadas que continuam compatíveis com o mínimo Android 9 e a TV Android 14.
- Android Lint: zero erros e 20 avisos, contra 27 na versão anterior. Permanecem avisos de tradução/RTL, alvo Android 14, orientação em futuras versões, banner vetorial e regras modernas de backup. Não foram ocultados com suppressões.
- Quinze verificações JVM: ordem, apps ocultos, restauração após mudança de posição, pacotes ausentes/reinstalados, duplicatas, limites de posição, substituição de mil pedidos com tarefa lenta, cancelamento e encerramento do executor.
- Dois cenários de assinatura: chave ausente e certificado divergente interrompem a geração antes de substituir a pasta build. Não é criada uma chave nova.
- Inspeção do bytecode da MainActivity: sem leitura de extras de configuração ou carregamento de ícones/nomes/rasterização de bitmaps nessa classe. Dados e imagens são preparados em AppCatalog.
- Manifesto mantém HOME pública, não adiciona INTERNET e permite recriação por mudanças de resolução, idioma e modo de interface.

Comando das regressões: `python3 tests/run.py`.

## Alterações entregues

1. Removido o setter de configuração por Intent externo; preferências privadas antigas preservadas.
2. Cache de miniaturas de 1 MiB e uma decodificação por vez, com deduplicação e descarte de pedidos de linhas reutilizadas. Consulta de imagens independente e cancelável.
3. AppCatalog prepara dados e imagens em segundo plano, usa cache de 2 MiB e invalida imagens por pacote. Mover/ocultar não recarrega os recursos dos apps.
4. Home visível recebe atualizações agrupadas em 250 ms; em segundo plano, atualiza ao retornar. Início também respeita uma atualização pendente.
5. Serviço de acessibilidade recalcula as atividades HOME após instalação, remoção, atualização ou reativação de pacotes.
6. Apps de fábrica com entrada LEANBACK_LAUNCHER são descobertos mesmo sem atualização; ferramentas conhecidas e outras HOME não entram automaticamente nessa descoberta.
7. Recriação pelo Android recalcula escala e recupera o foco por nome de pacote.
8. Compilação assinada exige a identidade original do certificado. Compilação --unsigned disponível somente para análise sem chave.
9. Erros de imagem geram log e substituto seguro, sem devolver um Drawable potencialmente grande. Temporários de wallpaper têm nomes únicos; tarefas obsoletas são canceladas e temporários antigos são limpos.
10. Galeria oferece Carregar mais após lotes de 200 imagens. Recursos da licença e animação são referenciados por R, e Wi-Fi é opcional para aparelhos apenas Ethernet.

## Pendente na TV

Não houve conexão ou instalação nesta etapa, conforme solicitado. Ainda precisam ser verificados na box:

- Abertura fria e retorno à Home; foco no primeiro app, último card +, Wi-Fi, engrenagem e menu longo.
- Mover, ocultar, restaurar e atualizar o APK mantendo ordem, Downloader oculto e wallpaper.
- Instalar/remover um app enquanto a Home permanece visível; não deve ser necessário sair e voltar.
- Selecionar wallpaper, atualizar a galeria durante carregamento e navegar rapidamente por muitas imagens.
- GIF animado: aplicação, pausa ao abrir outro app e retorno; cancelar uma importação não deve substituir o fundo por resultado antigo.
- Trocar resolução e reativar outro launcher com o serviço de acessibilidade conectado.
- RAM, CPU, frames e possíveis vazamentos durante uso prolongado. Não há ganho numérico de consumo confirmado nesta etapa.

O visual foi preservado nas constantes e componentes; esta validação estática não certifica o resultado visual no firmware da TV.
