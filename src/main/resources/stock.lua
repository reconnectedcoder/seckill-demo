if (redis.call('exists', KEYS[1]) == 1) then
    local stock = tonumber(redis.call('get', KEYS[1]));
    if (stock > 0) then
        redis.call('incrby', KEYS[1], -1);
        return stock;
    end;
        return 0;
end;

-- 多个步骤：1.对key加锁
-- 2.获取加锁的key
-- 3.比较加锁的key
-- 4.删除锁