package com.hiwangzi.luv.model.exception

class NotFoundException(
  message: String,
  cause: Throwable? = null
) : LuvGeneralException(404, "NOT_FOUND", message, cause)
