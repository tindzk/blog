#!/usr/bin/env nu

tar -vczf styles.tar.gz blog.styles
mc cp styles.tar.gz blog-data/timsblog/styles.tar.gz

cd static/
mc cp favicon.ico blog-data/timsblog/assets/images/
mc cp favicon.svg blog-data/timsblog/assets/images/
let fonts = (ls *.woff2 | get name)
if ($fonts | length) > 0 {
    mc cp ...$fonts blog-data/timsblog/assets/fonts/
}
cd ..

cd articles/
tar -vczf ../articles.tar.gz ...(ls *.md *.toml | get name)
mc cp ../articles.tar.gz blog-data/timsblog/articles.tar.gz
let article_assets = (ls *.jpg | get name)
if ($article_assets | length) > 0 {
    mc cp ...$article_assets blog-data/timsblog/assets/articles/
}
cd ..

cd posts/
tar -vczf ../posts.tar.gz ...(ls *.md | get name)
mc cp ../posts.tar.gz blog-data/timsblog/posts.tar.gz
let post_assets = (ls *.png | get name)
if ($post_assets | length) > 0 {
    mc cp ...$post_assets blog-data/timsblog/assets/posts/
}
cd ..

cd about/
let about_assets = (ls *.png | get name)
if ($about_assets | length) > 0 {
    mc cp ...$about_assets blog-data/timsblog/assets/about/
}
