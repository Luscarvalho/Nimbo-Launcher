# Validação Nimbo 1.8.0 — 20/10/2026

- Versão 1.8.0, código 18; pacote com.homesimples.launcher e certificado originais.
- Nome Nimbo aplicado aos textos de interface, nome do app e descrição do serviço. Marca, ícone e banner vetoriais; banner 160 × 90 dp.
- Tela do botão Início redesenhada com Views nativas, foco contrastante, cards, estados e navegação explícita pelo controle.
- Compilação e assinatura aprovadas. Android Lint: zero erros, oito avisos preexistentes: OldTargetApi (1), DiscouragedApi por orientação (3), RtlHardcoded (3) e DataExtractionRules (1). Sem novas supressões.
- Emulador Android TV 14 / API 34 / 1920 × 1080: foco nos três cards e no retorno, ida e volta das configurações nativas de acessibilidade, abertura e cancelamento do seletor nativo de launcher, teste do botão Início, retorno à Home e marca no painel lateral aprovados.
- Cancelar o seletor não gera outro diálogo. O teste Início abriu o launcher padrão existente do emulador.
- Atualização instalada no emulador com install -r, sem limpar dados. Não foi trocado o launcher padrão nem ativado o serviço de acessibilidade durante os testes.
- Nenhuma conexão com a TV física. RAM, CPU e navegação no aparelho físico não foram medidos nesta etapa.
- Configurações de backup, armazenamento de preferências e chave de assinatura preservados.
