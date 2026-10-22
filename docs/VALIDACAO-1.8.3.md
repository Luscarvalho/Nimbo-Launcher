# Validação Nimbo 1.8.3 — 20/10/2026

- Versão 1.8.3, código 21. Pacote, assinatura, backup e preferências preservados.
- Wallpaper abre diretamente WallpaperPickerActivity a partir das configurações. Removida a página intermediária Escolher imagem ou GIF.
- A opção Voltar ao fundo original foi movida ao seletor e aparece quando há um wallpaper personalizado. A escolha retorna RESULT_RESET; MainActivity executa WallpaperController.clear no request correspondente. Selecionar imagem mantém RESULT_OK e URI; cancelar não altera o fundo.
- Compilação e assinatura aprovadas. Android Lint: zero erros e oito avisos preexistentes (OldTargetApi 1, DiscouragedApi por orientação 3, RtlHardcoded 3, DataExtractionRules 1), sem novas supressões.
- Emulador Android TV 14: abertura direta, retorno, seleção de PNG temporário, presença da restauração no seletor e restauração do fundo original conferidos. A janela translúcida foi avaliada por capturas reais, pois parte das leituras UIAutomator retorna a árvore da Home subjacente.
- Imagem temporária removida e permissões de imagens retornadas ao estado anterior ao fim do teste. Não houve conexão com a TV física.
