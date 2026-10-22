# Validação 1.5.2 — corte direito do +

Diagnóstico confirmado na TV: o + estava selecionado, mas sua área ancestral terminava em x=1176, exatamente na borda não ampliada do card. O espaço calculado na versão 1.5.1 não entrava na largura medida pelo HorizontalScrollView.

Correção: padding real na área interna e dimensões mínimas de medição, preservando tamanho, espaçamento e zoom dos cards.

APK compilado na pasta oficial e instalado na TV SEI300BHM, Android 14. A conferência final confirmou:

- + com focused=true.
- Espaço de 43 px após o limite normal do card dentro da área visível da fileira.
- Canto direito ampliado e sombra visíveis.
- Rolagem horizontal exercitada com mais aplicativos presentes na fileira.
- + deixado selecionado para revisão do usuário.

SHA-256 do APK: `b551d34c1741658d1a02c217981473cd97890c8de2aa3a2cc3f69954bcb29033`.
