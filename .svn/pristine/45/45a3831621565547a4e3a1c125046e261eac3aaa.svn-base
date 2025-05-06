from AgentBrain import AgentBrain
from AgentTools import GreetTool, getAgentTools

class AgentConn:
    def __init__(self, conn):
        self.conn = conn
        self.brain = None

    def getBrain(self):
        if self.brain != None: return self.brain

        self.brain = AgentBrain("Agent")
        self.brain.addTool(GreetTool(self.conn))
        agentTools = getAgentTools(self.conn)
        for tool in agentTools:
            self.brain.addTool(tool)
        return self.brain

    async def start(self):
        print("Starting the agent server...")
        if self.conn is None:
            await self.startTest()
            return

    async def startTest(self):
        while True:
            msg = input("Ask the AI agent: ")
            if not msg:
                continue  # 如果输入为空，则继续等待用户输入
            # 检查用户输入是否为退出指令
            if msg.lower() in ["exit", "quit"]:
                print("Goodbye!")
                break

            response = await self.getBrain().arun(msg)
            print(response['messages'][-1].content)
