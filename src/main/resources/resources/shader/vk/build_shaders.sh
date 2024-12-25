#!/usr/bin/env bash

for f in `ls *.vert`; do
  echo "compiling vertex shader $f"
  glslc $f -o $f.spv
done

for f in `ls *.frag`; do
  echo "compiling fragment shader $f"
  glslc $f -o $f.spv
done
