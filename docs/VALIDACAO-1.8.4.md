# Validação Nimbo 1.8.4 — 20/10/2026

- Versão 1.8.4, código 22; pacote, chave, dados, preferências e backup preservados.
- Adicionado Configurações → Menu de energia, com o mesmo componente de linha, foco e recuos das outras opções.
- Ação exclusiva: AccessibilityService.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG). Sem permissão REBOOT, root, comando shell, leitura de telas ou filtragem de teclas no aplicativo.
- Referência da instância conectada do serviço removida em onUnbind/onDestroy. Desconexão abre orientação de ativação; falha ao abrir o menu mostra mensagem. Retorno das configurações nativas atualiza a página sem abrir o menu automaticamente.
- Nome/descrição do serviço atualizados para Nimbo — Início e energia, preservando seu ComponentName e configuração de capacidades.
- Emulador Android TV 14 / API 34: foco no botão, abertura do ActionsDialog nativo com Desligar e Reiniciar, cancelamento por Voltar, orientação com serviço desligado, atalho para acessibilidade e atualização ao retornar com o serviço ligado conferidos.
- Nenhuma ação de desligar ou reiniciar selecionada. Identificador de boot comparado antes/depois e preservado. Estado inicial de acessibilidade restaurado.
- Algumas leituras UIAutomator mostraram a árvore da Home sob painéis. A validação de foco e diálogo foi feita por capturas diretas, sem usar UIAutomator para interagir com o serviço em teste.
- Compilação e assinatura aprovadas. Android Lint: zero erros e oito avisos preexistentes: OldTargetApi (1), DiscouragedApi por orientação (3), RtlHardcoded (3), DataExtractionRules (1). Sem novas supressões.
- Nenhuma conexão com a TV física; as opções do menu dependem do firmware do aparelho. Nenhuma medição nova de RAM/CPU.
