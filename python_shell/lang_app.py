# coding=utf-8
from flask import Flask, request
import json
from pymongo import MongoClient
from redis import ConnectionPool, StrictRedis
from logger import logger

# redis 是lpush key 是webchat value：电话号码
app = Flask(__name__)
myaddr = "172.12"
if "172.17" in myaddr:  # 本机的IP是172
    redis_url = "redis://:BIgsfintech27581@172.17.0.168:6379/14"
    mon_url = "mongodb://admin:Mongo2018914@172.17.0.170:27017/online_data"
else:  # 线上
    pass


logger.info("加载配置文件")
logger.info("redis环境" + redis_url)
logger.info("mongodb环境" + mon_url)

pool = ConnectionPool.from_url(url=redis_url, decode_responses=True)
redis_cli = StrictRedis(connection_pool=pool)

mc = MongoClient(mon_url, maxPoolSize=10)
db = mc['online_data']
wechat_friends = db["wechat_friends"]


@app.route('/get_one')
def get_one():
    logger.info(request.url)
    value = []
    for i in range(1):
        if redis_cli.lpop("wechat") is not None:
            value.append(redis_cli.lpop("wechat"))
    print(json.dumps(value))
    return json.dumps(value)


# 队列一号
@app.route('/get_phone')
def get_phone():
    logger.info(request.url)
    value = []
    for i in range(10):
        item = redis_cli.lpop("wechat")
        if item is not None:
            value.append(item)
    logger.info(json.dumps(value))
    return json.dumps(value)


# 队列2号
@app.route('/get_phone2')
def get_phone2():
    logger.info(request.url)
    value = []
    for i in range(10):
        item = redis_cli.lpop("wechat2")
        if item is not None:
            value.append(item)
    logger.info(json.dumps(value))
    return json.dumps(value)


# http://127.0.0.1:5000/save_data?data={"result":[{"phone":"asf","wxid":"adf"},{"phone":"asf","wxid":"adf"}]}
@app.route('/save_data')
def save_date():
    logger.info(request.url)
    args = request.args
    if args.get("data", "") != "":
        data = args.get("data", "")
        logger.info("data>>>>>>"+data)
        json_str = json.loads(data)
        phones = json_str["result"]
        # 有wxid保存，没有放入redis
        for phone in phones:
            if phone["wxid"] != "":
                wechat_friends.update({"phone": phone["phone"], "status": 0},
                                      {"$set": {"status": 1, "wxid": phone["wxid"]}})
            else:
                wechat_friends.update({"phone": phone["phone"], "status": 0},
                                      {"$set": {"status": 2, "wxid": phone["wxid"]}})
                redis_cli.lpush("wechat_2", phone["phone"])
        return "ok"
    else:
        return "error"


# http://127.0.0.1:5000/save_data?data={"result":[{"phone":"asf","wxid":"adf"},{"phone":"asf","wxid":"adf"}]}
@app.route('/save_data2')
def save_data2():
    logger.info(request.url)
    args = request.args
    if args.get("data", "") != "":
        data = args.get("data", "")
        logger.info("data>>>>>>" + data)
        json_str = json.loads(data)
        phones = json_str["result"]
        # 有wxid保存，没有放入redis
        for phone in phones:
            if phone["wxid"] != "":
                wechat_friends.update({"phone": phone["phone"], "status": 0},
                                      {"$set": {"status": 1, "wxid": phone["wxid"]}})
            else:
                wechat_friends.update({"phone": phone["phone"], "status": 0},
                                      {"$set": {"status": -1, "wxid": phone["wxid"]}})
        return "ok"
    else:
        return "error"


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=55555, debug=True)
