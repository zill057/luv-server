package com.hiwangzi.luv.model.exception

class InvalidCredentialException(
  message: String = "用户名或密码错误",
  cause: Throwable? = null
) : GeneralException(401, "INVALID_CREDENTIAL", message, cause)
