# Validação Nimbo 1.8.2 — 20/10/2026

- Versão 1.8.2, código 20. Pacote, certificado, backup e preferências preservados.
- Seletor de wallpaper no formato de painel lateral, alinhado à aparência do SettingsSidebar: fundo, largura, recuos, fontes, ícones e contraste do foco.
- Linhas de ação compartilhadas com os menus existentes. A galeria mantém ListView e carregamento assíncrono/cache de miniaturas. Destaque da lista condicionado a foco/pressão, para não deixar um card claro ao subir para as opções.
- Removida a opção Sobre o Nimbo, seu diálogo e textos sem uso. A licença MIT continua incluída no APK e em LICENSE; o manifesto referencia seu recurso bruto.
- Compilação e assinatura aprovadas. Regressões: 15 verificações JVM e dois testes de proteção da chave aprovados.
- Android Lint: zero erros e oito avisos preexistentes, sem novas supressões: OldTargetApi (1), DiscouragedApi por orientação (3), RtlHardcoded (3), DataExtractionRules (1).
- Emulador Android TV 14 / API 34: menu sem Sobre, seletor sem permissão, concessão de acesso, estado sem imagens, miniatura/nome/dimensões e navegação entre ações e galeria conferidos por captura real. Aplicação de PNG temporário e restauração do fundo original conferidas.
- O emulador resolve Outros locais para com.android.tv.frameworkpackagestubs/.Stubs$DocumentsStub, que retorna sem fornecer arquivos. Um seletor externo real não foi validado nesse emulador; a consulta local MediaStore funcionou.
- O UIAutomator retornou a árvore da Home sob a janela translúcida em parte das leituras. A aparência e o foco do seletor foram avaliados pelas capturas reais, junto da janela ativa e das ações executadas.
- Imagem temporária removida e permissões de teste devolvidas ao estado anterior ao encerrar a validação. Nenhuma conexão com a TV física nesta etapa.
