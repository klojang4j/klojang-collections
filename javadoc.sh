#!/bin/bash

rm -rf docs/api/*

src="src/main/java"
klojang_home="~/git-repos/klojang"

#   --module-source-path "${src}:${klojang_home}/klojang-check/${src}:${klojang_home}/klojang-util/${src}" \

javadoc \
  --source-path "${src}:${klojang_home}/klojang-check/${src}:${klojang_home}/klojang-util/${src}" \
  -d docs/api \
  -protected \
  --module org.klojang.collections \
  -link https://docs.oracle.com/en/java/javase/18/docs/api/index.html \
  -windowtitle 'Klojang Collections' \
  org.klojang.collections
