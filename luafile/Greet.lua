-- begin
-- desc: 用名字作为参数问候一个人
-- param: {'name': 'name', 'type': 'string', 'desc': '要问候的人的名字', 'require': True}
-- end

local Date = luajava.bindClass("java.util.Date")
local date = luajava.newInstance("java.util.Date")

local APKUtil = luajava.bindClass("com.melon.util.APKUtil")


function go(params)
    APKUtil:openAppDetail("com.melon.phoneagent")
	return date:toString() .. "> Hello " .. params.name .. " !"
end

