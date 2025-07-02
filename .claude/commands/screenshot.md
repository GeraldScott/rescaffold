1. Search through /home/geraldo/Pictures/Screenshots to find the latest screenshot by date modified using
   Bash(find "/home/geraldo/Pictures/Screenshots" -name "\*.png" -type f -exec ls -lt {} + | head -1)
2. Comment on the contents of the screenshot