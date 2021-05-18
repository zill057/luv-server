package com.hiwangzi.luv.model.exception

class ExpiredTokenError(
  message: String = "Access token expired",
  cause: Throwable? = null
) : GeneralException(401, "EXPIRED_TOKEN", message, cause)
