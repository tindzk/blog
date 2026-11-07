# Web fonts

Inter and JetBrains Mono are hosted here as WOFF2 variable fonts, retaining all
upstream glyphs, normal and italic styles and the original variable axes. Inter
supports weights 100–900 and optical sizes 14–32. JetBrains Mono supports weights
100–800. The filenames include the source font's Git blob hash prefix.

Source: https://github.com/google/fonts/tree/86aced91de05cd4f96a06838124cca45be337b2b/ofl

The SIL Open Font Licences are included in `inter-OFL.txt` and
`jetbrainsmono-OFL.txt`. Reproduce the files from the pinned sources with:

```sh
uv run scripts/vendor-fonts.py
```

`blog.styles` declares these fonts inline with `font-display: swap`. There is no
external font stylesheet or JavaScript loader. Fallback fonts render while WOFF2
files download. Keep the CSS URL paths in single quotes so the Rust template
loader applies the configured asset URL in production.

`scripts/publish.nu` uploads the fonts and licences, then the template bundle.
Versioned font files receive an immutable one-year cache lifetime. When updating
fonts, update the source revision, hash suffixes, CSS URLs and local Rust routes
together. Do not replace font contents under an existing versioned filename.
