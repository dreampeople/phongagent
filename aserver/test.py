#!/usr/bin/env python3

import asyncio
import struct
import json
from melonlib import mutil
from melonlib.maconn import maconn

async def doChat(conn):
    print('doChat...........')
    await maconn.sendType(conn, 0, "my name is hello")
    buf = await maconn.recvBufWithVerify(conn)
    buf = buf.decode()
    print(buf)

async def doListLua(conn):
    await maconn.sendType(conn, 10)
    buf = await maconn.recvBufWithVerify(conn)
    ret = json.loads(buf)
    print(ret)
    return ret

async def doGetLuaFile(conn, fileName, m5):
    await maconn.sendType(conn, 11, fileName)
    buf = await maconn.recvBufWithVerify(conn)
    print(mutil.getStringMd5(buf), m5)
    print(buf)

async def main():
    conn = await maconn.connectServer('127.0.0.1', 9939)
    #await doChat(conn)
    j = await doListLua(conn)
    for name in j:
        m5 = j[name]
        await doGetLuaFile(conn, name, m5)
    conn.close()

asyncio.run(main())
