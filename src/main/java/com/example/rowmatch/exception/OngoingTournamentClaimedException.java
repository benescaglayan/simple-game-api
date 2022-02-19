package com.example.rowmatch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Unable to claim ongoing tournament's reward.")
public class OngoingTournamentClaimedException extends Throwable {
}
