from langchain.agents import Tool

class BaseAgentTool:
    def __init__(self, conn):
        self.conn = conn
        self.name = 'agentName'
        self.description = "This is a tool for the agent."
        self.params = {}

    def getTool(self):
        """
        获取工具的描述信息，以字典形式返回。

        该方法会根据 `self.params` 中的参数信息，构建一个符合规范的工具描述字典。
        字典中包含工具的名称、描述、参数类型、参数描述、是否必需等信息。

        Returns:
            dict: 工具的描述信息字典，如果 `self.doFunc` 为 None，则返回 None。
        """
        if self.doFunc is None: return None

        params = {}
        for k, v in self.params.items():
            param = {
                "type": v.get("type", "string"),
                "description": v.get("description", ""),
            }

            # 处理缺省值
            if "default" in v:
                param["default"] = v["default"]
                v["required"] = False

            # 处理枚举值
            if "enum" in v:
                param["enum"] = v["enum"]

            params[k] = param

        return {
            "type": "function",
            "function": {
                "name": self.name,
                "description": self.description,
                "parameters": {
                    "type": "object",
                    "properties": params,
                    "required": [k for k, v in self.params.items() if v.get('required', True) ] # 提取所有 required 为 True 的参数名称为 required 数组，其他为 optional 数组
                }
            }
        }

    def invoke(self, args):
        if self.doFunc is None: return None
        return self.doFunc(**args)

    async def ainvoke(self, args):
        if self.doFunc is None: return None
        return await self.doAFunc(**args)
