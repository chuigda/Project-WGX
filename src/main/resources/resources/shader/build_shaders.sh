#!/usr/bin/env bash

for f in `find . -iname *.vert | grep vk`; do
  echo "compiling vertex shader $f"
  glslc $f -o $f.spv
done

for f in `find . -iname *.frag | grep vk`; do
  echo "compiling fragment shader $f"
  glslc $f -o $f.spv
done
