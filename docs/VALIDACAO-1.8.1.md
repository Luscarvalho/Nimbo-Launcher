# Validação Nimbo 1.8.1 — 20/10/2026

- Versão 1.8.1, código 19; pacote e certificado originais preservados.
- Configuração do botão Início integrada ao próprio SettingsSidebar da Home, mantendo wallpaper, cores, margens, tipografia, ícones e foco dos outros menus. Removido o desenho exclusivo da configuração 1.8.0.
- Ações de configuração compartilhadas entre a Home e a entrada externa LauncherSetupActivity. Retorno às configurações por Esquerda/Voltar, fechamento pelo botão Voltar e restauração do foco ao retornar de telas nativas.
- Compilação e assinatura aprovadas; 15 verificações JVM e dois cenários de proteção de assinatura passaram.
- Android Lint: zero erros, oito avisos preexistentes: OldTargetApi (1), DiscouragedApi por orientação (3), RtlHardcoded (3), DataExtractionRules (1). Nenhuma supressão adicionada.
- Emulador Android TV 14 / API 34: abertura da página a partir das configurações, foco e contraste em cada opção, abertura/cancelamento do seletor nativo, ida e volta das configurações de acessibilidade e navegação entre níveis aprovados.
- Teste Início e entrada externa conferidos no emulador, sem trocar o launcher padrão nem ativar o serviço de acessibilidade.
- Nenhuma conexão com a TV física. Sem medições novas de RAM/CPU. Nome Nimbo, ícone, banner, preferências, wallpaper e regras de backup preservados.
