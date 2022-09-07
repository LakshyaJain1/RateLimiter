package com.ratelimiter.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rate Limit Exception which should be thrown in case of any exception occur
 * while using the library.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitException extends RuntimeException {
    private int status;
    private String message;
}
