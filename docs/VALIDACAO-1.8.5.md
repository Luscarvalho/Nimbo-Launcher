# Validação Nimbo 1.8.5 — 20/10/2026

- Versão 1.8.5, código 23; remoção completa da função Menu de energia solicitada pelo usuário.
- MainActivity, HomeAccessibilityService, LineIcon e strings.xml comparados byte a byte com a versão 1.8.3, anterior à função, e restaurados a esse conteúdo. Não há chamada GLOBAL_ACTION_POWER_DIALOG, página de ativação ou referência estática do serviço.
- Nome/descrição da acessibilidade restaurados para Nimbo — botão Início. ComponentName, permissões, backup, dados e assinatura preservados.
- Compilação e assinatura aprovadas. Lint: zero erros e oito avisos preexistentes, sem novas supressões.
- Atualização instalada no emulador Android TV 14 com install -r, sem limpar dados. Configurações conferidas visualmente, sem a opção de energia.
- Nenhuma conexão com a TV física. Nenhuma ação de desligar ou reiniciar executada.
