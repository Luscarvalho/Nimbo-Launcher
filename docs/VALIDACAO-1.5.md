# Validação 1.5 — adicionar apps pelo card +

TV SEI300BHM, Android 14. APK compilado diretamente da pasta oficial e instalado com dados preservados.

- Fileira com cards do mesmo tamanho e intervalo reduzido de 24 para 16 px na TV atual.
- Card vazio com + após os aplicativos, acessível pelo controle.
- Painel lateral vazio com mensagem e opção Voltar à tela inicial.
- Pressão longa na Play Store abriu Ocultar; o atalho foi ocultado temporariamente.
- + abriu Adicionar aplicativos com nome e ícone da Play Store.
- Escolher a Play Store restaurou o atalho e fechou o painel.
- Restauração repetida usando OK no controle; a hierarquia confirmou o app restaurado com focused=true após o fechamento.
- Menu de configurações conferido sem Apps ocultos; Configurações da TV, Wallpaper e Definir como padrão continuam presentes.
- Todos os aplicativos utilizados nos testes foram restaurados. O wallpaper escolhido pelo usuário foi preservado.

A rolagem horizontal usa o componente nativo do Android e remove o antigo corte em seis apps. Nesta rodada havia cinco aplicativos, portanto não foi exercitado excesso de cards além da largura da tela.

SHA-256 do APK: `41954d807f4418ced81ac58e5cefc8a57cfac62e17753def92a207eb4efd4fc2`.
