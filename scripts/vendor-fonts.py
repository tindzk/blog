# /// script
# dependencies = ["fonttools==4.60.1", "brotli==1.1.0"]
# ///
"""Run `uv run scripts/vendor-fonts.py` to reproduce the bundled web fonts."""
from io import BytesIO
from pathlib import Path
from urllib.parse import quote
from urllib.request import urlopen

from fontTools.ttLib import TTFont

REVISION = "86aced91de05cd4f96a06838124cca45be337b2b"
ROOT = Path(__file__).resolve().parent.parent
FONTS = [
    ("inter", "Inter[opsz,wght].ttf", "Inter-normal-047c92f6e221.woff2"),
    ("inter", "Inter-Italic[opsz,wght].ttf", "Inter-italic-c17757842824.woff2"),
    ("jetbrainsmono", "JetBrainsMono[wght].ttf", "JetBrainsMono-normal-aa310be8b717.woff2"),
    ("jetbrainsmono", "JetBrainsMono-Italic[wght].ttf", "JetBrainsMono-italic-5210f7350c95.woff2"),
]


def download(family, name):
    url = f"https://raw.githubusercontent.com/google/fonts/{REVISION}/ofl/{family}/{quote(name)}"
    with urlopen(url, timeout=30) as response:
        return response.read()


for family, source, target in FONTS:
    font = TTFont(BytesIO(download(family, source)), recalcTimestamp=False)
    font.flavor = "woff2"
    font.save(ROOT / "static" / target)
    print(target)

for family in ["inter", "jetbrainsmono"]:
    (ROOT / "static" / f"{family}-OFL.txt").write_bytes(download(family, "OFL.txt"))
