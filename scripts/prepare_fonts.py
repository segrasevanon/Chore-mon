"""Make explicit static weights so Android displays the bundled variable font reliably."""
from pathlib import Path
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont

root = Path(__file__).resolve().parents[1] / "app/src/main/res/font"
for name, weight in [("regular", 400), ("medium", 500), ("semibold", 600), ("bold", 700), ("extrabold", 800), ("black", 900)]:
    font = TTFont(root / "nunito.ttf")
    instantiateVariableFont(font, {"wght": weight}, inplace=True).save(root / f"nunito_{name}.ttf")
    print(f"Nunito {weight} prepared")
