#!/usr/bin/env nu

use std/formats *

let cache_path = "s3-cache.jsonl"
mut cache = open $cache_path

tar -vczf styles.tar.gz blog.styles

cd articles/
tar -vczf ../articles.tar.gz ...(ls *.md *.toml | get name)
cd ..

cd posts/
tar -vczf ../posts.tar.gz ...(ls *.md | get name)
cd ..

mut files_to_copy = [
    { source: "styles.tar.gz", target: "blog-data/timsblog/styles.tar.gz" },
    { source: "articles.tar.gz", target: "blog-data/timsblog/articles.tar.gz" },
    { source: "posts.tar.gz", target: "blog-data/timsblog/posts.tar.gz" },
    { source: "static/favicon.ico", target: "blog-data/timsblog/assets/images/favicon.ico" },
    { source: "static/favicon.svg", target: "blog-data/timsblog/assets/images/favicon.svg" }
]

let fonts = (ls static/*.woff2 | get name)
for $font in $fonts {
    let target_path = $"blog-data/timsblog/assets/fonts/($font | path basename)"
    $files_to_copy = ($files_to_copy | append { source: $font, target: $target_path })
}

let article_assets = (ls articles/*.jpg | get name)
for $asset in $article_assets {
    let target_path = $"blog-data/timsblog/assets/articles/($asset | path basename)"
    $files_to_copy = ($files_to_copy | append { source: $asset, target: $target_path })
}

let post_assets = (ls posts/*.png | get name)
for $asset in $post_assets {
    let target_path = $"blog-data/timsblog/assets/posts/($asset | path basename)"
    $files_to_copy = ($files_to_copy | append { source: $asset, target: $target_path })
}

let about_assets = (ls about/*.png | get name)
for $asset in $about_assets {
    let target_path = $"blog-data/timsblog/assets/about/($asset | path basename)"
    $files_to_copy = ($files_to_copy | append { source: $asset, target: $target_path })
}

for $file in $files_to_copy {
    if (not ($file.source | path exists)) {
        echo $"File not found: ($file.source)"
        continue
    }

    let current_hash = (open -r $file.source | xxh64sum | split row " " | first)
    let cached_entry = ($cache | where target == $file.target)

    if ($cached_entry | is-empty) {
        echo $"New file: ($file.source)"
    } else {
        let cached_hash = ($cached_entry | first).hash
        if ($cached_hash == $current_hash) {
            echo $"($file.source) up-to-date"
            continue
        } else {
            echo $"File changed: ($file.source)"
        }
    }

    mc cp $file.source $file.target

    $cache = ($cache | where target != $file.target)
    $cache = ($cache | append { target: $file.target, hash: $current_hash })
    $cache | save --force $cache_path
}
