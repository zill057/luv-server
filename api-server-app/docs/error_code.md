# 接口错误码

|状态码|错误码|描述|解决方案|
|-----|-----|----|------|
|404|NOT_FOUND|请求的资源不存在|请商户检查需要查询的id或者请求URL是否正确|
|500|SYSTEM_ERROR|系统错误|5开头的状态码都为系统问题，请使用相同参数稍后重新调用|
|400|PARAM_ERROR|参数错误|根据错误提示，传入正确参数|
|401|INVALID_CREDENTIAL|用户名或密码错误||
|401|EXPIRED_TOKEN|访问凭据已失效|调用token刷新接口获取新token|
|401|EXPIRED_REFRESH_TOKEN|刷新凭据已失效|退出系统|
