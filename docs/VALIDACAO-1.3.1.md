# Validação 1.3.1 — cores dos ícones

Correção de LineIcon.setColorFilter e setAlpha: chamar invalidateSelf após alterar o Paint. O desenho anterior era reutilizado nas mudanças de foco, apesar da mudança da cor do fundo.

Validado na TV SEI300BHM com Android 14:

- Engrenagem clara no fundo escuro, escura sobre o fundo claro ao ganhar foco e novamente clara ao perder foco.
- Painel aberto pelo controle: navegar de Configurações da TV para Wallpaper, Definir como padrão e Apps ocultos.
- Ícone da opção selecionada escuro sobre fundo claro; os que perderam foco voltam à cor clara sobre fundo escuro.
- Textos permanecem visíveis. Nenhuma alteração na lógica de aplicativos ou wallpapers.
- APK compilado na pasta oficial do projeto e instalado com preservação dos dados.

SHA-256 do APK: `0c9530a147b5da91847ad93ea3cfeeb8ec21510d91d4c224e6bdc9b19ecb5ee3`.
