## 简介

* 遵循统一的 Restful 的设计风格
* 使用 JSON 作为数据交互的格式
* 所有需要鉴权的接口均需设置 HTTP 头部：`Authorization: Bearer $accessToken`，详情见[鉴权机制](#Authentication)

## 规则说明

### 基本信息

* 所有的 API 请求必须使用 HTTPS
* 请求时不应忽略服务器证书验证的错误，避免被恶意劫持

### 数据格式

* API 使用 JSON 作为消息体的数据交换格式
* 图片上传 API 除外

### 参数兼容性

* 请求是否成功，与请求参数的顺序无关
* 请求是否成功，与请求 JSON 中的键值对出现的顺序无关
* 处理应答时，不应假设应答 JSON 中的键值对出现的顺序
* 新的 API 版本可能在请求或应答中加入新的参数或者 JSON 的键值对
* 新的 API 版本不会去除请求和应答中已经存在的必填参数或者 JSON 的键值对
* 当请求或应答中的 JSON 键值对的值为空（`null`）时，可以省略

### 字符集

* API 使用 UTF-8 字符编码

### 日期格式

* 所有的日期对象，使用毫秒时间戳表示，示例：`1618983268000`

### 错误信息

* API 使用 HTTP 状态码来表示请求处理的结果
  * 处理成功的请求，如果有应答的消息体将返回 `200`，若没有应答的消息体将返回 `204`
  * 已经被成功接受待处理的请求，将返回 `202`
  * 请求处理失败时，如缺少必要的入参，将会返回 `4xx` 范围内的错误码
  * 请求处理时发生了服务器侧的服务系统错误，将返回 `500`/`501`/`503` 的状态码

### 错误码和错误提示

当请求处理失败时，除了 HTTP 状态码表示错误之外，API 将在消息体返回错误相应说明具体的错误原因

```json
{
  "code": "PARAM_ERROR",
  "message": "参数错误",
  "detail": {
    "field": "/amount/currency",
    "value": "XYZ",
    "issue": "Currency code is invalid",
    "location": "body"
  }
}
```

* `code`：详细错误码
* `message`：错误描述，使用易理解的文字表示错误的原因
* `field`: 指示错误参数的位置。当错误参数位于请求 body 的JSON时，填写指向参数的 [JSON Pointer](https://tools.ietf.org/html/rfc6901) 。当错误参数位于请求的 url
  或者 querystring 时，为参数的变量名。
* `value`: 错误的值
* `issue`: 具体错误原因

### 应答的语种

* API 允许调用方声明应答中的错误描述使用的自然语言语种。如果有需要，设置请求的 HTTP 头 `Accept-Language`。目前支持：
  * en
  * zh-CN
* 当不设置或者值不支持时，将使用简体中文（zh-CN）。

## HTTP 状态码

|           状态码          |              错误类型              |                一般的解决方案                 |       常见示例      |
|---------------------------|------------------------------------|-----------------------------------------------|---------------------|
| 200 - OK                  |               处理成功             |                                               |                     |
| 202 - Accepted            |    服务器已接受请求，但尚未处理    |                                               |                     |
| 204 - No Content          |        处理成功，无返回 Body       |                                               |                     |
| 400 - Bad Request         |          协议或者参数非法          |      请根据接口返回的详细信息检查您的程序     | PARAM_ERROR         |
| 401 - Unauthorized        |            身份验证失败            |              请检查 token 信息                | EXPIRED_TOKEN       |
| 403 - Forbidden           |              权限异常              |                 相关权限不足                  |                     |
| 404 - Not Found           |           请求的资源不存在         |   请检查需要查询的 id 或者请求 URL 是否正确   |                     |
| 429 - Too Many Requests   |           请求超过频率限制         |         请求未受理，请降低频率后重试          |                     |
| 500 - Server Error        |              系统错误              |         按具体接口的错误指引进行重试          | SYSTEM_ERROR        |
| 502 - Bad Gateway         |         服务下线，暂时不可用       |           请求无法处理，请稍后重试            |                     |
| 503 - Service Unavailable |         服务不可用，过载保护       |           请求无法处理，请稍后重试            |                     |

<h2 id="Authentication">鉴权机制</h2>

* 客户端请求登录接口，服务端返回访问凭据（accessToken）、刷新凭据（refreshToken）
* accessToken 有效期为 1 天，当使用过期 accessToken 访问接口时会返回错误 `401`, 错误码为 `EXPIRED_TOKEN`
* refreshToken 有效期为 7 天，当使用过期 refreshToken 访问接口时会返回错误 `401`, 错误码为 `EXPIRED_REFRESH_TOKEN`
* 可以使用过期 accessToken 访问 token 刷新接口，获取新的访问凭据（accessToken）、刷新凭据（refreshToken）
