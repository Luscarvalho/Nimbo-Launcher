# Validação Nimbo 1.9.0 — 21/10/2026

- Novo pacote/namespace com.nimbo.launcher; versão 1.9.0, código 24. Nome visível Nimbo preservado.
- Manifesto, metadados da licença, settingsActivity, fontes Java, classes geradas, testes e script de atualização usam o novo identificador. Compilação produz build/nimbo.apk.
- Certificado de assinatura original preservado. A chave mantém o nome histórico home-simples.jks e não integra o ZIP distribuído.
- Compilação, assinatura e 15 regressões JVM mais os dois testes de proteção da chave aprovados. Script de atualização validado por zsh -n. Lint: zero erros e oito avisos preexistentes, sem novas supressões.
- Na TV Box HD, usuário 0, instalado o novo pacote e transferidos shared_prefs/config.xml e files/wallpaper.image, conferidos por SHA-256.
- Migração por versões temporárias de desenvolvimento assinadas, isoladas das fontes de distribuição. Em seguida ambos os pacotes voltaram a APKs de produção; DEBUGGABLE ausente em ambos, verificado no Android.
- com.nimbo.launcher/.MainActivity confirmado como Home padrão. A escolha de acessibilidade existente foi transferida ao novo ComponentName, mantendo os outros serviços; novo serviço vinculado, capabilities=0, sem leitura de telas.
- Pacote antigo com.homesimples.launcher desativado, sem desinstalação nem limpeza de seus dados. Nova Home aberta na TV; wallpaper e fileira de aplicativos conferidos visualmente.
- Nenhuma alteração nos demais aplicativos, Google Cast ou configurações de energia. Não houve reinício nem desligamento.
- Ferramentas e arquivo temporário com dados pessoais não fazem parte do app ou do ZIP de código-fonte.
