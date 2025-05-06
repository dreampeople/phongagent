import os, re
from acomm import basedir, luadir, luacdir
from melonlib.maconn import maconn
from BaseAgentTool import BaseAgentTool
from melonlib.mobj import mobj
import traceback

luaFiles = {}
# 定义一个函数 getAgentTools，用于获取所有 Lua 代理工具的实例
def getAgentTools(conn):
    ret = []
    for lua in luaFiles:
        # 创建一个 LuaAgentTool 实例，并将其添加到 ret 列表中
        ret.append(LuaAgentTool(conn, luaFiles[lua]))
    return ret

class GreetTool(BaseAgentTool):
    def __init__(self, conn):
        super().__init__(conn)
        self.name = "Greet"
        self.description = "用名字作为参数问候一个人"
        self.params = {
            "name": {
                "type": "string",
                "description": "要问候的人的名字",
                "required": True
            }
        }

    def doFunc(self, name: str) -> str:
        """Greet a person by their name."""
        return f"Hello, {name}!"

    async def doAFunc(self, name: str) -> str:
        """Greet a person by their name."""
        return f"Hello, {name}!"

class LuaAgentTool(BaseAgentTool):
    def __init__(self, conn, lua):
        super().__init__(conn)
        self.name = lua.name
        self.description = lua.description
        self.params = lua.params
        print(self.name, self.description)
        print(self.params)

    def doFunc(self, **kwargs) -> str:
        """Run a lua function."""
        o = mobj()
        o.func = self.name
        o.params = kwargs
        self.conn.sendType('lua', o.toJson())

    async def doAFunc(self, **kwargs) -> str:
        """Run a lua function."""
        print("call doAFunc ", self.name, kwargs)
        #return f"Hello, {kwargs['name']}!"
        o = mobj()
        o.func = self.name
        o.params = kwargs
        await maconn.send(self.conn, 0, o.toJson())
        try:
            buf = await maconn.recvBufWithVerify(self.conn)
            if not buf:
                return 'call error: none'

            buf = buf.decode()
        except Exception as e:
            buf = '???'
            traceback.print_exc()
        if buf.startswith('ok:'):
            return buf[3:]

        return buf

def go():
    fns = os.listdir(luacdir)

    for fn in fns:
        if not fn.endswith('.lua'): continue
        name = fn[:-4]
        fname = f'{luacdir}/{fn}'
        jfname = f'{fname}.json'
        o = mobj(open(jfname, 'r').read())
        o.name = name
        if o is None: continue

        luaFiles[name] = o

go()
