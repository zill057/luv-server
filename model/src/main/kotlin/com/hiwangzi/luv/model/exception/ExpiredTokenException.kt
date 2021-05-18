package com.hiwangzi.luv.model.exception

class ExpiredTokenException(
  message: String = "Access token expired",
  cause: Throwable? = null
) : LuvGeneralException(401, "EXPIRED_TOKEN", message, cause)
