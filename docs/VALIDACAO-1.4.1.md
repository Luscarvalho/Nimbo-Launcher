# Validação 1.4.1 — atalho de Wi-Fi

TV SEI300BHM com Android 14. APK gerado na pasta oficial e instalado com os dados preservados.

- Cima a partir de um app seleciona a engrenagem; Esquerda seleciona Wi-Fi.
- Ícone de Wi-Fi escuro sobre o círculo claro ao ganhar foco; volta à cor normal ao sair do foco.
- OK abriu diretamente com.android.tv.settings/.connectivity.NetworkActivity por ACTION_WIFI_SETTINGS.
- Voltar retornou ao launcher com Wi-Fi ainda selecionado, confirmado pela hierarquia da interface.
- Direita e Esquerda alternam entre Wi-Fi e engrenagem; Baixo retorna ao aplicativo usado anteriormente.
- Indicador continuou informando Wi-Fi conectado com internet após retornar da configuração.
- As configurações de conexão não foram alteradas durante o teste.

SHA-256 do APK: `196f047298090390bb32e155efc2a647566f7fcc090a066c75477675baa660eb`.
