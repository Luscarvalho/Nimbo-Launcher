# Validação 1.7.2 — 20/10/2026

- Versão 1.7.2, código 17; mesma chave e certificado das versões anteriores.
- Centralizados textos e formatos de menus, configuração, galeria, mensagens de wallpaper e descrição de rede. Português e conteúdo preservados; pluralização via recurso Android.
- Banner 160 × 90 dp com viewport e paths originais; proporção 16:9 preservada.
- Compilação e assinatura aprovadas.
- Android Lint: zero erros e oito avisos. Eliminados os onze SetTextI18n e o VectorRaster. Permanecem OldTargetApi (1), DiscouragedApi por orientação (3), RtlHardcoded (3) e DataExtractionRules (1). Nenhuma supressão adicionada.
- Conferidos no emulador Android TV 14: textos da Home, estado de rede, painel de configurações e resumo formatado de launcher/acessibilidade, incluindo quebra de linha.
- Regras de backup comparadas antes/depois e mantidas. Sem mudança de launcher padrão ou concessão de permissões durante a conferência.
- Nenhuma instalação na TV física; dados do emulador preservados na atualização.
