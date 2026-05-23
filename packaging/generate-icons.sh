#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="${ROOT_DIR}/src/main/resources/images/app.png"
OUT="${ROOT_DIR}/packaging"

mkdir -p "${OUT}/icon.iconset"

if [[ ! -f "${SRC}" ]]; then
  echo "Logo not found: ${SRC}" >&2
  exit 1
fi

echo "Generating icons from ${SRC}"

sips -z 16 16 "${SRC}" --out "${OUT}/icon.iconset/icon_16x16.png" >/dev/null
sips -z 32 32 "${SRC}" --out "${OUT}/icon.iconset/icon_16x16@2x.png" >/dev/null
sips -z 32 32 "${SRC}" --out "${OUT}/icon.iconset/icon_32x32.png" >/dev/null
sips -z 64 64 "${SRC}" --out "${OUT}/icon.iconset/icon_32x32@2x.png" >/dev/null
sips -z 128 128 "${SRC}" --out "${OUT}/icon.iconset/icon_128x128.png" >/dev/null
sips -z 256 256 "${SRC}" --out "${OUT}/icon.iconset/icon_128x128@2x.png" >/dev/null
sips -z 256 256 "${SRC}" --out "${OUT}/icon.iconset/icon_256x256.png" >/dev/null
sips -z 512 512 "${SRC}" --out "${OUT}/icon.iconset/icon_256x256@2x.png" >/dev/null
sips -z 512 512 "${SRC}" --out "${OUT}/icon.iconset/icon_512x512.png" >/dev/null
sips -z 1024 1024 "${SRC}" --out "${OUT}/icon.iconset/icon_512x512@2x.png" >/dev/null

iconutil -c icns "${OUT}/icon.iconset" -o "${OUT}/app.icns"
cp "${SRC}" "${OUT}/app.png"
sips -z 512 512 "${SRC}" --out "${OUT}/app-512.png" >/dev/null

python3 - <<PY
from pathlib import Path
from PIL import Image

src = Path("${SRC}")
out = Path("${OUT}/app.ico")
img = Image.open(src).convert("RGBA")
img.save(out, format="ICO", sizes=[(16, 16), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)])
PY

echo "Created:"
echo "  ${OUT}/app.icns"
echo "  ${OUT}/app.ico"
echo "  ${OUT}/app-512.png"
