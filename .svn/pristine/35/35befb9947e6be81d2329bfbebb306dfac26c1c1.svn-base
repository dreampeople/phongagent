#!/usr/bin/env python3

import asyncio
from AgentConn import AgentConn
from AgentServer import AgentServer

async def mainTest():
    await AgentConn(None).start()

async def main():
    #启动服务
    await AgentServer.start("0.0.0.0", 9939, AgentServer.startRun)

if __name__ == "__main__":
    #asyncio.run(mainTest())
    asyncio.run(main())
