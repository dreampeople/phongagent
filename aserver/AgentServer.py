import os, json
from acomm import basedir, luadir, luacdir
from AgentBrain import AgentBrain
from melonlib.maconn import maconnserver, maconn
from AgentTools import *
import traceback

class AgentServer(maconnserver):
    def __init__(self, conn, addr):
        super().__init__(conn, addr)
        self.brain = None

    def getBrain(self):
        if self.brain != None: return self.brain

        self.brain = AgentBrain("Agent")
        #self.brain.addTool(GreetTool(self.conn))
        tools = getAgentTools(self.conn)
        for tool in tools:
            self.brain.addTool(tool)

        return self.brain

    async def retData(self, ret, **params):
        '''
        返回数据
        '''

        print('>>>>>', params)
        await maconn.send(self.conn, ret, json.dumps(params))

    async def doChat(self):
        '''
        处理命令
        '''

        print('>>> doChat')
        msg = self.recvBuf.decode()
        print(msg)

        try:
            response = await self.getBrain().arun(msg)
            #print(response['messages'][-1].content)
            content = response['messages'][-1].content
            await self.retData(0, content=content)
        except Exception as e:
            traceback.print_exc()
            await self.retData(-1, errmsg = str(e))

    async def getLuaList(self):
        '''
        获取lua列表
        '''
        print('>>> getLuaList')
        lines = open(f"{luacdir}/config.ini", "r").readlines()
        luaList = []
        for line in lines:
            line = line.strip()
            if line == '' or line[0] == '#':
                continue
            name,m5 = line.split(",")
            luaList.append({"name":name, "md5":m5})
        await self.retData(0, luaList=luaList)

    async def getLuaFile(self):
        '''
        获取lua文件
        '''
        #print('>>> getLuaFile')
        #print(self.recvBuf)
        luaFileName = self.recvBuf.decode()
        #print(luaFileName)
        luaFileName = f"{luacdir}/{luaFileName}.lua"
        if not os.path.exists(luaFileName):
            await self.retData(-1, errmsg = "file not found")
            return

        #print(luaFileName)
        luaFile = open(luaFileName , "rb").read()
        #print(luaFile)
        await maconn.send(self.conn, 0, luaFile)

    async def runStart(self):
        '''
        当客户端连接时，会调用这个函数，处理业务逻辑
        '''
        print('>>> runStart')


    async def runLoop(self, bType):
        '''
        处理业务逻辑
        '''

        print('>>> runLoop', bType)

        try:
            if bType == 0:
                await self.doChat()
                return

            if bType == 10:
                await self.getLuaList()
                return

            if bType == 11:
                await self.getLuaFile()
                return
        except:
            traceback.print_exc()

    async def runEnd(self):
        '''
        当客户端断开连接时，会调用这个函数
        '''
        print('>>> runEnd')

    @staticmethod
    async def serverStarted(conn):
        '''
        当服务器启动时，会调用这个函数
        conn: 服务端连接对象
        '''
        pass

    @staticmethod
    async def startRun(conn, addr):
        '''
        服务器启动时，会调用这个函数，处理业务逻辑
        '''

        ss = AgentServer(conn, addr)
        try:
            await ss.run()
        except Exception as e:
            print(e)
            conn.close()


