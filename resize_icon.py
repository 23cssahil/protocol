import os
from PIL import Image, ImageDraw

src = r"C:\Users\DELL\.gemini\antigravity-ide\brain\413f9281-2f10-436c-8107-2c71b04cd026\protocol_app_icon_1787829252697.jpg"
app_res = r"C:\Users\DELL\.gemini\antigravity-ide\scratch\protocol-app\app\src\main\res"

sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192
}

img = Image.open(src).convert("RGBA")

for folder, size in sizes.items():
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    os.makedirs(os.path.join(app_res, folder), exist_ok=True)
    
    # Save standard icon
    path = os.path.join(app_res, folder, "ic_launcher.png")
    resized.save(path, "PNG")
    
    # Create round icon
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size, size), fill=255)
    
    round_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    round_img.paste(resized, (0, 0), mask=mask)
    
    path_round = os.path.join(app_res, folder, "ic_launcher_round.png")
    round_img.save(path_round, "PNG")

print("Icons generated successfully in all mipmap folders!")
