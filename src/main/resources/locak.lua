if redis.call("get",KEYS[1]) == ARGV[1] then
    return redis.call("del",KEYS[1])
else
    return 0
end

-- KEYS[1] 获取传入的key参数
--
-- ARGV[1] 获取传入的limit参数



