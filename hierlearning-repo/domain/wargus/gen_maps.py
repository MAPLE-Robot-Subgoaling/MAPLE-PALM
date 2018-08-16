#!/usr/bin/python
# Makes multiple Wargus maps

import os

for m in range(100):
    os.system("rgmap 20 20 3 15 2 3 -o maps/map_" + str(m) + ".pud.gz")
