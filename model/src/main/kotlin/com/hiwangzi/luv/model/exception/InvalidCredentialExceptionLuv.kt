package com.hiwangzi.luv.model.exception

class InvalidCredentialExceptionLuv(
  message: String = "用户名或密码错误",
  cause: Throwable? = null
) : LuvGeneralException(401, "INVALID_CREDENTIAL", message, cause)
