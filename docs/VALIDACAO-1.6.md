# Validação 1.6 — menu de aplicativos

TV SEI300BHM com Android 14. Compilação na pasta oficial e instalação com os dados preservados.

- Pressão longa abre painel com ícone, nome, Abrir, Mover, Ocultar, Informações do app e Desinstalar para app comum.
- Ação Abrir iniciou org.localsend.localsend_app/.MainActivity.
- Informações abriu com.android.tv.settings/.device.apps.AppManagementActivity.
- Desinstalar abriu com.google.android.packageinstaller/com.android.packageinstaller.UninstallerActivity, registrado pelo Android. O teste foi cancelado e o LocalSend continuou instalado. Nenhuma remoção foi confirmada.
- Mover abriu a lista de posições. UniTV foi colocado na segunda posição e sua posição física foi verificada pela hierarquia. Depois, voltou à primeira posição e a ordem anterior foi confirmada.
- Ocultar retirou o UniTV; o + permaneceu acessível e listado com focused=true após navegar até o fim.
- Recuperação pelo + restaurou UniTV. O Downloader continuou oculto, preservando a preferência anterior do usuário.
- Navegação corrigida para parar nas extremidades sem retorno do foco ao início.
- Relógio, ícones e wallpaper preservados. Menu final do LocalSend deixado aberto.

O caminho de desinstalação de atualizações de app de sistema não foi executado. A lógica respeita os flags do pacote e a confirmação nativa; não foi realizada desinstalação real em qualquer teste.

SHA-256 do APK: `4bf3d7e8c1192784a497194f5b0f4268a426701e438f2b63e5517e6becfb6b21`.
