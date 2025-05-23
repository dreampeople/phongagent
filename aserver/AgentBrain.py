#from langchain_deepseek import ChatDeepSeek
from langchain_community.chat_models.tongyi import ChatTongyi

from langchain_core.messages import AIMessage, HumanMessage, SystemMessage, ToolMessage

from dotenv import load_dotenv
load_dotenv()

class AgentBrain:
    """
    AgentBrain 类表示一个智能代理的大脑。

    属性:
        name (str): 代理的名称。

    方法:
        __init__(name):
            初始化 AgentBrain 实例，设置代理的名称。
    """
    def __init__(self, name):
        self.name = name
        #self.llm = ChatDeepSeek(temperature=0,model="deepseek-chat")
        self.llm = ChatTongyi(temperature=0, model="qwen-max")
        #self.llm = OpenAI(temperature=0)
        self.agentTools = {}
        self.llm_with_tools = None

    def addTool(self, agentTool):
        """
        添加工具到代理的大脑。

        参数:
            name (str): 工具的名称。
            func (callable): 工具的功能函数。
            description (str): 工具的描述。
        """
        self.agentTools[agentTool.name] = agentTool

    def invoke(self):
        """
        运行代理的大脑，处理输入并生成响应。
        参数:
            msg (str): 用户输入的文本。
        返回:
            str: 代理的响应。
        """

        response = self.llm_with_tools.invoke(self.messages)

        if response is None:
            self.messages.append(AIMessage(content="抱歉，我无法回答这个问题。"))  # Add the response to the conversation history i
            return self.messages

        if not response.tool_calls:
            self.messages.append(response)  # Add the response to the conversation history i
            return self.messages

        self.messages.append(response)

        for tool_call in response.tool_calls:
            tool_name = tool_call['name']
            args = tool_call['args']
            tool_call_id = tool_call['id']

            if tool_name in self.agentTools:
                ret = self.agentTools[tool_name].invoke(args)
                print('ret=', ret)
            else:
                ret = f"未知工具: {tool_name}"
            self.messages.append(ToolMessage(content=ret, tool_call_id=tool_call_id))

        return self.invoke()
 
    async def ainvoke(self):
        """
        运行代理的大脑，处理输入并生成响应。
        参数:
            msg (str): 用户输入的文本。
        返回:
            str: 代理的响应。
        """

        response = await self.llm_with_tools.ainvoke(self.messages)

        if response is None:
            self.messages.append(AIMessage(content="抱歉，我无法回答这个问题。"))  # Add the response to the conversation history i
            return self.messages

        if not response.tool_calls:
            self.messages.append(response)  # Add the response to the conversation history i
            return self.messages

        self.messages.append(response)

        for tool_call in response.tool_calls:
            tool_name = tool_call['name']
            args = tool_call['args']
            tool_call_id = tool_call['id']

            if tool_name in self.agentTools:
                ret = await self.agentTools[tool_name].ainvoke(args)
                print('ret=', ret)
            else:
                ret = f"未知工具: {tool_name}"
            self.messages.append(ToolMessage(content=ret, tool_call_id=tool_call_id))

        return await self.ainvoke()

    def _run(self, msg):
        """
        运行代理的大脑，处理输入并生成响应。
        参数:
            msg (str): 用户输入的文本。
        返回:
            str: 代理的响应。
        """

        if self.llm_with_tools is None:
            tools = []
            for tool in self.agentTools.values():
                t = tool.getTool()
                if t is None: continue
                tools.append(t)
            print(tools)
            #self.agent = create_react_agent(self.llm, tools)
            self.llm_with_tools = self.llm.bind_tools(tools)

        print("Running the agent brain...")
        self.messages = [
            SystemMessage(content="这是一个Android手机的智能助手,你可以决策调用哪个工具实现用户要求,结束时不要问用户问题。"),
            HumanMessage(content=msg)
        ]

        return

    def run(self, msg):
        """
        运行代理的大脑，处理输入并生成响应。
        参数:
            msg (str): 用户输入的文本。
        返回:
            str: 代理的响应。
        """

        self._run(msg)
        self.invoke()
        return {'messages': self.messages}

    async def arun(self, msg):
        """
        运行代理的大脑，处理输入并生成响应。
        参数:
            msg (str): 用户输入的文本。
        返回:
            str: 代理的响应。
        """

        print("Running the agent brain...")
        self._run(msg)
        print("ainvoke...")
        #messages = {'messages': messages}
        await self.ainvoke()
        print("ainvoke end")
        return {'messages': self.messages}
