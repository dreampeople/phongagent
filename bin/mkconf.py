#!/usr/bin/env python3
import os, sys, re
from melonlib import mutil
from melonlib.mobj import mobj

basedir = os.path.abspath(__file__)
basedir = os.path.dirname(os.path.dirname(basedir))

def getLuaDesc(fname):
    lines = open(fname, "r").readlines()
    flag = 0
    success = True
    o = mobj()
    o.params = {}
    for line in lines:
        if flag == 0 and re.match(r'^--\s*begin[\s\t]*',line):
            flag = 1
            continue
        if flag != 1: continue
        if re.match(r'^--\s*end[\s\t]*',line):
            flag = 2
            break

        if line[:2] != '--':
            success = False
            break
        line = line[2:].strip()

        if line.startswith('desc:'):
            line = line[len('desc:'):].strip()
            o.description = line
            continue

        if line.startswith('param:'):
            line = line[len('param:'):].strip()
            p = mobj(eval(line))
            o.params[p.name] = {'type': p.type, 'description': p.desc, 'require': p.require}
            continue

    if not success: return
    if flag != 2: return

    if 'description' not in o: return

    if 'params' not in o:
        o.params = []

    return o

def compileLua(fromFile, toFile):
    lines = open(fromFile, 'r').readlines()
    f = open(toFile, "w")
    for line in lines:
        if line.startswith('--'):
            continue
        f.write(line)
    f.close()

success = True
lines = []
files = os.listdir(f'{basedir}/luafile')
for fname in files:
    if not fname.endswith('.lua'): continue

    fileName = f'{basedir}/luafile/{fname}'
    luaName = fname[:-4]
    ofileName = f'{basedir}/luacfile/{fname}'

    o = getLuaDesc(fileName)
    if o is None:
        success = False
        print(f'{fname} 缺少描述')
        continue

    ret = mutil.runShell2(f'luac -p {fileName}')
    if len(ret[1]) > 0:
        success = False
        print(f'{fname} error: {ret[1]}')
        continue

    #ret = mutil.runShell2(f'luac -o {ofileName} {fileName}')

    compileLua(fileName, ofileName)
    open(f'{ofileName}.json','w').write(o.toJson())
    m5 = mutil.getFileMd5(ofileName)
    lines.append(f'{luaName},{m5}')

open(f'{basedir}/luacfile/config.ini','w').write('\n'.join(lines))
