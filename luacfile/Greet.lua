
local Date = luajava.bindClass("java.util.Date")
local date = luajava.newInstance("java.util.Date")

local APKUtil = luajava.bindClass("com.melon.util.APKUtil")


function go(params)
    APKUtil:openAppDetail("com.melon.phoneagent")
	return date:toString() .. "> Hello " .. params.name .. " !"
end

