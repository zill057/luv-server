package com.hiwangzi.luv.model.exception

class SystemException(
  message: String = "系统错误",
  cause: Throwable? = null
) : LuvGeneralException(500, "SYSTEM_ERROR", message, cause)
