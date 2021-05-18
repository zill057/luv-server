package com.hiwangzi.luv.model.exception

class ExpiredRefreshTokenException(
  message: String = "Refresh token expired",
  cause: Throwable? = null
) : LuvGeneralException(401, "EXPIRED_REFRESH_TOKEN", message, cause)
