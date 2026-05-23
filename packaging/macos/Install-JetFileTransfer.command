#!/bin/bash
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
APP="${DIR}/JetFileTransfer.app"

if [[ ! -d "${APP}" ]]; then
  osascript -e 'display alert "Jet File Transfer" message "JetFileTransfer.app bulunamadi. Zip dosyasini once acin." as critical'
  exit 1
fi

xattr -cr "${APP}"
open "${APP}"
