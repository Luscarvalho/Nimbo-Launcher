#!/bin/zsh
set -e
cd -- "$(dirname -- "$0")"
python3 build.py
TV_ALVO=${1:-${TV_ALVO:-}}
if [[ -z "$TV_ALVO" ]]; then
  TV_ALVO=$(adb devices | awk 'NR>1 && $2=="device" {print $1}')
  if [[ -z "$TV_ALVO" || "$TV_ALVO" == *$'\n'* ]]; then
    echo "Informe a TV: ./atualizar-tv.command IP:PORTA (ou defina TV_ALVO)."
    exit 1
  fi
fi
if ! adb -s "$TV_ALVO" get-state >/dev/null 2>&1; then
  adb connect "$TV_ALVO"
fi
adb -s "$TV_ALVO" install -r --user 0 build/nimbo.apk
adb -s "$TV_ALVO" shell am start -W -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER -n com.nimbo.launcher/.MainActivity
