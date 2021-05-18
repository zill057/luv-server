package com.hiwangzi.luv.model.exception

class SystemException(
  message: String = "系统错误",
  cause: Throwable? = null
) : GeneralException(500, "SYSTEM_ERROR", message, cause)
