package com.hiwangzi.luv.model.exception

import com.hiwangzi.luv.model.exception.general.LuvException

class SystemError(
  message: String = "系统错误",
  cause: Throwable? = null
) : LuvException(500, "SYSTEM_ERROR", message, cause)
