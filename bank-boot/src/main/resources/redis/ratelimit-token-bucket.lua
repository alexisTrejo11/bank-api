-- KEYS[1] = redis key (hash)
-- ARGV[1] = capacity (integer burst / max tokens)
-- ARGV[2] = refill tokens per second (double)
-- ARGV[3] = now epoch millis (long)
-- Returns: { allowed (1|0), remaining_floor, retry_after_seconds }

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_per_sec = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local t = redis.call('HMGET', key, 'tokens', 'last_ts')
local tokens = tonumber(t[1])
local last_ts = tonumber(t[2])

if not tokens then
  tokens = capacity
  last_ts = now
end

local elapsed = math.max(0, (now - last_ts) / 1000.0)
tokens = math.min(capacity, tokens + elapsed * refill_per_sec)
last_ts = now

if tokens >= 1.0 - 1e-9 then
  tokens = tokens - 1
  redis.call('HSET', key, 'tokens', tostring(tokens), 'last_ts', tostring(last_ts))
  redis.call('EXPIRE', key, 7200)
  return { 1, math.floor(tokens), 0 }
else
  redis.call('HSET', key, 'tokens', tostring(tokens), 'last_ts', tostring(last_ts))
  redis.call('EXPIRE', key, 7200)
  local need = 1.0 - tokens
  local retry_sec = 60
  if refill_per_sec > 1e-9 then
    retry_sec = math.max(1, math.ceil(need / refill_per_sec))
  end
  return { 0, 0, retry_sec }
end
