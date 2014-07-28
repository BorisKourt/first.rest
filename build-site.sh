#!/bin/sh
echo "bldr >> Hello, $USER! Now building this stasis project:"
echo "     \- Compiling Styles."
sass src/scss/main.scss resources/public/styles/main.css
echo "bldr >> Styles Compiled"
echo "     \- Fetching deps."
lein deps
echo "bldr >> Deps Fetched"
echo "     \- Building Site."
lein build-site
echo "bldr >> Site Built in .html/"
echo "     \- Churning out the docs."
lein marg -d html/doc/ -f index.html
echo "bldr >> Docs Built in .html/doc/"
echo "     \- Checking for out-dated deps."
lein ancient
echo "bldr >> DONE!"