#!/usr/bin/env python3
"""
Faro Design - Textur-Generator
Erzeugt pro Kategorie 60 eigene Styles (20 Farben x 3 Varianten) als PNGs unter
src/main/resources/assets/farodesign/styles/<kategorie>/<style>/...
Der Ordnerbaum im Style-Ordner entspricht dem Pfad unter assets/minecraft/textures/.
Eigene Styles: einfach neuen Ordner mit gleichem Aufbau + preview.png hinzufuegen
und in index.json eintragen (oder dieses Skript erweitern) und neu bauen.
"""
import os, json, random, colorsys, math, shutil, sys
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
OUT = os.path.join(ROOT, "src/main/resources/assets/farodesign/styles")

COLORS = [
    ("Lavender", (181, 126, 220)), ("Light Blue", (120, 190, 255)), ("Light Green", (144, 238, 144)),
    ("Light Pink", (255, 182, 206)), ("Lilac", (200, 162, 200)), ("Magenta", (230, 40, 180)),
    ("Mint", (152, 255, 200)), ("Peach", (255, 203, 164)), ("Purple", (150, 60, 230)),
    ("Red", (225, 40, 50)), ("Orange", (255, 140, 20)), ("Yellow", (255, 225, 50)),
    ("Cyan", (30, 220, 230)), ("Blue", (50, 90, 240)), ("Green", (40, 180, 70)),
    ("Lime", (170, 230, 40)), ("White", (240, 240, 245)), ("Gray", (130, 135, 145)),
    ("Black", (60, 55, 80)), ("Gold", (245, 190, 40)),
]


def clamp(v): return max(0, min(255, int(v)))
def mul(c, f): return tuple(clamp(x * f) for x in c)
def mix(a, b, t): return tuple(clamp(a[i] * (1 - t) + b[i] * t) for i in range(3))
def hue_shift(c, deg):
    h, s, v = colorsys.rgb_to_hsv(*[x / 255 for x in c])
    r, g, b = colorsys.hsv_to_rgb((h + deg / 360) % 1, s, v)
    return (clamp(r * 255), clamp(g * 255), clamp(b * 255))


class Pal:
    def __init__(self, base, variant):
        if variant == 1:
            self.d, self.m, self.l = mul(base, .45), mul(base, .85), mix(base, (255, 255, 255), .3)
            self.a = hue_shift(base, 35)
        elif variant == 2:
            self.d, self.m, self.l = mul(base, .25), mul(base, .6), mul(base, .95)
            self.a = mix(hue_shift(base, 180), (255, 255, 255), .3)
        else:
            self.d, self.m, self.l = mix(base, (0, 0, 0), .3), mix(base, (255, 255, 255), .35), mix(base, (255, 255, 255), .7)
            self.a = (255, 255, 255)
        self.k = mul(self.d, .45)
        self.base = base

    def txt(self):
        f = lambda c: "%02x%02x%02x" % c
        return " ".join(f(x) for x in (self.d, self.m, self.l, self.a))


def canvas(w=16, h=None):
    img = Image.new("RGBA", (w, h or w), (0, 0, 0, 0))
    return img, ImageDraw.Draw(img)


# ---------------------------------------------------------------- Items (16x16)
def sword(p):
    i, d = canvas()
    d.line([(5, 11), (13, 3)], fill=p.m, width=2)
    d.line([(6, 10), (14, 2)], fill=p.l)
    d.point((14, 1), fill=p.l)
    d.line([(3, 8), (8, 13)], fill=p.a)
    d.line([(2, 14), (4, 12)], fill=p.d, width=2)
    d.point((1, 15), fill=p.a)
    return i

def axe(p):
    i, d = canvas()
    d.line([(2, 14), (10, 6)], fill=p.d, width=2)
    d.polygon([(7, 2), (12, 1), (14, 6), (11, 8), (9, 7)], fill=p.m, outline=p.k)
    d.line([(8, 3), (12, 2)], fill=p.l)
    d.point((13, 5), fill=p.l)
    d.point((10, 6), fill=p.a)
    return i

def pickaxe(p):
    i, d = canvas()
    d.line([(2, 14), (11, 5)], fill=p.d, width=2)
    d.line([(4, 5), (7, 2), (11, 2), (14, 5), (14, 9)], fill=p.m, width=2)
    d.line([(5, 4), (8, 2), (11, 2)], fill=p.l)
    d.point((14, 6), fill=p.l)
    d.point((11, 5), fill=p.a)
    return i

def mace(p):
    i, d = canvas()
    d.line([(2, 14), (9, 7)], fill=p.d, width=2)
    d.ellipse((7, 1, 14, 8), fill=p.m, outline=p.k)
    for pt in [(10, 0), (15, 4), (10, 9), (6, 4), (14, 1), (14, 8)]:
        d.point(pt, fill=p.l)
    d.point((9, 3), fill=p.l); d.point((10, 3), fill=p.l)
    d.point((11, 5), fill=p.a)
    d.line([(3, 12), (6, 9)], fill=p.a)
    return i

def spear(p):
    i, d = canvas()
    d.line([(1, 15), (10, 6)], fill=p.d, width=1)
    d.line([(2, 15), (11, 6)], fill=mix(p.d, p.m, .4), width=1)
    d.polygon([(9, 7), (11, 3), (15, 0), (12, 4), (13, 9)], fill=p.m, outline=p.k)
    d.line([(11, 5), (14, 1)], fill=p.l)
    d.point((10, 7), fill=p.a); d.point((9, 8), fill=p.a)
    d.point((3, 13), fill=p.a)
    return i

def trident(p):
    i, d = canvas()
    d.line([(2, 14), (11, 5)], fill=p.d, width=1)
    d.line([(10, 6), (14, 2)], fill=p.l, width=2)
    d.line([(8, 4), (11, 1)], fill=p.m, width=1)
    d.line([(12, 8), (15, 5)], fill=p.m, width=1)
    d.line([(8, 4), (12, 8)], fill=p.a)
    d.point((15, 1), fill=p.l)
    return i

def xp_bottle(p):
    i, d = canvas()
    d.rectangle((6, 1, 9, 2), fill=p.a)
    d.rectangle((6, 3, 9, 5), fill=p.k)
    d.ellipse((2, 5, 13, 15), fill=p.k)
    d.ellipse((3, 7, 12, 14), fill=p.m)
    d.ellipse((4, 9, 11, 14), fill=p.l)
    d.point((5, 8), fill=(255, 255, 255)); d.point((6, 8), fill=(255, 255, 255))
    d.point((9, 11), fill=p.a)
    d.rectangle((7, 4, 8, 5), fill=p.l)
    return i

def golden_apple(p, enchanted=False):
    i, d = canvas()
    d.ellipse((2, 4, 13, 14), fill=p.m, outline=p.k)
    d.ellipse((4, 6, 9, 11), fill=p.l if enchanted else mix(p.m, p.l, .5))
    for pt in [(4, 6), (5, 6), (4, 7)]:
        d.point(pt, fill=(255, 255, 255))
    d.line([(8, 4), (9, 1)], fill=p.d)
    d.rectangle((10, 1, 12, 2), fill=p.a)
    if enchanted:
        d.point((11, 10), fill=p.a); d.point((6, 12), fill=p.a)
    return i

def totem(p):
    i, d = canvas()
    d.ellipse((5, 1, 10, 6), fill=p.l, outline=p.k)
    d.point((6, 3), fill=p.k); d.point((9, 3), fill=p.k)
    d.polygon([(4, 7), (11, 7), (12, 13), (3, 13)], fill=p.m, outline=p.k)
    d.rectangle((1, 7, 3, 10), fill=p.l, outline=p.k)
    d.rectangle((12, 7, 14, 10), fill=p.l, outline=p.k)
    d.rectangle((4, 13, 6, 15), fill=p.d, outline=p.k)
    d.rectangle((9, 13, 11, 15), fill=p.d, outline=p.k)
    d.rectangle((7, 8, 8, 10), fill=p.a)
    return i

def crystal_item(p):
    i, d = canvas()
    d.polygon([(8, 0), (13, 5), (8, 15), (3, 5)], fill=p.m, outline=p.k)
    d.polygon([(8, 1), (8, 14), (4, 5)], fill=p.d)
    d.polygon([(8, 1), (12, 5), (8, 6)], fill=p.l)
    d.point((7, 3), fill=(255, 255, 255)); d.point((8, 2), fill=p.a)
    return i

def helmet(p):
    i, d = canvas()
    d.rectangle((3, 3, 12, 12), fill=p.m, outline=p.k)
    d.rectangle((5, 8, 10, 12), fill=(0, 0, 0, 0))
    d.line([(4, 4), (11, 4)], fill=p.l)
    d.rectangle((3, 7, 12, 7), fill=p.a)
    return i

def chestplate(p):
    i, d = canvas()
    d.rectangle((1, 1, 14, 6), fill=p.m, outline=p.k)
    d.rectangle((3, 6, 12, 15), fill=p.m, outline=p.k)
    d.rectangle((5, 1, 10, 3), fill=(0, 0, 0, 0))
    d.line([(4, 7), (11, 7)], fill=p.l)
    d.rectangle((7, 8, 8, 12), fill=p.a)
    return i

def leggings(p):
    i, d = canvas()
    d.rectangle((2, 1, 13, 6), fill=p.m, outline=p.k)
    d.rectangle((2, 6, 6, 15), fill=p.m, outline=p.k)
    d.rectangle((9, 6, 13, 15), fill=p.m, outline=p.k)
    d.line([(3, 2), (12, 2)], fill=p.l)
    d.rectangle((2, 4, 13, 4), fill=p.a)
    return i

def boots(p):
    i, d = canvas()
    d.rectangle((2, 4, 6, 13), fill=p.m, outline=p.k)
    d.rectangle((9, 4, 13, 13), fill=p.m, outline=p.k)
    d.rectangle((1, 11, 6, 14), fill=p.d, outline=p.k)
    d.rectangle((9, 11, 14, 14), fill=p.d, outline=p.k)
    d.line([(3, 5), (3, 9)], fill=p.l); d.line([(10, 5), (10, 9)], fill=p.l)
    return i


# ---------------------------------------------------------------- Bloecke / Entities
def noise_tile(p, seed, colors, size=16, smooth=True):
    rng = random.Random(seed)
    img = Image.new("RGBA", (size, size), (0, 0, 0, 255))
    g = [[rng.random() for _ in range(size)] for _ in range(size)]
    if smooth:
        g2 = [[(g[y][x] * 2 + g[(y + 1) % size][x] + g[y][(x + 1) % size] + g[(y - 1) % size][(x - 1) % size]) / 5
               for x in range(size)] for y in range(size)]
        g = g2
    n = len(colors)
    for y in range(size):
        for x in range(size):
            idx = min(n - 1, int(g[y][x] ** 1.0 * n))
            img.putpixel((x, y), colors[idx] + (255,))
    return img

def obsidian(p, seed):
    return noise_tile(p, seed, [mul(p.d, .5), mul(p.d, .75), p.d, mix(p.d, p.m, .5), p.m])

def glowstone(p, seed):
    img = noise_tile(p, seed, [p.d, p.m, p.m, p.l, mix(p.l, (255, 255, 255), .6)])
    return img

def anchor_side(p, seed, level):
    img = obsidian(p, seed)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), outline=p.k)
    for n in range(level):
        d.rectangle((2 + n * 3, 6, 3 + n * 3, 9), fill=p.l)
        d.point((2 + n * 3, 6), fill=p.a)
    return img

def anchor_top(p, seed, on):
    img = obsidian(p, seed)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), outline=p.k)
    d.rectangle((3, 3, 12, 12), fill=p.l if on else mul(p.d, .6), outline=p.m if on else p.k)
    if on:
        d.rectangle((6, 6, 9, 9), fill=p.a)
    return img

def armor_layer(p, seed, w=64, h=32):
    rng = random.Random(seed)
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for y in range(h):
        for x in range(w):
            r = rng.random()
            c = p.m
            if y % 4 == 0: c = p.d
            elif r > .86: c = p.l
            elif r < .12: c = mix(p.m, p.d, .5)
            if x % 16 == 0 or x % 16 == 15: c = mix(c, p.a, .35)
            img.putpixel((x, y), c + (255,))
    return img

def entity_fill(p, seed, w, h):
    rng = random.Random(seed)
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for y in range(h):
        for x in range(w):
            r = rng.random()
            c = p.m if r > .25 else p.d
            if r > .9: c = p.l
            img.putpixel((x, y), c + (255,))
    return img


# ---------------------------------------------------------------- Himmel / Glint
def sun(p):
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse((4, 4, 27, 27), fill=p.m + (80,))
    d.ellipse((8, 8, 23, 23), fill=p.l + (255,))
    d.ellipse((11, 11, 20, 20), fill=mix(p.l, (255, 255, 255), .6) + (255,))
    return img

def moon_phases(p):
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    for n in range(8):
        tile = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        d = ImageDraw.Draw(tile)
        d.ellipse((6, 6, 25, 25), fill=p.l + (255,))
        d.ellipse((10, 10, 14, 14), fill=p.m + (255,))
        d.ellipse((17, 16, 21, 20), fill=p.m + (255,))
        off = [0, 4, 9, 14, 40, -14, -9, -4][n]   # Phasen-Schatten
        if n != 0:
            sh = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
            ImageDraw.Draw(sh).ellipse((6 + off, 6, 25 + off, 25), fill=(0, 0, 0, 255))
            for y in range(32):
                for x in range(32):
                    if sh.getpixel((x, y))[3] and tile.getpixel((x, y))[3]:
                        tile.putpixel((x, y), p.d + (255,))
        img.paste(tile, ((n % 4) * 32, (n // 4) * 32))
    return img

def clouds(p, seed):
    rng = random.Random(seed)
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for _ in range(70):
        x, y = rng.randrange(256), rng.randrange(256)
        w, h = rng.randrange(6, 20), rng.randrange(3, 8)
        for ox in (-256, 0, 256):
            for oy in (-256, 0, 256):
                d.rectangle((x + ox, y + oy, x + ox + w, y + oy + h), fill=mix(p.l, (255, 255, 255), .4) + (255,))
    return img

def glint(p, seed, size=64):
    rng = random.Random(seed)
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    for y in range(size):
        for x in range(size):
            t = (math.sin((x + y) / size * math.pi * 6) + 1) / 2
            a = int(255 * (t ** 3) * (0.6 + 0.4 * rng.random()))
            c = mix(p.m, p.l, t)
            img.putpixel((x, y), c + (a,))
    return img


# ---------------------------------------------------------------- Kategorien
TOOL_TIERS = ["wooden", "stone", "copper", "iron", "golden", "diamond", "netherite"]
ARMOR_ITEM_MATS = ["chainmail", "copper", "iron", "golden", "diamond", "netherite"]
ARMOR_LAYER_MATS = ["chainmail", "copper", "iron", "gold", "diamond", "netherite"]


def save(img, base, rel):
    path = os.path.join(base, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def preview_of(img):
    if img.size == (16, 16):
        return img
    return img.resize((16, 16), Image.BILINEAR if img.size[0] > 16 else Image.NEAREST)


def build(cat, p, base, seed):
    """Schreibt die Dateien des Styles nach base und gibt die Vorschau (16x16) zurueck."""
    if cat == "glint":
        g = glint(p, seed)
        save(g, base, "misc/enchanted_glint_item.png")
        save(g, base, "misc/enchanted_glint_entity.png")
        pv = Image.new("RGBA", (16, 16), (20, 16, 30, 255))
        pv.alpha_composite(preview_of(g)); return pv
    if cat == "sky":
        s = sun(p)
        save(s, base, "environment/sun.png")
        save(moon_phases(p), base, "environment/moon_phases.png")
        save(clouds(p, seed), base, "environment/clouds.png")
        return preview_of(s)
    if cat == "armour":
        for m in ARMOR_ITEM_MATS:
            save(helmet(p), base, f"item/{m}_helmet.png")
            save(chestplate(p), base, f"item/{m}_chestplate.png")
            save(leggings(p), base, f"item/{m}_leggings.png")
            save(boots(p), base, f"item/{m}_boots.png")
        for m in ARMOR_LAYER_MATS:
            save(armor_layer(p, seed), base, f"entity/equipment/humanoid/{m}.png")
            save(armor_layer(p, seed + 1), base, f"entity/equipment/humanoid_leggings/{m}.png")
        return chestplate(p)
    if cat == "totem":
        t = totem(p); save(t, base, "item/totem_of_undying.png"); return t
    if cat in ("sword", "axe", "pickaxe"):
        fn = {"sword": sword, "axe": axe, "pickaxe": pickaxe}[cat]
        img = fn(p)
        for t in TOOL_TIERS:
            save(img, base, f"item/{t}_{cat}.png")
        return img
    if cat == "xp_bottle":
        x = xp_bottle(p); save(x, base, "item/experience_bottle.png"); return x
    if cat == "mace":
        m = mace(p); save(m, base, "item/mace.png"); return m
    if cat == "golden_apple":
        a = golden_apple(p); save(a, base, "item/golden_apple.png")
        save(golden_apple(p, True), base, "item/enchanted_golden_apple.png"); return a
    if cat == "spear":
        sp = spear(p)
        for t in TOOL_TIERS:
            save(sp, base, f"item/{t}_spear.png")
        return sp
    if cat == "trident":
        t = trident(p)
        save(t, base, "item/trident.png")
        save(entity_fill(p, seed, 32, 32), base, "entity/trident.png")
        return t
    if cat == "gui_color":
        img = Image.new("RGBA", (16, 16), p.d + (255,))
        d = ImageDraw.Draw(img)
        d.rectangle((1, 1, 14, 14), fill=p.m, outline=p.l)
        d.rectangle((4, 4, 11, 11), fill=p.l, outline=p.a)
        return img
    if cat == "glowstone":
        g = glowstone(p, seed); save(g, base, "block/glowstone.png"); return g
    if cat == "obsidian":
        o = obsidian(p, seed); save(o, base, "block/obsidian.png"); return o
    if cat == "anchor":
        save(anchor_top(p, seed, True), base, "block/respawn_anchor_top.png")
        save(anchor_top(p, seed, False), base, "block/respawn_anchor_top_off.png")
        save(obsidian(p, seed + 5), base, "block/respawn_anchor_bottom.png")
        for lvl in range(5):
            save(anchor_side(p, seed + lvl, lvl), base, f"block/respawn_anchor_side{lvl}.png")
        return anchor_top(p, seed, True)
    if cat == "crystal":
        save(crystal_item(p), base, "item/end_crystal.png")
        save(entity_fill(p, seed, 64, 32), base, "entity/end_crystal/end_crystal.png")
        return crystal_item(p)
    raise ValueError(cat)


CATEGORIES = ["glint", "sky", "armour", "totem", "sword", "axe", "pickaxe", "xp_bottle", "mace",
              "golden_apple", "spear", "trident", "gui_color", "glowstone", "anchor", "crystal", "obsidian"]


def main():
    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    index = {}
    for cat in CATEGORIES:
        ids = []
        for ci, (name, rgb) in enumerate(COLORS):
            for v in (1, 2, 3):
                sid = name.lower().replace(" ", "_") + ("" if v == 1 else f"_{v}")
                base = os.path.join(OUT, cat, sid)
                os.makedirs(base, exist_ok=True)
                p = Pal(rgb, v)
                pv = build(cat, p, base, seed=hash((cat, sid)) & 0xFFFF if False else ci * 31 + v * 7)
                pv.save(os.path.join(base, "preview.png"))
                with open(os.path.join(base, "palette.txt"), "w") as f:
                    f.write(p.txt())
                ids.append(sid)
        index[cat] = ids
    with open(os.path.join(OUT, "index.json"), "w") as f:
        json.dump(index, f, indent=1)
    print("fertig:", sum(len(v) for v in index.values()), "Styles")


if __name__ == "__main__":
    main()
