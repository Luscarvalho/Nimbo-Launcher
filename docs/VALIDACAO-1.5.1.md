# Validação 1.5.1 — zoom e sombra dos cards

Reproduzido o corte superior no card da Play Store. A reserva externa do scroll não protegia o desenho que ultrapassava a área interna da fileira.

Correção: espaço interno acima e nas extremidades dos cards, reserva inferior para legenda e sombra, além de solicitação para manter o retângulo do foco visível.

Testado na TV SEI300BHM, Android 14:

- Card no meio da fileira ampliado, com borda superior e cantos inteiros.
- Primeiro card ampliado com bordas e sombra lateral/inferior sem o corte da área anterior.
- Card + selecionado na extremidade, com espaço para o zoom e a sombra.
- Navegação entre os cards pelo controle.
- Tamanho normal, intervalo entre cards e wallpaper preservados.

APK gerado na pasta oficial do projeto e instalado mantendo os dados. SHA-256: `15e6e19de3eee741cc1d098ed62cb90b62663d5bd2780c723b07cdd21adc73f2`.
